package dbp.exploreconnet.comment.application;

import com.jayway.jsonpath.JsonPath;
import com.fasterxml.jackson.databind.ObjectMapper;
import dbp.exploreconnet.comment.dto.CommentRequestDto;
import dbp.exploreconnet.comment.dto.CommentUpdateDto;
import dbp.exploreconnet.email.domain.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CommentControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String userAuthToken;
    private Long postId;
    private Long userId;

    @MockBean
    private EmailService emailService;

    @BeforeEach
    void setup() throws Exception {
        doNothing().when(emailService).correoSingIn(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
        String userEmail = "owner" + System.currentTimeMillis() + "@example.com";
        String signUpPayloadUser = "{\"email\":\"" + userEmail + "\", \"name\":\"Test Owner\", \"password\":\"password123\", \"role\":\"OWNER\"}";

        // Realizar el registro (signin)
        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpPayloadUser))
                .andExpect(status().isOk());

        // Login para obtener el token de autenticación
        String loginPayloadUser = "{\"email\":\"" + userEmail + "\", \"password\":\"password123\"}";
        MvcResult userLoginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayloadUser))
                .andExpect(status().isOk())
                .andReturn();

        // Extraer el token de autenticación de la respuesta
        userAuthToken = JsonPath.read(userLoginResult.getResponse().getContentAsString(), "$.token");

        // Obtener el id del usuario mediante el endpoint /users/me
        MvcResult userMeResult = mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + userAuthToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        userId = ((Number) JsonPath.read(userMeResult.getResponse().getContentAsString(), "$.id")).longValue();

        // Crear un lugar (Place) necesario para el Post
        MockMultipartFile imageFile = new MockMultipartFile("image", "place-image.jpg", "image/jpeg", "place image content".getBytes());
        MvcResult placeResult = mockMvc.perform(multipart("/places")
                        .file(imageFile)
                        .param("name", "New Test Place")
                        .param("address", "456 Test Ave")
                        .param("description", "Another test place")
                        .param("category", "CAFETERIA")
                        .param("openingHours", "10AM - 6PM")
                        .param("coordinate.latitude", "34.0522")
                        .param("coordinate.longitude", "-118.2437")
                        .param("ownerEmail", userEmail)
                        .header("Authorization", "Bearer " + userAuthToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn();

        Long placeId = ((Number) JsonPath.read(placeResult.getResponse().getContentAsString(), "$.id")).longValue();

        // Crear un post usando el userId y placeId obtenidos
        MockMultipartFile postImageFile = new MockMultipartFile("image", "test-image.jpg", "image/jpeg", "test image content".getBytes());
        mockMvc.perform(multipart("/post")
                        .file(postImageFile)
                        .param("userId", userId.toString())
                        .param("placeId", placeId.toString())
                        .param("description", "Post for comment testing")
                        .header("Authorization", "Bearer " + userAuthToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated());

        // Llamar al nuevo método GET /post/me para obtener el postId del post creado
        MvcResult postListResult = mockMvc.perform(get("/post/me")
                        .param("page", "0")
                        .param("size", "1")
                        .header("Authorization", "Bearer " + userAuthToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        postId = ((Number) JsonPath.read(postListResult.getResponse().getContentAsString(), "$.content[0].id")).longValue();
    }

    @Test
    void createComment() throws Exception {
        CommentRequestDto commentRequest = new CommentRequestDto();
        commentRequest.setContent("This is a test comment");
        commentRequest.setUserId(userId);
        commentRequest.setPostId(postId);

        String commentRequestJson = objectMapper.writeValueAsString(commentRequest);

        mockMvc.perform(post("/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentRequestJson)
                        .header("Authorization", "Bearer " + userAuthToken))
                .andExpect(status().isOk());
    }

    @Test
    void updateComment() throws Exception {
        // Crear comentario
        CommentRequestDto commentRequest = new CommentRequestDto();
        commentRequest.setContent("Original comment content");
        commentRequest.setUserId(userId);
        commentRequest.setPostId(postId);

        String commentRequestJson = objectMapper.writeValueAsString(commentRequest);

        // Crear comentario con POST sin esperar un ID en la respuesta
        mockMvc.perform(post("/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentRequestJson)
                        .header("Authorization", "Bearer " + userAuthToken))
                .andExpect(status().isOk());

        // Obtener el comentario recién creado con GET para capturar su ID
        MvcResult getResult = mockMvc.perform(get("/comments/post/{postId}", postId)
                        .param("page", "0")
                        .param("size", "1")
                        .header("Authorization", "Bearer " + userAuthToken))
                .andExpect(status().isOk())
                .andReturn();

        // Extraer el ID del comentario del JSON de respuesta
        Long commentId = JsonPath.parse(getResult.getResponse().getContentAsString()).read("$.content[0].id", Long.class);

        // Crear payload para la actualización del comentario
        CommentUpdateDto commentUpdate = new CommentUpdateDto();
        commentUpdate.setContent("Updated comment content");
        commentUpdate.setUserId(userId);

        String commentUpdateJson = objectMapper.writeValueAsString(commentUpdate);

        // Llamar al método PUT para actualizar el comentario
        mockMvc.perform(put("/comments/" + commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentUpdateJson)
                        .header("Authorization", "Bearer " + userAuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Updated comment content"))
                .andExpect(jsonPath("$.id").value(commentId))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.postId").value(postId));
    }





    @Test
    public void likeComment() throws Exception {
        String commentContent = "Test comment for like feature";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("content", commentContent);
        requestBody.put("userId", userId);
        requestBody.put("postId", postId);

        mockMvc.perform(post("/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody))
                        .header("Authorization", "Bearer " + userAuthToken))
                .andExpect(status().isOk());

        MvcResult commentListResult = mockMvc.perform(get("/comments/post/{postId}", postId)
                        .param("page", "0")
                        .param("size", "1")
                        .header("Authorization", "Bearer " + userAuthToken))
                .andExpect(status().isOk())
                .andReturn();

        Long commentId = JsonPath.parse(commentListResult.getResponse().getContentAsString()).read("$.content[0].id", Long.class);

        mockMvc.perform(patch("/comments/like/{id}", commentId)
                        .header("Authorization", "Bearer " + userAuthToken))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteComment() throws Exception {
        // Crear el comentario
        String commentContent = "Test comment for deletion feature";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("content", commentContent);
        requestBody.put("userId", userId);
        requestBody.put("postId", postId);

        mockMvc.perform(post("/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody))
                        .header("Authorization", "Bearer " + userAuthToken))
                .andExpect(status().isOk());

        // Obtener el comentario recién creado para capturar el `commentId`
        MvcResult getResult = mockMvc.perform(get("/comments/post/{postId}", postId)
                        .param("page", "0")
                        .param("size", "1")
                        .header("Authorization", "Bearer " + userAuthToken))
                .andExpect(status().isOk())
                .andReturn();

        Long commentId = JsonPath.parse(getResult.getResponse().getContentAsString()).read("$.content[0].id", Long.class);

        // Llamar al método DELETE para eliminar el comentario
        mockMvc.perform(delete("/comments/{commentId}", commentId)
                        .header("Authorization", "Bearer " + userAuthToken))
                .andExpect(status().isNoContent());
    }


    @Test
    public void getCommentsByPostId() throws Exception {
        String commentContent = "Test comment for retrieval by post ID";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("content", commentContent);
        requestBody.put("userId", userId);
        requestBody.put("postId", postId);

        mockMvc.perform(post("/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody))
                        .header("Authorization", "Bearer " + userAuthToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/comments/post/{postId}", postId)
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + userAuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].content").value(commentContent))
                .andExpect(jsonPath("$.content[0].postId").value(postId))
                .andExpect(jsonPath("$.content[0].userId").value(userId))
                .andExpect(jsonPath("$.content[0].name").value("Test Owner"));
    }
}







