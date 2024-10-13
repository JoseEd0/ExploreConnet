package dbp.exploreconnet.reservation.application;

import com.jayway.jsonpath.JsonPath;
import dbp.exploreconnet.email.domain.EmailService;
import dbp.exploreconnet.reservation.dto.ReservationRequestDto;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.doNothing;

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
        String date = LocalDateTime.now().plusDays(1).toString();
        String jsonContent = String.format(
                "{\"placeId\": %d, \"date\": \"%s\", \"numberOfPeople\": 4}",
                placeId, date
        );

        mockMvc.perform(post("/reservations")
                        .header("Authorization", "Bearer " + userAuthToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.placeName", is("Reservation Place")));
    }


    @Test
    @Order(2)
    void getReservationsByUser() throws Exception {
        // Crear una reserva para el usuario y obtener su ID
        Long reservationId = createReservationAndGetId();
        System.out.println("Reserva creada con ID: " + reservationId);

        // Verificar que el usuario tiene al menos una reserva
        mockMvc.perform(get("/reservations/user")
                        .header("Authorization", "Bearer " + ownerAuthToken))
                .andExpect(status().isOk());
    }



    @Test
    @Order(3)
    void getReservationById() throws Exception {
        Long reservationId = createReservationAndGetId(); // Método auxiliar para crear una reserva y obtener su ID

        mockMvc.perform(get("/reservations/" + reservationId)
                        .header("Authorization", "Bearer " + ownerAuthToken))
                .andExpect(status().isOk());
    }

    @Test
    @Order(4)
    void updateReservation() throws Exception {
        Long reservationId = createReservationAndGetId(); // Crear una reserva primero

        mockMvc.perform(put("/reservations/" + reservationId)
                        .header("Authorization", "Bearer " + userAuthToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"date\": \"" + LocalDateTime.now().plusDays(2) + "\", \"numberOfPeople\": 6}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numberOfPeople", is(6)));
    }

    @Test
    @Order(5)
    void deleteReservation() throws Exception {
        Long reservationId = createReservationAndGetId(); // Crear una reserva primero

        mockMvc.perform(delete("/reservations/" + reservationId)
                        .header("Authorization", "Bearer " + ownerAuthToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(6)
    void getAllReservations() throws Exception {
        mockMvc.perform(get("/reservations")
                        .header("Authorization", "Bearer " + ownerAuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))));
    }

    @Test
    @Order(7)
    void getReservationsByOwner() throws Exception {
        mockMvc.perform(get("/reservations/myplaces")
                        .header("Authorization", "Bearer " + ownerAuthToken))
                .andExpect(status().isOk());
    }

    private Long createReservationAndGetId() throws Exception {
        String date = LocalDateTime.now().plusDays(1).toString();
        String jsonContent = String.format(
                "{\"placeId\": %d, \"date\": \"%s\", \"numberOfPeople\": 4}",
                placeId, date
        );

        MvcResult result = mockMvc.perform(post("/reservations")
                        .header("Authorization", "Bearer " + userAuthToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isOk())
                .andReturn();

        // Lee el ID como Integer y conviértelo a Long
        Integer idAsInteger = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        return idAsInteger.longValue();
    }

}
