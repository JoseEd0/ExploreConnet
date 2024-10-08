package dbp.exploreconnet.user.application;

import dbp.exploreconnet.user.domain.User;
import dbp.exploreconnet.user.domain.UserService;
import dbp.exploreconnet.user.dto.UserRequestDto;
import dbp.exploreconnet.user.dto.UserResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PreAuthorize("hasAuthority('USER')")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PreAuthorize("hasAuthority('USER')")
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long id, @RequestBody UserRequestDto userRequestDto) {
        return ResponseEntity.ok(userService.updateUser(id, userRequestDto));
    }


    @PreAuthorize("hasAuthority('USER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }


    @PreAuthorize("hasAuthority('OWNER')")
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PreAuthorize("hasAnyAuthority('USER', 'OWNER')")
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @PreAuthorize("hasAnyAuthority('USER', 'OWNER')")
    @PutMapping("/edit/me")
    public ResponseEntity<UserResponseDto> updateCurrentUser(@RequestBody UserRequestDto userRequestDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return ResponseEntity.ok(userService.updateUserByEmail(email, userRequestDto));
    }

}
