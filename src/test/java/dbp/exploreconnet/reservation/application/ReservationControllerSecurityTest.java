package dbp.exploreconnet.reservation.application;

import com.jayway.jsonpath.JsonPath;
import dbp.exploreconnet.email.domain.EmailService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.hamcrest.Matchers.greaterThan;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
class ReservationControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmailService emailService;

    private String ownerAuthToken;
    private String userAuthToken;
    private Long placeId;

    @BeforeEach
    void setup() throws Exception {
        doNothing().when(emailService).correoSingIn(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());

        doNothing().when(emailService).sendReservationQRCode(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyString()
        );


        // Crear usuario OWNER único
        String uniqueOwnerEmail = "owner" + System.currentTimeMillis() + "@example.com";
        String signUpPayloadOwner = "{\"email\":\"" + uniqueOwnerEmail + "\", \"name\":\"Test Owner\", \"password\":\"password123\", \"role\":\"OWNER\"}";

        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpPayloadOwner))
                .andExpect(status().isOk());

        String loginPayloadOwner = "{\"email\":\"" + uniqueOwnerEmail + "\", \"password\":\"password123\"}";
        MvcResult ownerLoginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayloadOwner))
                .andExpect(status().isOk())
                .andReturn();

        ownerAuthToken = JsonPath.read(ownerLoginResult.getResponse().getContentAsString(), "$.token");

        // Crear usuario USER único
        String uniqueUserEmail = "user" + System.currentTimeMillis() + "@example.com";
        String signUpPayloadUser = "{\"email\":\"" + uniqueUserEmail + "\", \"name\":\"Reservation User\", \"password\":\"password123\", \"role\":\"USER\"}";

        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpPayloadUser))
                .andExpect(status().isOk());

        String loginPayloadUser = "{\"email\":\"" + uniqueUserEmail + "\", \"password\":\"password123\"}";
        MvcResult userLoginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayloadUser))
                .andExpect(status().isOk())
                .andReturn();

        userAuthToken = JsonPath.read(userLoginResult.getResponse().getContentAsString(), "$.token");

        // Crear lugar para las reservas
        MockMultipartFile imageFile = new MockMultipartFile("image", "test-image.jpg", "image/jpeg", "test image content".getBytes());
        MvcResult placeResult = mockMvc.perform(multipart("/places")
                        .file(imageFile)
                        .param("name", "Reservation Place")
                        .param("address", "456 Reservation St")
                        .param("description", "Place for reservation testing")
                        .param("category", "RECREATIONAL")
                        .param("openingHours", "9AM - 10PM")
                        .param("coordinate.latitude", "34.0522")
                        .param("coordinate.longitude", "-118.2437")
                        .header("Authorization", "Bearer " + ownerAuthToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn();

        placeId = ((Number) JsonPath.read(placeResult.getResponse().getContentAsString(), "$.id")).longValue();
    }

    @Test
    @Order(1)
    void createReservation() throws Exception {
        String reservationPayload = String.format(
                "{\"placeId\": %d, \"date\": \"2023-12-31T18:00:00\", \"numberOfPeople\": 5}", placeId);

        mockMvc.perform(post("/reservations")
                        .header("Authorization", "Bearer " + userAuthToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservationPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("Reservation User"))
                .andExpect(jsonPath("$.userEmail").exists())
                .andExpect(jsonPath("$.placeName").value("Reservation Place"))
                .andExpect(jsonPath("$.date").value("2023-12-31T18:00:00"))
                .andExpect(jsonPath("$.numberOfPeople").value(5));
    }

    @Test
    @Order(2)
    void getReservationsByUser() throws Exception {
        String reservationPayload = String.format(
                "{\"placeId\": %d, \"date\": \"2023-12-31T18:00:00\", \"numberOfPeople\": 5}", placeId);

        mockMvc.perform(post("/reservations")
                        .header("Authorization", "Bearer " + userAuthToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservationPayload))
                .andExpect(status().isOk());

        mockMvc.perform(get("/reservations/user")
                        .header("Authorization", "Bearer " + ownerAuthToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray()); // Verifica que sea un array, sin importar si está vacío
    }



    @Test
    @Order(3)
    void getReservationById() throws Exception {
        String reservationPayload = String.format(
                "{\"placeId\": %d, \"date\": \"2023-12-31T18:00:00\", \"numberOfPeople\": 5}", placeId);

        MvcResult reservationResult = mockMvc.perform(post("/reservations")
                        .header("Authorization", "Bearer " + userAuthToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservationPayload))
                .andExpect(status().isOk())
                .andReturn();

        Long reservationId = ((Number) JsonPath.read(reservationResult.getResponse().getContentAsString(), "$.id")).longValue();

        mockMvc.perform(get("/reservations/" + reservationId)
                        .header("Authorization", "Bearer " + ownerAuthToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").value(reservationId));
    }

    @Test
    @Order(4)
    void updateReservation() throws Exception {
        String initialReservationPayload = String.format(
                "{\"placeId\": %d, \"date\": \"2023-12-31T18:00:00\", \"numberOfPeople\": 5}", placeId);

        MvcResult createResult = mockMvc.perform(post("/reservations")
                        .header("Authorization", "Bearer " + userAuthToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(initialReservationPayload))
                .andExpect(status().isOk())
                .andReturn();

        Long reservationId = ((Number) JsonPath.read(createResult.getResponse().getContentAsString(), "$.id")).longValue();

        String updatedReservationPayload = """
        {
            "date": "2024-01-15T20:00:00",
            "numberOfPeople": 8
        }
        """;

        mockMvc.perform(put("/reservations/" + reservationId)
                        .header("Authorization", "Bearer " + ownerAuthToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedReservationPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reservationId))
                .andExpect(jsonPath("$.date").value("2024-01-15T20:00:00"))
                .andExpect(jsonPath("$.numberOfPeople").value(8));
    }

    @Test
    @Order(5)
    void getAllReservations() throws Exception {
        String reservationPayload = String.format(
                "{\"placeId\": %d, \"date\": \"2023-12-31T18:00:00\", \"numberOfPeople\": 5}", placeId);

        mockMvc.perform(post("/reservations")
                        .header("Authorization", "Bearer " + userAuthToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservationPayload))
                .andExpect(status().isOk());

        mockMvc.perform(get("/reservations")
                        .header("Authorization", "Bearer " + ownerAuthToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThan(0)))
                .andExpect(jsonPath("$[0].reservationId").exists());
    }

    @Test
    @Order(6)
    void getReservationsByOwner() throws Exception {
        String reservationPayload = String.format(
                "{\"placeId\": %d, \"date\": \"2023-12-31T18:00:00\", \"numberOfPeople\": 5}", placeId);

        mockMvc.perform(post("/reservations")
                        .header("Authorization", "Bearer " + userAuthToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservationPayload))
                .andExpect(status().isOk());

        mockMvc.perform(get("/reservations/myplaces")
                        .header("Authorization", "Bearer " + ownerAuthToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThan(0)))
                .andExpect(jsonPath("$[0].placeId").value(placeId));
    }

    @Test
    @Order(7)
    void deleteReservation() throws Exception {
        String reservationPayload = String.format(
                "{\"placeId\": %d, \"date\": \"2023-12-31T18:00:00\", \"numberOfPeople\": 5}", placeId);

        MvcResult reservationResult = mockMvc.perform(post("/reservations")
                        .header("Authorization", "Bearer " + userAuthToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservationPayload))
                .andExpect(status().isOk())
                .andReturn();

        Long reservationId = ((Number) JsonPath.read(reservationResult.getResponse().getContentAsString(), "$.id")).longValue();

        mockMvc.perform(delete("/reservations/" + reservationId)
                        .header("Authorization", "Bearer " + ownerAuthToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/reservations/" + reservationId)
                        .header("Authorization", "Bearer " + ownerAuthToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
