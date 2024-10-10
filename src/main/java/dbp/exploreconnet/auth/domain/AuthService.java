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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    @Autowired
    private UserRepository<User> userRepository;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

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

        userRepository.save(user);
        JwtAuthResponseDto response = new JwtAuthResponseDto();
        response.setToken(jwtService.generateToken(user));
        return response;
    }

    public boolean verifyPasswordByEmail(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return passwordEncoder.matches(password, user.getPassword());
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String token = jwtService.resolveToken(request);
        if (token != null) {
            jwtService.invalidateToken(token);
        }
    }

}
