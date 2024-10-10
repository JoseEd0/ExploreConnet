package dbp.exploreconnet.user.application;

import com.jayway.jsonpath.JsonPath;
import dbp.exploreconnet.email.domain.EmailService;
import dbp.exploreconnet.user.dto.UserProfilePhotoUpdateDto;
import dbp.exploreconnet.user.dto.UserRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    EmailService emailService;

    private String authToken;
    private Long userId;
    private String fixedUserAuthToken;
    private Long fixedUserId;


    @BeforeEach
    void setup() throws Exception {
        doNothing().when(emailService).correoSingIn(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());

        // Intento de registro de un usuario principal con un email fijo
        String signUpPayload = "{\"email\":\"testuser@example.com\", \"name\":\"Test User\", \"password\":\"password123\", \"role\":\"USER\"}";

        MvcResult signupResult = mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpPayload))
                .andReturn();

        // Si el usuario principal ya existe, realiza login
        String loginPayload = "{\"email\":\"testuser@example.com\", \"password\":\"password123\"}";
        String loginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        authToken = JsonPath.read(loginResponse, "$.token");

        // Obtener el ID del usuario principal después de login para usarlo en otras pruebas
        MvcResult userMeResult = mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        Long userId = ((Number) JsonPath.read(userMeResult.getResponse().getContentAsString(), "$.id")).longValue();

        // **Nuevo usuario específico para getUserById con email fijo**
        String newUserSignUpPayload = "{\"email\":\"fixeduser@example.com\", \"name\":\"Fixed User\", \"password\":\"password123\", \"role\":\"USER\"}";

        MvcResult newUserSignupResult = mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newUserSignUpPayload))
                .andReturn();

        // Login del nuevo usuario para obtener el token y el ID
        String newUserLoginPayload = "{\"email\":\"fixeduser@example.com\", \"password\":\"password123\"}";
        String newUserLoginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newUserLoginPayload))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String newUserAuthToken = JsonPath.read(newUserLoginResponse, "$.token");

        // Obtener el ID del nuevo usuario para el test específico
        MvcResult newUserMeResult = mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + newUserAuthToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        Long newUserId = ((Number) JsonPath.read(newUserMeResult.getResponse().getContentAsString(), "$.id")).longValue();

        // Guardar el token e ID del nuevo usuario para las pruebas
        this.fixedUserAuthToken = newUserAuthToken;
        this.fixedUserId = newUserId;
    }



    @Test
    void getUserById() throws Exception {
        mockMvc.perform(get("/users/" + fixedUserId) // Usa fixedUserId
                        .header("Authorization", "Bearer " + fixedUserAuthToken) // Usa fixedUserAuthToken
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("fixeduser@example.com"))
                .andExpect(jsonPath("$.fullName").value("Fixed User"));
    }



    @Test
    void getMe() throws Exception {
        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void updateUser() throws Exception {
        UserRequestDto userUpdateDto = new UserRequestDto();
        userUpdateDto.setFullName("Updated User");
        userUpdateDto.setEmail("updateduser@example.com");
        userUpdateDto.setPassword("newpassword123");
        userUpdateDto.setRole("USER");

        String userUpdateJson = objectMapper.writeValueAsString(userUpdateDto);

        mockMvc.perform(put("/users/1")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userUpdateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Updated User"))
                .andExpect(jsonPath("$.email").value("updateduser@example.com"));
    }

    @Test
    void updateProfilePhoto() throws Exception {
        MockMultipartFile profilePhoto = new MockMultipartFile(
                "profilePhoto",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        mockMvc.perform(multipart("/users/1/profile-photo")
                        .file(profilePhoto)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(request -> {
                            request.setMethod("PUT"); // Cambia el método a PUT
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profileImage").exists()); // Cambiado de `profileImageUrl` a `profileImage`
    }

    @Test
    void getAllUsers() throws Exception {
        // Paso 1: Registro y login del usuario con rol OWNER
        String signUpPayload = "{\"email\":\"owneruser@example.com\", \"name\":\"Owner User\", \"password\":\"password123\", \"role\":\"OWNER\"}";

        MvcResult signupResult = mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpPayload))
                .andReturn();

        String ownerAuthToken;
        if (signupResult.getResponse().getStatus() == 409) {
            String loginPayload = "{\"email\":\"owneruser@example.com\", \"password\":\"password123\"}";
            String loginResponse = mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(loginPayload))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            ownerAuthToken = JsonPath.read(loginResponse, "$.token");
        } else {
            String loginPayload = "{\"email\":\"owneruser@example.com\", \"password\":\"password123\"}";
            String loginResponse = mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(loginPayload))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            ownerAuthToken = JsonPath.read(loginResponse, "$.token");
        }

        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + ownerAuthToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray()); // Verifica que la respuesta sea una lista (array)
    }

    @Test
    void getCurrentUser() throws Exception {
        String signUpPayload = "{\"email\":\"currentuser@example.com\", \"name\":\"Current User\", \"password\":\"password123\", \"role\":\"USER\"}";

        MvcResult signupResult = mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpPayload))
                .andReturn();

        String currentUserAuthToken;
        if (signupResult.getResponse().getStatus() == 409) {
            String loginPayload = "{\"email\":\"currentuser@example.com\", \"password\":\"password123\"}";
            String loginResponse = mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(loginPayload))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            currentUserAuthToken = JsonPath.read(loginResponse, "$.token");
        } else {
            String loginPayload = "{\"email\":\"currentuser@example.com\", \"password\":\"password123\"}";
            String loginResponse = mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(loginPayload))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            currentUserAuthToken = JsonPath.read(loginResponse, "$.token");
        }

        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + currentUserAuthToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("currentuser@example.com"))
                .andExpect(jsonPath("$.fullName").value("Current User"))
                .andExpect(jsonPath("$.role").value("USER")); // Verifica que el rol y los datos sean correctos
    }

    @Test
    void updateCurrentUser() throws Exception {
        String signUpPayload = "{\"email\":\"edituser@example.com\", \"name\":\"Edit User\", \"password\":\"password123\", \"role\":\"USER\"}";

        MvcResult signupResult = mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpPayload))
                .andReturn();

        String editUserAuthToken;
        if (signupResult.getResponse().getStatus() == 409) {
            String loginPayload = "{\"email\":\"edituser@example.com\", \"password\":\"password123\"}";
            String loginResponse = mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(loginPayload))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            editUserAuthToken = JsonPath.read(loginResponse, "$.token");
        } else {
            String loginPayload = "{\"email\":\"edituser@example.com\", \"password\":\"password123\"}";
            String loginResponse = mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(loginPayload))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            editUserAuthToken = JsonPath.read(loginResponse, "$.token");
        }

        String updatePayload = "{\"fullName\":\"Updated User\", \"password\":\"newpassword456\", \"role\":\"USER\"}";

        mockMvc.perform(put("/users/edit/me")
                        .header("Authorization", "Bearer " + editUserAuthToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Updated User"))
                .andExpect(jsonPath("$.email").value("edituser@example.com"))
                .andExpect(jsonPath("$.role").value("USER")); // Verifica que los datos se actualizaron correctamente
    }



    @Test
    @Order(8)
    void deleteUser() throws Exception {
        // Paso 1: Registro o login de un usuario con rol USER
        String signUpPayload = "{\"email\":\"deleteuser@example.com\", \"name\":\"Delete User\", \"password\":\"password123\", \"role\":\"USER\"}";

        MvcResult signupResult = mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpPayload))
                .andReturn();

        String deleteUserAuthToken;
        Long userIdToDelete;

        if (signupResult.getResponse().getStatus() == 409) {
            // Si el usuario ya existe, solo hacemos login
            String loginPayload = "{\"email\":\"deleteuser@example.com\", \"password\":\"password123\"}";
            String loginResponse = mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(loginPayload))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            deleteUserAuthToken = JsonPath.read(loginResponse, "$.token");

            MvcResult userResult = mockMvc.perform(get("/users/me")
                            .header("Authorization", "Bearer " + deleteUserAuthToken))
                    .andExpect(status().isOk())
                    .andReturn();
            userIdToDelete = ((Number) JsonPath.read(userResult.getResponse().getContentAsString(), "$.id")).longValue();
        } else {
            String loginPayload = "{\"email\":\"deleteuser@example.com\", \"password\":\"password123\"}";
            String loginResponse = mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(loginPayload))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            deleteUserAuthToken = JsonPath.read(loginResponse, "$.token");

            MvcResult userResult = mockMvc.perform(get("/users/me")
                            .header("Authorization", "Bearer " + deleteUserAuthToken))
                    .andExpect(status().isOk())
                    .andReturn();
            userIdToDelete = ((Number) JsonPath.read(userResult.getResponse().getContentAsString(), "$.id")).longValue();
        }

        mockMvc.perform(delete("/users/" + userIdToDelete)
                        .header("Authorization", "Bearer " + deleteUserAuthToken))
                .andExpect(status().isNoContent()); // Verificamos que la respuesta sea 204 No Content
    }









}
