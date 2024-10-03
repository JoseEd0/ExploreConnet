package dbp.exploreconnet.place.domain;

import dbp.exploreconnet.place.application.PlaceController;
import dbp.exploreconnet.place.domain.PlaceService;
import dbp.exploreconnet.place.dto.PlaceRequestDto;
import dbp.exploreconnet.place.dto.PlaceResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PlaceControllerIntegrationTest {

    @InjectMocks
    private PlaceController placeController;

    @Mock
    private PlaceService placeService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreatePlace() {
        PlaceRequestDto placeRequest = new PlaceRequestDto();
        placeRequest.setName("Place Name");
        placeRequest.setAddress("123 Main St");
        placeRequest.setImage("image.jpg");
        placeRequest.setDescription("A nice place");
        placeRequest.setCategory(PlaceCategory.CAFETERIA);
        placeRequest.setOpeningHours("9 AM - 9 PM");
        placeRequest.setLatitude(40.7128);
        placeRequest.setLongitude(-74.0060);

        PlaceResponseDto placeResponse = new PlaceResponseDto();
        placeResponse.setId(1L);
        placeResponse.setName("Place Name");

        when(placeService.createPlace(placeRequest)).thenReturn(placeResponse);

        ResponseEntity<PlaceResponseDto> response = placeController.createPlace(placeRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(placeResponse, response.getBody());
        verify(placeService, times(1)).createPlace(placeRequest);
    }

    @Test
    public void testGetPlaceById() {
        Long placeId = 1L;
        PlaceResponseDto placeResponse = new PlaceResponseDto();
        placeResponse.setId(placeId);
        placeResponse.setName("Place Name");

        when(placeService.getPlaceById(placeId)).thenReturn(placeResponse);

        ResponseEntity<PlaceResponseDto> response = placeController.getPlaceById(placeId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(placeResponse, response.getBody());
        verify(placeService, times(1)).getPlaceById(placeId);
    }

    @Test
    public void testUpdatePlace() {
        Long placeId = 1L;
        PlaceRequestDto placeRequest = new PlaceRequestDto();
        placeRequest.setName("Updated Place");
        placeRequest.setAddress("456 New Address");
        placeRequest.setDescription("Updated description");
        placeRequest.setCategory(PlaceCategory.RESTAURANT);

        PlaceResponseDto updatedPlace = new PlaceResponseDto();
        updatedPlace.setId(placeId);
        updatedPlace.setName("Updated Place");

        when(placeService.updatePlace(placeId, placeRequest)).thenReturn(updatedPlace);

        ResponseEntity<PlaceResponseDto> response = placeController.updatePlace(placeId, placeRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedPlace, response.getBody());
        verify(placeService, times(1)).updatePlace(placeId, placeRequest);
    }

    @Test
    public void testDeletePlace() {
        // Arrange
        Long placeId = 1L;
        doNothing().when(placeService).deletePlace(placeId);

        // Act
        ResponseEntity<Void> response = placeController.deletePlace(placeId);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(placeService, times(1)).deletePlace(placeId);
    }

    @Test
    public void testGetAllPlaces() {
        // Arrange
        PlaceResponseDto place1 = new PlaceResponseDto();
        place1.setId(1L);
        place1.setName("Place 1");

        PlaceResponseDto place2 = new PlaceResponseDto();
        place2.setId(2L);
        place2.setName("Place 2");

        List<PlaceResponseDto> places = Arrays.asList(place1, place2);

        when(placeService.getAllPlaces()).thenReturn(places);

        ResponseEntity<List<PlaceResponseDto>> response = placeController.getAllPlaces();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(places, response.getBody());
        verify(placeService, times(1)).getAllPlaces();
    }
}
