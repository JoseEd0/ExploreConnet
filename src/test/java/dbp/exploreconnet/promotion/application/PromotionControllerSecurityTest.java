package dbp.exploreconnet.promotion.application;


import dbp.exploreconnet.email.domain.EmailService;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import com.jayway.jsonpath.JsonPath;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
class PromotionControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    private String authToken;
    private Long placeId;

    @MockBean
    EmailService emailService;

    @BeforeEach
    void setup() throws Exception {

        doNothing().when(emailService).correoSingIn(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());

        String uniqueEmail = "owneruser" + System.currentTimeMillis() + "@example.com";

        String signUpPayload = "{\"email\":\"" + uniqueEmail + "\", \"name\":\"Owner User\", \"password\":\"password123\", \"role\":\"OWNER\"}";
        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpPayload))
                .andExpect(status().isOk());

        String loginPayload = "{\"email\":\"" + uniqueEmail + "\", \"password\":\"password123\"}";
        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andReturn();

        authToken = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.token");

        MockMultipartFile imageFile = new MockMultipartFile("image", "test-image.jpg", "image/jpeg", "test image content".getBytes());
        MvcResult placeResult = mockMvc.perform(multipart("/places")
                        .file(imageFile)
                        .param("name", "Promotion Test Place")
                        .param("address", "123 Promo St")
                        .param("description", "Place for promotion testing")
                        .param("category", "RECREATIONAL")
                        .param("openingHours", "10AM - 8PM")
                        .param("coordinate.latitude", "40.7128")
                        .param("coordinate.longitude", "-74.0060")
                        .param("ownerEmail", uniqueEmail)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn();

        placeId = ((Number) JsonPath.read(placeResult.getResponse().getContentAsString(), "$.id")).longValue();
    }

    @Test
    @Order(1)
    void createPromotion() throws Exception {
        // Crear una imagen de prueba para la promoción
        MockMultipartFile promotionImage = new MockMultipartFile("image", "promo-image.jpg", "image/jpeg", "promo image content".getBytes());

        mockMvc.perform(multipart("/promotions")
                        .file(promotionImage)
                        .param("placeId", placeId.toString())
                        .param("description", "20% off on all items")
                        .param("discount", "20")
                        // Se ajusta el formato de fecha a LocalDateTime compatible
                        .param("startDate", "2023-10-10T10:00:00")
                        .param("endDate", "2023-12-31T23:59:59")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("20% off on all items"))
                .andExpect(jsonPath("$.discount").value(20))
                .andExpect(jsonPath("$.placeName").value("Promotion Test Place"))
                .andExpect(jsonPath("$.imageUrl").isNotEmpty())
                .andExpect(jsonPath("$.startDate").value("2023-10-10T10:00:00"))
                .andExpect(jsonPath("$.endDate").value("2023-12-31T23:59:59"));
    }

    @Test
    @Order(2)
    void getPromotionById() throws Exception {
        // Primero, crea la promoción para asegurar que tenemos un ID válido
        MockMultipartFile promotionImage = new MockMultipartFile("image", "promo-image.jpg", "image/jpeg", "promo image content".getBytes());
        MvcResult promotionResult = mockMvc.perform(multipart("/promotions")
                        .file(promotionImage)
                        .param("placeId", placeId.toString())
                        .param("description", "20% off on all items")
                        .param("discount", "20")
                        .param("startDate", "2023-10-10T10:00:00")
                        .param("endDate", "2023-12-31T23:59:59")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn();

        Long promotionId = ((Number) JsonPath.read(promotionResult.getResponse().getContentAsString(), "$.id")).longValue();

        mockMvc.perform(get("/promotions/" + promotionId)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(promotionId))
                .andExpect(jsonPath("$.description").value("20% off on all items"))
                .andExpect(jsonPath("$.discount").value(20))
                .andExpect(jsonPath("$.placeName").value("Promotion Test Place"))
                .andExpect(jsonPath("$.imageUrl").isNotEmpty())
                .andExpect(jsonPath("$.startDate").value("2023-10-10T10:00:00"))
                .andExpect(jsonPath("$.endDate").value("2023-12-31T23:59:59"));
    }

    @Test
    @Order(3)
    void updatePromotion() throws Exception {
        MockMultipartFile initialPromotionImage = new MockMultipartFile("image", "initial-image.jpg", "image/jpeg", "initial image content".getBytes());
        MvcResult initialPromotionResult = mockMvc.perform(multipart("/promotions")
                        .file(initialPromotionImage)
                        .param("placeId", placeId.toString())
                        .param("description", "Initial Promotion Description")
                        .param("discount", "15")
                        .param("startDate", "2023-10-10T10:00:00")
                        .param("endDate", "2023-12-31T23:59:59")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn();

        Long promotionId = ((Number) JsonPath.read(initialPromotionResult.getResponse().getContentAsString(), "$.id")).longValue();

        // Archivo de imagen actualizado para la promoción
        MockMultipartFile updatedPromotionImage = new MockMultipartFile("image", "updated-image.jpg", "image/jpeg", "updated image content".getBytes());

        mockMvc.perform(multipart("/promotions/" + promotionId)
                        .file(updatedPromotionImage)
                        .param("description", "Updated Promotion Description")
                        .param("discount", "25")
                        .param("startDate", "2023-11-01T10:00:00")
                        .param("endDate", "2024-01-31T23:59:59")
                        .header("Authorization", "Bearer " + authToken)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(promotionId))
                .andExpect(jsonPath("$.description").value("Updated Promotion Description"))
                .andExpect(jsonPath("$.discount").value(25))
                .andExpect(jsonPath("$.startDate").value("2023-11-01T10:00:00"))
                .andExpect(jsonPath("$.endDate").value("2024-01-31T23:59:59"))
                .andExpect(jsonPath("$.placeName").value("Promotion Test Place"))
                .andExpect(jsonPath("$.imageUrl").isNotEmpty());
    }

    @Test
    @Order(4)
    void getAllPromotions() throws Exception {
        // Crear un lugar para la promoción
        MockMultipartFile placeImageFile = new MockMultipartFile("image", "place-image.jpg", "image/jpeg", "place image content".getBytes());
        MvcResult placeResult = mockMvc.perform(multipart("/places")
                        .file(placeImageFile)
                        .param("name", "Promotion Test Place")
                        .param("address", "123 Promo St")
                        .param("description", "Place for promotion testing")
                        .param("category", "RECREATIONAL")
                        .param("openingHours", "10AM - 8PM")
                        .param("coordinate.latitude", "40.7128")
                        .param("coordinate.longitude", "-74.0060")
                        .param("ownerEmail", "owneruser@example.com")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn();

        // Asegurarse de que el ID se maneje correctamente como Integer
        Integer placeIdInt = JsonPath.read(placeResult.getResponse().getContentAsString(), "$.id");
        Long placeId = placeIdInt.longValue();

        // Crear una promoción asociada al lugar creado
        MockMultipartFile promotionImageFile = new MockMultipartFile("image", "promotion-image.jpg", "image/jpeg", "promotion image content".getBytes());
        mockMvc.perform(multipart("/promotions")
                        .file(promotionImageFile)
                        .param("placeId", placeId.toString())
                        .param("description", "20% off on all items")
                        .param("discount", "20")
                        .param("startDate", "2023-10-10T10:00:00")
                        .param("endDate", "2023-12-31T20:00:00")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());

        // Verificar que el endpoint devuelve al menos una promoción
        mockMvc.perform(get("/promotions")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(Matchers.greaterThan(0)));
    }

    @Test
    @Order(5)
    void getPromotionsByPlace() throws Exception {
        String userEmail = "user" + System.currentTimeMillis() + "@example.com";
        String userPassword = "password123";
        String signUpPayloadUser = "{\"email\":\"" + userEmail + "\", \"name\":\"Test User\", \"password\":\"" + userPassword + "\", \"role\":\"USER\"}";

        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpPayloadUser))
                .andExpect(status().isOk());

        String loginPayloadUser = "{\"email\":\"" + userEmail + "\", \"password\":\"" + userPassword + "\"}";
        MvcResult loginResultUser = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayloadUser))
                .andExpect(status().isOk())
                .andReturn();

        String authTokenUser = JsonPath.read(loginResultUser.getResponse().getContentAsString(), "$.token");

        MockMultipartFile placeImageFile = new MockMultipartFile("image", "place-image.jpg", "image/jpeg", "place image content".getBytes());
        MvcResult placeResult = mockMvc.perform(multipart("/places")
                        .file(placeImageFile)
                        .param("name", "Promotion Test Place")
                        .param("address", "123 Promo St")
                        .param("description", "Place for promotion testing")
                        .param("category", "RECREATIONAL")
                        .param("openingHours", "10AM - 8PM")
                        .param("coordinate.latitude", "40.7128")
                        .param("coordinate.longitude", "-74.0060")
                        .param("ownerEmail", userEmail)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn();

        Integer placeIdInt = JsonPath.read(placeResult.getResponse().getContentAsString(), "$.id");
        Long placeId = placeIdInt.longValue();

        MockMultipartFile promotionImageFile = new MockMultipartFile("image", "promotion-image.jpg", "image/jpeg", "promotion image content".getBytes());
        mockMvc.perform(multipart("/promotions")
                        .file(promotionImageFile)
                        .param("placeId", placeId.toString())
                        .param("description", "Special Offer 15% off")
                        .param("discount", "15")
                        .param("startDate", "2023-10-10T10:00:00")
                        .param("endDate", "2023-12-31T20:00:00")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());

        mockMvc.perform(get("/promotions/place/" + placeId)
                        .header("Authorization", "Bearer " + authTokenUser) // Autenticación con usuario USER
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(Matchers.greaterThan(0)))
                .andExpect(jsonPath("$[0].placeName").value("Promotion Test Place"))
                .andExpect(jsonPath("$[0].description").value("Special Offer 15% off"))
                .andExpect(jsonPath("$[0].discount").value(15));
    }

    @Test
    @Order(6)
    void getPromotionsByMyPlaces() throws Exception {
        // Crear un usuario OWNER para autenticación
        String ownerEmail = "owneruser" + System.currentTimeMillis() + "@example.com";
        String ownerPassword = "password123";
        String signUpPayloadOwner = "{\"email\":\"" + ownerEmail + "\", \"name\":\"Owner User\", \"password\":\"" + ownerPassword + "\", \"role\":\"OWNER\"}";

        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpPayloadOwner))
                .andExpect(status().isOk());

        String loginPayloadOwner = "{\"email\":\"" + ownerEmail + "\", \"password\":\"" + ownerPassword + "\"}";
        MvcResult loginResultOwner = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayloadOwner))
                .andExpect(status().isOk())
                .andReturn();

        String authTokenOwner = JsonPath.read(loginResultOwner.getResponse().getContentAsString(), "$.token");

        MockMultipartFile placeImageFile = new MockMultipartFile("image", "place-image.jpg", "image/jpeg", "place image content".getBytes());
        MvcResult placeResult = mockMvc.perform(multipart("/places")
                        .file(placeImageFile)
                        .param("name", "Promotion Owner Place")
                        .param("address", "123 Owner St")
                        .param("description", "Place for owner's promotion testing")
                        .param("category", "RECREATIONAL")
                        .param("openingHours", "10AM - 8PM")
                        .param("coordinate.latitude", "40.7128")
                        .param("coordinate.longitude", "-74.0060")
                        .param("ownerEmail", ownerEmail)
                        .header("Authorization", "Bearer " + authTokenOwner)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn();

        Integer placeIdInt = JsonPath.read(placeResult.getResponse().getContentAsString(), "$.id");
        Long placeId = placeIdInt.longValue();

        MockMultipartFile promotionImageFile = new MockMultipartFile("image", "promotion-image.jpg", "image/jpeg", "promotion image content".getBytes());
        mockMvc.perform(multipart("/promotions")
                        .file(promotionImageFile)
                        .param("placeId", placeId.toString())
                        .param("description", "Exclusive Offer 20% off")
                        .param("discount", "20")
                        .param("startDate", "2023-10-10T10:00:00")
                        .param("endDate", "2023-12-31T20:00:00")
                        .header("Authorization", "Bearer " + authTokenOwner)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());

        mockMvc.perform(get("/promotions/myplaces")
                        .header("Authorization", "Bearer " + authTokenOwner) // Autenticación con usuario OWNER
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(Matchers.greaterThan(0)))
                .andExpect(jsonPath("$[0].placeName").value("Promotion Owner Place"))
                .andExpect(jsonPath("$[0].description").value("Exclusive Offer 20% off"))
                .andExpect(jsonPath("$[0].discount").value(20));
    }

    @Test
    @Order(7)
    void deletePromotion() throws Exception {
        String ownerEmail = "owneruser" + System.currentTimeMillis() + "@example.com";
        String ownerPassword = "password123";
        String signUpPayloadOwner = "{\"email\":\"" + ownerEmail + "\", \"name\":\"Owner User\", \"password\":\"" + ownerPassword + "\", \"role\":\"OWNER\"}";

        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpPayloadOwner))
                .andExpect(status().isOk());

        // Autenticar el usuario OWNER
        String loginPayloadOwner = "{\"email\":\"" + ownerEmail + "\", \"password\":\"" + ownerPassword + "\"}";
        MvcResult loginResultOwner = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayloadOwner))
                .andExpect(status().isOk())
                .andReturn();

        String authTokenOwner = JsonPath.read(loginResultOwner.getResponse().getContentAsString(), "$.token");

        // Crear un lugar asociado al usuario OWNER
        MockMultipartFile placeImageFile = new MockMultipartFile("image", "place-image.jpg", "image/jpeg", "place image content".getBytes());
        MvcResult placeResult = mockMvc.perform(multipart("/places")
                        .file(placeImageFile)
                        .param("name", "Promotion Deletion Place")
                        .param("address", "456 Owner St")
                        .param("description", "Place for promotion deletion testing")
                        .param("category", "RECREATIONAL")
                        .param("openingHours", "10AM - 8PM")
                        .param("coordinate.latitude", "40.7128")
                        .param("coordinate.longitude", "-74.0060")
                        .param("ownerEmail", ownerEmail)
                        .header("Authorization", "Bearer " + authTokenOwner)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn();

        Integer placeIdInt = JsonPath.read(placeResult.getResponse().getContentAsString(), "$.id");
        Long placeId = placeIdInt.longValue();

        MockMultipartFile promotionImageFile = new MockMultipartFile("image", "promotion-image.jpg", "image/jpeg", "promotion image content".getBytes());
        MvcResult promotionResult = mockMvc.perform(multipart("/promotions")
                        .file(promotionImageFile)
                        .param("placeId", placeId.toString())
                        .param("description", "Temporary Promotion")
                        .param("discount", "10")
                        .param("startDate", "2023-10-10T10:00:00")
                        .param("endDate", "2023-12-31T20:00:00")
                        .header("Authorization", "Bearer " + authTokenOwner)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn();

        Integer promotionIdInt = JsonPath.read(promotionResult.getResponse().getContentAsString(), "$.id");
        Long promotionId = promotionIdInt.longValue();

        mockMvc.perform(delete("/promotions/" + promotionId)
                        .header("Authorization", "Bearer " + authTokenOwner)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/promotions/" + promotionId)
                        .header("Authorization", "Bearer " + authTokenOwner)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
