package dbp.exploreconnet.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import dbp.exploreconnet.user.domain.User;
import dbp.exploreconnet.user.domain.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    private final UserService userService;

    public String extractUsername(String token) {
        return JWT.decode(token).getSubject();
    }

    public String generateToken(UserDetails data) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + 1000 * 60 * 60 * 10);

        Algorithm algorithm = Algorithm.HMAC256(secret);

        return JWT.create()
                .withSubject(data.getUsername())
                .withClaim("role", data.getAuthorities().toArray()[0].toString())
                .withIssuedAt(now)
                .withExpiresAt(expiration)
                .sign(algorithm);
    }

    public String generateToken(User user) {
        UserDetails userDetails = userService.userDetailsService().loadUserByUsername(user.getUsername());
        return generateToken(userDetails);
    }

    public void validateToken(String token, String userEmail) throws AuthenticationException {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret)).build();
            verifier.verify(token);

            UserDetails userDetails = userService.userDetailsService().loadUserByUsername(userEmail);

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    token,
                    userDetails.getAuthorities()
            );

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authToken);
            SecurityContextHolder.setContext(context);

        } catch (JWTVerificationException ex) {
            throw new AuthenticationException("Token JWT no válido", ex) {};
        }
    }
}
