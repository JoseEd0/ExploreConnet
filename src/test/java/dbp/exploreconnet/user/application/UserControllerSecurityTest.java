package dbp.exploreconnet.user.application;

import com.jayway.jsonpath.JsonPath;
import dbp.exploreconnet.email.domain.EmailService;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.mockito.Mockito.*;
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
    private PasswordEncoder passwordEncoder;

    @MockBean
    EmailService emailService;

    private String authToken;
    private Long userId;
    private String fixedUserAuthToken;
    private Long fixedUserId;
    private String ownerToken;  // Nueva variable para el token del usuario OWNER

    @BeforeEach
    void setup() throws Exception {
        // Simular el comportamiento del PasswordEncoder
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        // Simulación del EmailService
        doNothing().when(emailService).correoSingIn(anyString(), anyString());

        // Registro y autenticación de usuario principal
        String signUpPayload = "{\"email\":\"testuser@example.com\", \"name\":\"Test User\", \"password\":\"password123\", \"role\":\"USER\"}";
        mockMvc.perform(post("/auth/signin").contentType(MediaType.APPLICATION_JSON).content(signUpPayload)).andReturn();

        String loginPayload = "{\"email\":\"testuser@example.com\", \"password\":\"password123\"}";
        String loginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        authToken = JsonPath.read(loginResponse, "$.token");

        MvcResult userMeResult = mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        userId = ((Number) JsonPath.read(userMeResult.getResponse().getContentAsString(), "$.id")).longValue();

        // Registro y autenticación de un segundo usuario
        String newUserSignUpPayload = "{\"email\":\"fixeduser@example.com\", \"name\":\"Fixed User\", \"password\":\"password123\", \"role\":\"USER\"}";
        mockMvc.perform(post("/auth/signin").contentType(MediaType.APPLICATION_JSON).content(newUserSignUpPayload)).andReturn();

        String newUserLoginPayload = "{\"email\":\"fixeduser@example.com\", \"password\":\"password123\"}";
        String newUserLoginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newUserLoginPayload))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        fixedUserAuthToken = JsonPath.read(newUserLoginResponse, "$.token");

        MvcResult newUserMeResult = mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + fixedUserAuthToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        fixedUserId = ((Number) JsonPath.read(newUserMeResult.getResponse().getContentAsString(), "$.id")).longValue();

        // Generación de un correo único para el usuario OWNER
        String uniqueOwnerEmail = "owneruser+" + UUID.randomUUID() + "@example.com";
        String ownerSignUpPayload = "{\"email\":\"" + uniqueOwnerEmail + "\", \"name\":\"Owner User\", \"password\":\"password123\", \"role\":\"OWNER\"}";

        // Registro y autenticación del usuario OWNER
        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ownerSignUpPayload))
                .andExpect(status().isOk());

        String ownerLoginPayload = "{\"email\":\"" + uniqueOwnerEmail + "\", \"password\":\"password123\"}";
        String ownerLoginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ownerLoginPayload))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        ownerToken = JsonPath.read(ownerLoginResponse, "$.token");
    }

    // Test 1: Obtener usuario por ID
    @Test
    @Order(1)
    void getUserById() throws Exception {
        mockMvc.perform(get("/users/" + fixedUserId)
                        .header("Authorization", "Bearer " + fixedUserAuthToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("fixeduser@example.com"))
                .andExpect(jsonPath("$.fullName").value("Fixed User"));
    }

    // Test 2: Obtener el usuario actual
    @Test
    @Order(2)
    void getMe() throws Exception {
        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // Test 3: Actualizar usuario
    @Test
    @Order(3)
    void updateUser() throws Exception {
        UserRequestDto userUpdateDto = new UserRequestDto();
        userUpdateDto.setFullName("Updated User");
        userUpdateDto.setEmail("updateduser" + System.currentTimeMillis() + "@example.com"); // Email único para cada ejecución
        userUpdateDto.setPassword("newpassword123");
        userUpdateDto.setRole("USER");

        String userUpdateJson = objectMapper.writeValueAsString(userUpdateDto);

        mockMvc.perform(put("/users/" + userId)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userUpdateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Updated User"))
                .andExpect(jsonPath("$.email").value(userUpdateDto.getEmail()));
    }

    // Test 4: Actualizar la foto de perfil
    @Test
    @Order(4)
    void updateProfilePhoto() throws Exception {
        MockMultipartFile profilePhoto = new MockMultipartFile("profilePhoto", "test-image.jpg", MediaType.IMAGE_JPEG_VALUE, "test image content".getBytes());

        mockMvc.perform(multipart("/users/" + userId + "/profile-photo")
                        .file(profilePhoto)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(request -> { request.setMethod("PUT"); return request; }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profileImage").exists());
    }

    // Test 5: Obtener todos los usuarios
    @Test
    @Order(5)
    void getAllUsers() throws Exception {
        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // Test 6: Actualizar el usuario actual
    @Test
    @Order(6)
    void updateCurrentUser() throws Exception {
        UserRequestDto updateUserRequest = new UserRequestDto();
        updateUserRequest.setFullName("Updated User Name");
        updateUserRequest.setPassword("newPassword123");
        updateUserRequest.setRole("USER");

        mockMvc.perform(put("/users/edit/me")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserRequest)))
                .andExpect(status().isOk());
    }

    // Test 7: Eliminar usuario
    @Test
    @Order(7)
    void deleteUser() throws Exception {
        mockMvc.perform(delete("/users/" + fixedUserId)
                        .header("Authorization", "Bearer " + fixedUserAuthToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    // Test 8: Obtener todos los usuarios después de la eliminación
    @Test
    @Order(8)
    void getAllUsersAfterDeletion() throws Exception {
        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}

