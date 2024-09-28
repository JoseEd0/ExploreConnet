package dbp.exploreconnet.user.domain;

import dbp.exploreconnet.exceptions.ResourceNotFoundException;
import dbp.exploreconnet.user.dto.UserRequestDto;
import dbp.exploreconnet.user.dto.UserResponseDto;
import dbp.exploreconnet.user.infrastructure.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return convertToDto(user);
    }

    public List<UserResponseDto> getUserByRole(String role) {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole().name().equalsIgnoreCase(role))
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public UserResponseDto updateUser(Long id, UserRequestDto userRequestDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setFullName(userRequestDto.getFullName());
        user.setEmail(userRequestDto.getEmail());
        user.setPassword(userRequestDto.getPassword()); // No encoding
        user.setRole(Role.valueOf(userRequestDto.getRole().toUpperCase()));
        userRepository.save(user);
        return convertToDto(user);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        userRepository.delete(user);
    }

    private UserResponseDto convertToDto(User user) {
        UserResponseDto userResponseDto = new UserResponseDto();
        userResponseDto.setId(user.getId());
        userResponseDto.setFullName(user.getFullName());
        userResponseDto.setEmail(user.getEmail());
        userResponseDto.setRole(user.getRole().name());
        return userResponseDto;
    }
}
