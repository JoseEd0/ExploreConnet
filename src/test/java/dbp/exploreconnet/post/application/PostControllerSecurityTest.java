package dbp.exploreconnet.post.application;

import org.springframework.test.annotation.DirtiesContext;
import com.jayway.jsonpath.JsonPath;
import com.fasterxml.jackson.databind.ObjectMapper;
import dbp.exploreconnet.email.domain.EmailService;
import dbp.exploreconnet.post.dto.PostUpdateContentDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class PostControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminAuthToken;
    private String ownerAuthToken;
    private String userAuthToken;
    private Long placeId;
    private Long userId;

    @MockBean
    private EmailService emailService;

    @BeforeEach
    void setup() throws Exception {
        // Mock del servicio de correo
        doNothing().when(emailService).correoSingIn(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());

        // Registrar y autenticar un usuario ADMIN
        String adminEmail = "admin" + System.currentTimeMillis() + "@example.com";
        String signUpPayloadAdmin = "{\"email\":\"" + adminEmail + "\", \"name\":\"Test Admin\", \"password\":\"password123\", \"role\":\"ADMIN\"}";
        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpPayloadAdmin))
                .andExpect(status().isOk());

        String loginPayloadAdmin = "{\"email\":\"" + adminEmail + "\", \"password\":\"password123\"}";
        MvcResult adminLoginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayloadAdmin))
                .andExpect(status().isOk())
                .andReturn();
        adminAuthToken = JsonPath.read(adminLoginResult.getResponse().getContentAsString(), "$.token");

        // Registrar y autenticar un usuario OWNER
        String ownerEmail = "owner" + System.currentTimeMillis() + "@example.com";
        String signUpPayloadOwner = "{\"email\":\"" + ownerEmail + "\", \"name\":\"Test Owner\", \"password\":\"password123\", \"role\":\"OWNER\"}";
        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpPayloadOwner))
                .andExpect(status().isOk());

        String loginPayloadOwner = "{\"email\":\"" + ownerEmail + "\", \"password\":\"password123\"}";
        MvcResult ownerLoginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayloadOwner))
                .andExpect(status().isOk())
                .andReturn();
        ownerAuthToken = JsonPath.read(ownerLoginResult.getResponse().getContentAsString(), "$.token");

        // Registrar y autenticar un usuario USER
        String userEmail = "user" + System.currentTimeMillis() + "@example.com";
        String signUpPayloadUser = "{\"email\":\"" + userEmail + "\", \"name\":\"Test User\", \"password\":\"password123\", \"role\":\"USER\"}";
        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpPayloadUser))
                .andExpect(status().isOk());

        String loginPayloadUser = "{\"email\":\"" + userEmail + "\", \"password\":\"password123\"}";
        MvcResult userLoginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayloadUser))
                .andExpect(status().isOk())
                .andReturn();
        userAuthToken = JsonPath.read(userLoginResult.getResponse().getContentAsString(), "$.token");

        // Obtener el ID del usuario USER
        MvcResult userMeResult = mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + userAuthToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        userId = ((Number) JsonPath.read(userMeResult.getResponse().getContentAsString(), "$.id")).longValue();

        // Crear un lugar (Place) necesario para el Post con el token de OWNER
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
                        .param("ownerEmail", ownerEmail)
                        .header("Authorization", "Bearer " + ownerAuthToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn();

        placeId = ((Number) JsonPath.read(placeResult.getResponse().getContentAsString(), "$.id")).longValue();
    }

    @Test
    void getPostsByPlaceId() throws Exception {
        // Intentar acceder a /post/place/{placeId} con rol ADMIN y recibir un 200 OK
        mockMvc.perform(get("/post/place/{placeId}", placeId)
                        .header("Authorization", "Bearer " + ownerAuthToken))
                .andExpect(status().isOk());

    }

    @Test
    void getPostById() throws Exception {
        // Paso 1: Crear un post para obtener su ID
        MockMultipartFile postImageFile = new MockMultipartFile("image", "test-image.jpg", "image/jpeg", "test image content".getBytes());
        MvcResult postCreationResult = mockMvc.perform(multipart("/post")
                        .file(postImageFile)
                        .param("userId", userId.toString())
                        .param("placeId", placeId.toString())
                        .param("description", "Test description for getPostById")
                        .header("Authorization", "Bearer " + userAuthToken))  // Usa el token de usuario/owner/admin según corresponda
                .andExpect(status().isCreated())
                .andReturn();

        // Extraer el ID del post recién creado desde la lista de posts del usuario actual
        MvcResult postListResult = mockMvc.perform(get("/post/me")
                        .param("page", "0")
                        .param("size", "1")
                        .header("Authorization", "Bearer " + userAuthToken))  // Usa el token de usuario/owner/admin según corresponda
                .andExpect(status().isOk())
                .andReturn();

        Long postId = ((Number) JsonPath.parse(postListResult.getResponse().getContentAsString()).read("$.content[0].id")).longValue();

        // Paso 2: Llamar al endpoint GET /post/{id} usando el postId
        mockMvc.perform(get("/post/{id}", postId)
                        .header("Authorization", "Bearer " + userAuthToken))  // Usa el token de usuario/owner/admin según corresponda
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(postId))
                .andExpect(jsonPath("$.description").value("Test description for getPostById"))
                .andExpect(jsonPath("$.ownerId").value(userId));
    }

    @Test
    void getAllPosts() throws Exception {
        // Paso 1: Crear varios posts para llenar la lista
        MockMultipartFile postImageFile = new MockMultipartFile("image", "test-image.jpg", "image/jpeg", "test image content".getBytes());

        for (int i = 0; i < 3; i++) {
            mockMvc.perform(multipart("/post")
                            .file(postImageFile)
                            .param("userId", userId.toString())
                            .param("placeId", placeId.toString())
                            .param("description", "Test post description " + i)
                            .header("Authorization", "Bearer " + userAuthToken)) // Usa el token de usuario
                    .andExpect(status().isCreated());
        }

        // Paso 2: Llamar al endpoint /post/all con parámetros de paginación
        mockMvc.perform(get("/post/all")
                        .param("page", "0")
                        .param("size", "2")
                        .header("Authorization", "Bearer " + userAuthToken))  // Usa el token de usuario
                .andExpect(status().isOk());
    }

    @Test
    void getPostsByCurrentUser() throws Exception {
        // Paso 1: Crear algunos posts asociados al usuario actual
        MockMultipartFile postImageFile = new MockMultipartFile("image", "test-image.jpg", "image/jpeg", "test image content".getBytes());

        for (int i = 0; i < 3; i++) {
            mockMvc.perform(multipart("/post")
                            .file(postImageFile)
                            .param("userId", userId.toString())
                            .param("placeId", placeId.toString())
                            .param("description", "User's post description " + i)
                            .header("Authorization", "Bearer " + userAuthToken)) // Usa el token del usuario
                    .andExpect(status().isCreated());
        }

        // Paso 2: Llamar al endpoint /post/me con parámetros de paginación
        mockMvc.perform(get("/post/me")
                        .param("page", "0")
                        .param("size", "2")
                        .header("Authorization", "Bearer " + userAuthToken))  // Usa el token del usuario
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))  // Verificar que trae 2 posts
                .andExpect(jsonPath("$.content[0].description").exists())  // Verificar que los posts tienen la descripción
                .andExpect(jsonPath("$.content[0].ownerId").value(userId))  // Verificar que el ownerId corresponde al usuario actual
                .andExpect(jsonPath("$.content[1].description").exists())
                .andExpect(jsonPath("$.totalElements").value(3))  // Total de posts del usuario debería ser 3
                .andExpect(jsonPath("$.totalPages").value(2));  // Con 3 posts y tamaño 2, debe haber 2 páginas
    }

    @Test
    void getPostsByUserId() throws Exception {
        // Paso 1: Crear varios posts asociados al usuario especificado (userId)
        MockMultipartFile postImageFile = new MockMultipartFile("image", "test-image.jpg", "image/jpeg", "test image content".getBytes());

        for (int i = 0; i < 3; i++) {
            mockMvc.perform(multipart("/post")
                            .file(postImageFile)
                            .param("userId", userId.toString())
                            .param("placeId", placeId.toString())
                            .param("description", "User's post description " + i)
                            .header("Authorization", "Bearer " + userAuthToken)) // Usa el token del usuario para crear los posts
                    .andExpect(status().isCreated());
        }

        // Paso 2: Llamar al endpoint /post/user/{userId} con parámetros de paginación
        mockMvc.perform(get("/post/user/{userId}", userId)
                        .param("page", "0")
                        .param("size", "2")
                        .header("Authorization", "Bearer " + userAuthToken))  // Usa el token del usuario actual
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))  // Verificar que trae 2 posts
                .andExpect(jsonPath("$.content[0].description").exists())  // Verificar que los posts tienen descripción
                .andExpect(jsonPath("$.content[0].ownerId").value(userId))  // Verificar que el ownerId corresponde al usuario especificado
                .andExpect(jsonPath("$.content[1].description").exists())
                .andExpect(jsonPath("$.totalElements").value(3))  // Total de posts del usuario debería ser 3
                .andExpect(jsonPath("$.totalPages").value(2));  // Con 3 posts y tamaño de página 2, debe haber 2 páginas
    }
    @Test
    void createPost() throws Exception {
        MockMultipartFile postImageFile = new MockMultipartFile("image", "test-image.jpg", "image/jpeg", "test image content".getBytes());

        mockMvc.perform(multipart("/post")
                        .file(postImageFile)
                        .param("userId", userId.toString())  // userId configurado en el setup
                        .param("placeId", placeId.toString())  // placeId configurado en el setup
                        .param("description", "Test description for new post")
                        .header("Authorization", "Bearer " + userAuthToken)  // Usa el token del usuario actual con rol USER o OWNER
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated());

        // Verificación adicional: Obtener el post recién creado para asegurar que los datos se guardaron correctamente
        MvcResult postListResult = mockMvc.perform(get("/post/me")
                        .param("page", "0")
                        .param("size", "1")
                        .header("Authorization", "Bearer " + userAuthToken))
                .andExpect(status().isOk())
                .andReturn();

        // Extraer y verificar el ID y la descripción del post
        Long postId = ((Number) JsonPath.parse(postListResult.getResponse().getContentAsString()).read("$.content[0].id")).longValue();
        mockMvc.perform(get("/post/{id}", postId)
                        .header("Authorization", "Bearer " + userAuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(postId))
                .andExpect(jsonPath("$.description").value("Test description for new post"))
                .andExpect(jsonPath("$.ownerId").value(userId))
                .andExpect(jsonPath("$.place.id").value(placeId))
                .andExpect(jsonPath("$.imageUrl").exists());
    }

    @Test
    void changeContent() throws Exception {
        // Paso 1: Crear un post inicial para obtener su ID
        MockMultipartFile postImageFile = new MockMultipartFile("image", "test-image.jpg", "image/jpeg", "test image content".getBytes());
        mockMvc.perform(multipart("/post")
                        .file(postImageFile)
                        .param("userId", userId.toString())
                        .param("placeId", placeId.toString())
                        .param("description", "Initial description")
                        .header("Authorization", "Bearer " + userAuthToken))
                .andExpect(status().isCreated());

        // Obtener el ID del post recién creado desde la lista de posts del usuario actual
        MvcResult postListResult = mockMvc.perform(get("/post/me")
                        .param("page", "0")
                        .param("size", "1")
                        .header("Authorization", "Bearer " + userAuthToken))
                .andExpect(status().isOk())
                .andReturn();

        Long postId = ((Number) JsonPath.parse(postListResult.getResponse().getContentAsString()).read("$.content[0].id")).longValue();

        // Paso 2: Crear un nuevo lugar (place) para asociarlo en la actualización del post
        MockMultipartFile newPlaceImageFile = new MockMultipartFile("image", "new-place-image.jpg", "image/jpeg", "new place image content".getBytes());
        MvcResult newPlaceResult = mockMvc.perform(multipart("/places")
                        .file(newPlaceImageFile)
                        .param("name", "New Place")
                        .param("address", "123 New Address")
                        .param("description", "New description for place")
                        .param("category", "RESTAURANT")
                        .param("openingHours", "9AM - 9PM")
                        .param("coordinate.latitude", "40.7128")
                        .param("coordinate.longitude", "-74.0060")
                        .param("ownerEmail", "owner@example.com")
                        .header("Authorization", "Bearer " + ownerAuthToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn();

        Long newPlaceId = ((Number) JsonPath.parse(newPlaceResult.getResponse().getContentAsString()).read("$.id")).longValue();

        // Paso 3: Preparar la solicitud de actualización del contenido (nuevo placeId)
        PostUpdateContentDto contentUpdate = new PostUpdateContentDto();
        contentUpdate.setPlaceId(newPlaceId);
        String contentUpdateJson = objectMapper.writeValueAsString(contentUpdate);

        // Paso 4: Enviar la solicitud PATCH para actualizar el placeId del post y esperar solo el status 204 No Content
        mockMvc.perform(patch("/post/content/{id}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contentUpdateJson)
                        .header("Authorization", "Bearer " + userAuthToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void likePost() throws Exception {
        // Paso 1: Crear un post para darle "like"
        MockMultipartFile postImageFile = new MockMultipartFile("image", "test-image.jpg", "image/jpeg", "test image content".getBytes());
        MvcResult postCreationResult = mockMvc.perform(multipart("/post")
                        .file(postImageFile)
                        .param("userId", userId.toString())
                        .param("placeId", placeId.toString())
                        .param("description", "Post for like testing")
                        .header("Authorization", "Bearer " + userAuthToken))
                .andExpect(status().isCreated())
                .andReturn();

        // Extraer el ID del post recién creado desde la lista de posts del usuario actual
        MvcResult postListResult = mockMvc.perform(get("/post/me")
                        .param("page", "0")
                        .param("size", "1")
                        .header("Authorization", "Bearer " + userAuthToken))
                .andExpect(status().isOk())
                .andReturn();

        Long postId = ((Number) JsonPath.parse(postListResult.getResponse().getContentAsString()).read("$.content[0].id")).longValue();

        // Paso 2: Enviar la solicitud PATCH para dar "like" al post y solo verificar el status 204 No Content
        mockMvc.perform(patch("/post/like/{id}", postId)
                        .header("Authorization", "Bearer " + userAuthToken))
                .andExpect(status().isNoContent());
    }
    @Test
    void changeMedia() throws Exception {
        // Paso 1: Crear un post para cambiar su media
        MockMultipartFile postImageFile = new MockMultipartFile("image", "test-image.jpg", "image/jpeg", "initial image content".getBytes());
        MvcResult postCreationResult = mockMvc.perform(multipart("/post")
                        .file(postImageFile)
                        .param("userId", userId.toString())
                        .param("placeId", placeId.toString())
                        .param("description", "Initial description")
                        .header("Authorization", "Bearer " + userAuthToken))
                .andExpect(status().isCreated())
                .andReturn();

        // Extraer el ID del post recién creado desde la lista de posts del usuario actual
        MvcResult postListResult = mockMvc.perform(get("/post/me")
                        .param("page", "0")
                        .param("size", "1")
                        .header("Authorization", "Bearer " + userAuthToken))
                .andExpect(status().isOk())
                .andReturn();

        Long postId = ((Number) JsonPath.parse(postListResult.getResponse().getContentAsString()).read("$.content[0].id")).longValue();

        // Paso 2: Crear archivos de imagen y video actualizados para la solicitud PATCH
        MockMultipartFile updatedImageFile = new MockMultipartFile("image", "updated-image.jpg", "image/jpeg", "updated image content".getBytes());
        MockMultipartFile updatedVideoFile = new MockMultipartFile("video", "updated-video.mp4", "video/mp4", "updated video content".getBytes());

        // Paso 3: Llamar al endpoint PATCH para cambiar la media del post
        mockMvc.perform(multipart("/post/media/{id}", postId)
                        .file(updatedImageFile)
                        .file(updatedVideoFile)
                        .param("description", "Updated description")
                        .header("Authorization", "Bearer " + userAuthToken)
                        .with(request -> {
                            request.setMethod("PATCH"); // Cambiar explícitamente el método a PATCH
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isNoContent());
    }
    @Test
    void getShareableUrl() throws Exception {
        // Paso 1: Crear un post con el token de USER
        MockMultipartFile postImageFile = new MockMultipartFile("image", "post-image.jpg", "image/jpeg", "post image content".getBytes());
        MvcResult postCreationResult = mockMvc.perform(multipart("/post")
                        .file(postImageFile)
                        .param("userId", userId.toString())
                        .param("placeId", placeId.toString())
                        .param("description", "Test post description")
                        .header("Authorization", "Bearer " + userAuthToken))
                .andExpect(status().isCreated())
                .andReturn();

        // Extraer el ID del post recién creado
        MvcResult postListResult = mockMvc.perform(get("/post/me")
                        .param("page", "0")
                        .param("size", "1")
                        .header("Authorization", "Bearer " + userAuthToken))
                .andExpect(status().isOk())
                .andReturn();
        Long postId = ((Number) JsonPath.parse(postListResult.getResponse().getContentAsString()).read("$.content[0].id")).longValue();

        // Paso 2: Solicitar la URL compartible del post creado
        mockMvc.perform(get("/post/share/{id}", postId)
                        .header("Authorization", "Bearer " + userAuthToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))  // Cambiado el expected para incluir charset=UTF-8
                .andExpect(content().string("https://exploreconnect.com/foro/post/" + postId))  // Verifica el contenido de la URL
                .andReturn();
    }

    @Test
    void deletePost() throws Exception {
        // Paso 1: Crear un post con el token de USER
        MockMultipartFile postImageFile = new MockMultipartFile("image", "post-image.jpg", "image/jpeg", "post image content".getBytes());
        MvcResult postCreationResult = mockMvc.perform(multipart("/post")
                        .file(postImageFile)
                        .param("userId", userId.toString())
                        .param("placeId", placeId.toString())
                        .param("description", "Test post description")
                        .header("Authorization", "Bearer " + userAuthToken))
                .andExpect(status().isCreated())
                .andReturn();

        // Extraer el ID del post recién creado para eliminarlo
        MvcResult postListResult = mockMvc.perform(get("/post/me")
                        .param("page", "0")
                        .param("size", "1")
                        .header("Authorization", "Bearer " + userAuthToken))
                .andExpect(status().isOk())
                .andReturn();
        Long postId = ((Number) JsonPath.parse(postListResult.getResponse().getContentAsString()).read("$.content[0].id")).longValue();

        // Paso 2: Eliminar el post con el ID obtenido
        mockMvc.perform(delete("/post/{id}", postId)
                        .header("Authorization", "Bearer " + userAuthToken))
                .andExpect(status().isNoContent())
                .andReturn();

    }




}
