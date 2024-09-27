package dbp.exploreconnet.auth.domain;

import dbp.exploreconnet.auth.dto.JwtAuthResponseDto;
import dbp.exploreconnet.auth.dto.LoginDto;
import dbp.exploreconnet.auth.dto.SigninDto;
import dbp.exploreconnet.config.JwtService;
import dbp.exploreconnet.events.SignIn.SignInEvent;
import dbp.exploreconnet.exceptions.UserAlreadyExistException;
import dbp.exploreconnet.user.domain.Role;
import dbp.exploreconnet.user.domain.User;
import dbp.exploreconnet.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher applicationEventPublisher;

    public JwtAuthResponseDto login(LoginDto logInDto) {
        User user = userRepository.findByEmail(logInDto.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Email not found"));

        if (!passwordEncoder.matches(logInDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }

        JwtAuthResponseDto response = new JwtAuthResponseDto();
        response.setToken(jwtService.generateToken(user));
        return response;
    }


    public JwtAuthResponseDto signIn(SigninDto signinDto) {
        if (userRepository.findByEmail(signinDto.getEmail()).isPresent()) {
            throw new UserAlreadyExistException("Email already exist");
        }

        applicationEventPublisher.publishEvent(new SignInEvent(signinDto.getEmail(), signinDto.getName()));

        User user = new User();
        user.setEmail(signinDto.getEmail());
        user.setPassword(passwordEncoder.encode(signinDto.getPassword()));
        user.setFullName(signinDto.getName());
        user.setCreatedAt(LocalDateTime.now());
        user.setRole(signinDto.getRole());

        userRepository.save(user);

        JwtAuthResponseDto response = new JwtAuthResponseDto();
        jwtService.generateToken(user);
        response.setMessage("Registro Exitoso");
        return response;
    }

    public boolean verifyPassword(Long userId, String password) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return passwordEncoder.matches(password, user.getPassword());
    }
}
