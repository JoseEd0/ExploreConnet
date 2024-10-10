package dbp.exploreconnet.user.domain;

import dbp.exploreconnet.exceptions.ResourceNotFoundException;
import dbp.exploreconnet.mediaStorage.domain.MediaStorageService;
import dbp.exploreconnet.user.dto.UserRequestDto;
import dbp.exploreconnet.user.dto.UserResponseDto;
import dbp.exploreconnet.user.infrastructure.UserRepository;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MediaStorageService mediaStorageService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean(name = "UserDetailsService")
    public UserDetailsService userDetailsService() {
        return username -> {
            User user = userRepository
                    .findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            return (UserDetails) user;
        };
    }

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


    public UserResponseDto updateUser(Long id, UserRequestDto userRequestDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setFullName(userRequestDto.getFullName());
        user.setEmail(userRequestDto.getEmail());

        String newPassword = userRequestDto.getPassword();
        if (!passwordEncoder.matches(newPassword, user.getPassword())) {
            user.setPassword(passwordEncoder.encode(newPassword));
        }

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
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public UserResponseDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return mapToUserResponseDto(user);
    }

    public UserResponseDto updateUserByEmail(String email, UserRequestDto userRequestDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        user.setFullName(userRequestDto.getFullName());
        user.setPassword(userRequestDto.getPassword());
        user.setRole(Role.valueOf(userRequestDto.getRole()));
        userRepository.save(user);
        return mapToUserResponseDto(user);
    }

    public UserResponseDto updateProfilePhoto(Long id, MultipartFile profilePhoto) throws FileUploadException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (profilePhoto == null || profilePhoto.isEmpty()) {
            throw new FileUploadException("Profile photo is missing or empty");
        }

        String profilePhotoUrl = mediaStorageService.uploadFile(profilePhoto);
        user.setProfileImageUrl(profilePhotoUrl); // Actualizar la URL de la foto de perfil

        userRepository.save(user);

        return mapToUserResponseDto(user);
    }




    private UserResponseDto mapToUserResponseDto(User user) {
        UserResponseDto userResponseDto = new UserResponseDto();
        userResponseDto.setId(user.getId());
        userResponseDto.setFullName(user.getFullName());
        userResponseDto.setEmail(user.getEmail());
        userResponseDto.setRole(user.getRole().name());
        userResponseDto.setProfileImage(user.getProfileImageUrl());
        return userResponseDto;
    }
}
