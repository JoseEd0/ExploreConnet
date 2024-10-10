package dbp.exploreconnet.review.application;

import com.jayway.jsonpath.JsonPath;
import dbp.exploreconnet.email.domain.EmailService;
import dbp.exploreconnet.review.dto.NewReviewDto;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReviewControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmailService emailService;

    private String userAuthToken;
    private String ownerAuthToken;
    private Long placeId;

    @BeforeEach
    void setup() throws Exception {
        // Configuración del mock para que el método de envío de correo no haga nada
        doNothing().when(emailService).correoSingIn(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());

        // Crear y autenticar el usuario USER
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

        // Crear y autenticar el usuario OWNER
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

        // Crear un lugar con el token del OWNER
        MockMultipartFile imageFile = new MockMultipartFile("image", "test-image.jpg", "image/jpeg", "test image content".getBytes());
        MvcResult placeResult = mockMvc.perform(multipart("/places")
                        .file(imageFile)
                        .param("name", "Test Place")
                        .param("address", "123 Test St")
                        .param("description", "Place for review testing")
                        .param("category", "RECREATIONAL")
                        .param("openingHours", "9AM - 10PM")
                        .param("coordinate.latitude", "40.7128")
                        .param("coordinate.longitude", "-74.0060")
                        .header("Authorization", "Bearer " + ownerAuthToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn();

        placeId = ((Number) JsonPath.read(placeResult.getResponse().getContentAsString(), "$.id")).longValue();
    }

    @Test
    @Order(1)
    void createNewReview() throws Exception {
        NewReviewDto newReview = new NewReviewDto();
        newReview.setComment("Great place to visit!");
        newReview.setRating(5);
        newReview.setPlaceId(placeId);

        String newReviewJson = String.format(
                "{\"comment\":\"%s\", \"rating\":%d, \"placeId\":%d}",
                newReview.getComment(), newReview.getRating(), newReview.getPlaceId()
        );

        mockMvc.perform(post("/review/new")
                        .header("Authorization", "Bearer " + userAuthToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newReviewJson))
                .andExpect(status().isOk())
                .andExpect(content().string("Review created"));
    }

    @Test
    @Order(2)
    void deleteReview() throws Exception {
        // Crear un review
        String newReviewJson = String.format(
                "{\"comment\":\"This review will be deleted\", \"rating\":4, \"placeId\":%d}", placeId);

        mockMvc.perform(post("/review/new")
                        .header("Authorization", "Bearer " + userAuthToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newReviewJson))
                .andExpect(status().isOk())
                .andExpect(content().string("Review created"));

        // Obtener el ID del review creado
        MvcResult getResult = mockMvc.perform(get("/review/myreviews")
                        .header("Authorization", "Bearer " + userAuthToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Extraer el primer ID de la lista devuelta en `myreviews`
        Integer reviewId = JsonPath.read(getResult.getResponse().getContentAsString(), "$[0].id");

        // Eliminar el review usando el ID extraído
        mockMvc.perform(delete("/review/" + reviewId)
                        .header("Authorization", "Bearer " + userAuthToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Review deleted"));

        // Verificar que el review ya no existe
        mockMvc.perform(get("/review/" + reviewId)
                        .header("Authorization", "Bearer " + userAuthToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(3)
    void getReviewById() throws Exception {
        // Crear un review
        String newReviewJson = String.format(
                "{\"comment\":\"Amazing place!\", \"rating\":4, \"placeId\":%d}", placeId);

        MvcResult createReviewResult = mockMvc.perform(post("/review/new")
                        .header("Authorization", "Bearer " + userAuthToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newReviewJson))
                .andExpect(status().isOk())
                .andReturn();

        // Obtener el ID del review creado a partir de la respuesta de "myreviews"
        MvcResult getResult = mockMvc.perform(get("/review/myreviews")
                        .header("Authorization", "Bearer " + userAuthToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        Integer reviewId = JsonPath.read(getResult.getResponse().getContentAsString(), "$[0].id");

        // Usar el ID para llamar al endpoint getReviewById
        mockMvc.perform(get("/review/" + reviewId)
                        .header("Authorization", "Bearer " + userAuthToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reviewId))
                .andExpect(jsonPath("$.comment").value("Amazing place!"))
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.placeId").value(placeId))
                .andExpect(jsonPath("$.placeName").value("Test Place"));
    }

    @Test
    @Order(4)
    void getMyReviews() throws Exception {
        String newReviewJson = String.format(
                "{\"comment\":\"Another great experience!\", \"rating\":5, \"placeId\":%d}", placeId);

        mockMvc.perform(post("/review/new")
                        .header("Authorization", "Bearer " + userAuthToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newReviewJson))
                .andExpect(status().isOk());

        mockMvc.perform(get("/review/myreviews")
                        .header("Authorization", "Bearer " + userAuthToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].comment").value("Another great experience!"))
                .andExpect(jsonPath("$[0].rating").value(5))
                .andExpect(jsonPath("$[0].placeId").value(placeId))
                .andExpect(jsonPath("$[0].placeName").value("Test Place"));
    }

    @Test
    @Order(5)
    void getAllReviews() throws Exception {
        String newReviewJson = String.format(
                "{\"comment\":\"An amazing experience for everyone.\", \"rating\":5, \"placeId\":%d}", placeId);

        mockMvc.perform(post("/review/new")
                        .header("Authorization", "Bearer " + ownerAuthToken) // Owner crea la review
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newReviewJson))
                .andExpect(status().isOk());

        mockMvc.perform(get("/review/all")
                        .header("Authorization", "Bearer " + userAuthToken) // User accede a todas las reviews
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.comment == 'An amazing experience for everyone.')]").exists())
                .andExpect(jsonPath("$[?(@.rating == 5)]").exists())
                .andExpect(jsonPath("$[?(@.placeId == " + placeId + ")]").exists())
                .andExpect(jsonPath("$[?(@.placeName == 'Test Place')]").exists());
    }



    @Test
    @Order(6)
    void getReviewsByMyPlace() throws Exception {
        // Crear una reseña para el lugar del OWNER usando el usuario USER
        String reviewPayload = String.format(
                "{\"comment\":\"Great place to visit!\", \"rating\":5, \"placeId\":%d}", placeId);

        mockMvc.perform(post("/review/new")
                        .header("Authorization", "Bearer " + userAuthToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reviewPayload))
                .andExpect(status().isOk());

        // Obtener reseñas del lugar del OWNER usando el token de OWNER
        mockMvc.perform(get("/review/myplaces")
                        .header("Authorization", "Bearer " + ownerAuthToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))  // Confirmar que hay una reseña
                .andExpect(jsonPath("$[0].comment").value("Great place to visit!"))
                .andExpect(jsonPath("$[0].rating").value(5))
                .andExpect(jsonPath("$[0].placeId").value(placeId))
                .andExpect(jsonPath("$[0].placeName").value("Test Place"));
    }
}




