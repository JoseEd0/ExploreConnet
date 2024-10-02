package dbp.exploreconnet.auth.application;

import dbp.exploreconnet.auth.domain.AuthService;
import dbp.exploreconnet.auth.domain.GoogleAuthService;
import dbp.exploreconnet.auth.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authenticationService;
    private final GoogleAuthService googleAuthService;

    @PostMapping("/google")
    public ResponseEntity<GoogleAuthResponseDto> validateGoogleAuthToken(@RequestBody GoogleAuthRequestDto googleAuthRequestDto) {
        return ResponseEntity.ok(googleAuthService.validate(googleAuthRequestDto));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponseDto> login(@RequestBody LoginDto logInDTO) {
        return ResponseEntity.ok(authenticationService.login(logInDTO));
    }

    @PostMapping("/signin")
    public ResponseEntity<JwtAuthResponseDto> signin(@RequestBody SigninDto signInDTO) {
        return ResponseEntity.ok(authenticationService.signIn(signInDTO));
    }

    @PostMapping("/verify-password")
    public ResponseEntity<Boolean> verifyPassword(@RequestBody UserPasswordVerificationRequestDto request) {
        boolean isValid = authenticationService.verifyPasswordByEmail(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(isValid);
    }

}