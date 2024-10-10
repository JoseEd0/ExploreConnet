package dbp.exploreconnet.place.application;

import com.jayway.jsonpath.JsonPath;
import dbp.exploreconnet.email.domain.EmailService;
import dbp.exploreconnet.place.domain.PlaceCategory;
import org.hamcrest.Matchers;
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
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
class PlaceControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    private String authToken;
    private Long placeId;
    private String uniquePlaceName;

    @MockBean
    EmailService emailService;

    @BeforeEach
    void setup() throws Exception {

        doNothing().when(emailService).correoSingIn(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());

        // Generar un correo y nombre de lugar únicos para evitar conflictos
        String uniqueEmail = "owneruser" + System.currentTimeMillis() + "@example.com";
        uniquePlaceName = "My Test Place " + System.currentTimeMillis();

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

        // Crear un lugar con un nombre único para evitar duplicados
        MockMultipartFile imageFile = new MockMultipartFile("image", "test-image.jpg", "image/jpeg", "test image content".getBytes());
        MvcResult placeResult = mockMvc.perform(multipart("/places")
                        .file(imageFile)
                        .param("name", uniquePlaceName) // Nombre único para cada prueba
                        .param("address", "123 Test St")
                        .param("description", "This is a test place")
                        .param("category", "RECREATIONAL")
                        .param("openingHours", "9AM - 5PM")
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
    void createPlace() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile("image", "new-image.jpg", "image/jpeg", "new image content".getBytes());

        mockMvc.perform(multipart("/places")
                        .file(imageFile)
                        .param("name", "New Test Place")
                        .param("address", "456 Test Ave")
                        .param("description", "Another test place")
                        .param("category", "CAFETERIA")
                        .param("openingHours", "10AM - 6PM")
                        .param("coordinate.latitude", "34.0522")
                        .param("coordinate.longitude", "-118.2437")
                        .param("ownerEmail", "owneruser@example.com")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Test Place"))
                .andExpect(jsonPath("$.address").value("456 Test Ave"))
                .andExpect(jsonPath("$.description").value("Another test place"))
                .andExpect(jsonPath("$.category").value("CAFETERIA"))
                .andExpect(jsonPath("$.openingHours").value("10AM - 6PM"))
                .andExpect(jsonPath("$.coordinate.latitude").value(34.0522))
                .andExpect(jsonPath("$.coordinate.longitude").value(-118.2437));
    }

    @Test
    @Order(2)
    void getPlaceById() throws Exception {
        mockMvc.perform(get("/places/" + placeId)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(placeId))
                .andExpect(jsonPath("$.name").value(uniquePlaceName)) // Verifica el nombre único
                .andExpect(jsonPath("$.address").value("123 Test St"))
                .andExpect(jsonPath("$.description").value("This is a test place"))
                .andExpect(jsonPath("$.category").value("RECREATIONAL"))
                .andExpect(jsonPath("$.openingHours").value("9AM - 5PM"))
                .andExpect(jsonPath("$.coordinate.latitude").value(40.7128))
                .andExpect(jsonPath("$.coordinate.longitude").value(-74.0060));
    }

    @Test
    @Order(3)
    void updatePlace() throws Exception {
        MockMultipartFile updatedImageFile = new MockMultipartFile("image", "updated-image.jpg", "image/jpeg", "updated image content".getBytes());

        mockMvc.perform(multipart("/places/" + placeId)
                        .file(updatedImageFile)
                        .param("name", "Updated Test Place")
                        .param("address", "456 Updated St")
                        .param("description", "This is an updated test place")
                        .param("category", "RESTAURANT")
                        .param("openingHours", "10AM - 6PM")
                        .param("coordinate.latitude", "41.0000")
                        .param("coordinate.longitude", "-75.0000")
                        .header("Authorization", "Bearer " + authToken)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Test Place"))
                .andExpect(jsonPath("$.address").value("456 Updated St"))
                .andExpect(jsonPath("$.description").value("This is an updated test place"))
                .andExpect(jsonPath("$.category").value("RESTAURANT"))
                .andExpect(jsonPath("$.openingHours").value("10AM - 6PM"))
                .andExpect(jsonPath("$.coordinate.latitude").value(41.0000))
                .andExpect(jsonPath("$.coordinate.longitude").value(-75.0000))
                .andExpect(jsonPath("$.imageUrl").isNotEmpty());
    }

    @Test
    @Order(4)
    void getAllPlaces() throws Exception {
        mockMvc.perform(get("/places")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(Matchers.greaterThan(0)));
    }

    @Test
    @Order(5)
    void getPlaceByName() throws Exception {
        // Usa el nombre único en el test para evitar duplicados
        mockMvc.perform(get("/places/name/" + uniquePlaceName)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(uniquePlaceName))
                .andExpect(jsonPath("$.address").value("123 Test St"))
                .andExpect(jsonPath("$.description").value("This is a test place"))
                .andExpect(jsonPath("$.category").value("RECREATIONAL"))
                .andExpect(jsonPath("$.openingHours").value("9AM - 5PM"))
                .andExpect(jsonPath("$.coordinate.latitude").value(40.7128))
                .andExpect(jsonPath("$.coordinate.longitude").value(-74.0060))
                .andExpect(jsonPath("$.imageUrl").isNotEmpty());
    }

    @Test
    @Order(6)
    void getMyPlaces() throws Exception {
        mockMvc.perform(get("/places/me")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value(uniquePlaceName))
                .andExpect(jsonPath("$[0].address").value("123 Test St"))
                .andExpect(jsonPath("$[0].description").value("This is a test place"))
                .andExpect(jsonPath("$[0].category").value("RECREATIONAL"))
                .andExpect(jsonPath("$[0].openingHours").value("9AM - 5PM"))
                .andExpect(jsonPath("$[0].coordinate.latitude").value(40.7128))
                .andExpect(jsonPath("$[0].coordinate.longitude").value(-74.0060))
                .andExpect(jsonPath("$[0].imageUrl").isNotEmpty());
    }

    @Test
    @Order(7)
    void deletePlace() throws Exception {
        MvcResult placeResult = mockMvc.perform(multipart("/places")
                        .file("image", "test-image.jpg".getBytes())
                        .param("name", "Place to Delete")
                        .param("address", "Delete St 123")
                        .param("description", "This place will be deleted in the test")
                        .param("category", "RECREATIONAL")
                        .param("openingHours", "10AM - 6PM")
                        .param("coordinate.latitude", "40.7128")
                        .param("coordinate.longitude", "-74.0060")
                        .param("ownerEmail", "owneruser@example.com")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn();

        Integer placeIdInt = JsonPath.read(placeResult.getResponse().getContentAsString(), "$.id");
        Long placeId = placeIdInt.longValue();

        mockMvc.perform(delete("/places/" + placeId)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/places/" + placeId)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
