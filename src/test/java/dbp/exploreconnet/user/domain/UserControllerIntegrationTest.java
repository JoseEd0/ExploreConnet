package dbp.exploreconnet.user.domain;

import dbp.exploreconnet.user.application.UserController;
import dbp.exploreconnet.user.dto.UserRequestDto;
import dbp.exploreconnet.user.dto.UserResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class UserControllerIntegrationTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // Test para el endpoint GET /users/{id}
    @Test
    public void testGetUserById() {
        Long userId = 1L;
        UserResponseDto mockUser = new UserResponseDto();
        mockUser.setId(userId);
        mockUser.setFullName("John Doe");
        mockUser.setEmail("john@example.com");

        when(userService.getUserById(userId)).thenReturn(mockUser);

        ResponseEntity<UserResponseDto> response = userController.getUserById(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockUser, response.getBody());
        verify(userService, times(1)).getUserById(userId);
    }

    // Test para el endpoint PUT /users/{id}
    @Test
    public void testUpdateUser() {
        Long userId = 1L;
        UserRequestDto updateUserRequest = new UserRequestDto();
        updateUserRequest.setFullName("Jane Doe");
        updateUserRequest.setEmail("jane@example.com");

        UserResponseDto updatedUser = new UserResponseDto();
        updatedUser.setId(userId);
        updatedUser.setFullName("Jane Doe");
        updatedUser.setEmail("jane@example.com");

        when(userService.updateUser(userId, updateUserRequest)).thenReturn(updatedUser);

        ResponseEntity<UserResponseDto> response = userController.updateUser(userId, updateUserRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedUser, response.getBody());
        verify(userService, times(1)).updateUser(userId, updateUserRequest);
    }

    // Test para el endpoint DELETE /users/{id}
    @Test
    public void testDeleteUser() {
        Long userId = 1L;

        // Simula el comportamiento de deleteUser en UserService
        doNothing().when(userService).deleteUser(userId);

        // Ejecuta el endpoint
        ResponseEntity<Void> response = userController.deleteUser(userId);

        // Verifica la respuesta
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userService, times(1)).deleteUser(userId);
    }

    // Test para el endpoint GET /users
    @Test
    public void testGetAllUsers() {
        List<UserResponseDto> mockUsers = new ArrayList<>();
        UserResponseDto user1 = new UserResponseDto();
        user1.setId(1L);
        user1.setFullName("John Doe");
        user1.setEmail("john@example.com");

        UserResponseDto user2 = new UserResponseDto();
        user2.setId(2L);
        user2.setFullName("Jane Smith");
        user2.setEmail("jane@example.com");

        mockUsers.add(user1);
        mockUsers.add(user2);

        when(userService.getAllUsers()).thenReturn(mockUsers);

        ResponseEntity<List<UserResponseDto>> response = userController.getAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockUsers, response.getBody());
        verify(userService, times(1)).getAllUsers();
    }
}
