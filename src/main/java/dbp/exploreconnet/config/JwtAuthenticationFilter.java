package dbp.exploreconnet.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Autowired
    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest servletRequest, HttpServletResponse servletResponse, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = servletRequest.getHeader("Authorization");
        String jwt;
        String userEmail;

        // Verificar si la cabecera tiene el token JWT en formato Bearer
        if (!StringUtils.hasText(authHeader) || !StringUtils.startsWithIgnoreCase(authHeader, "Bearer ")) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        // Extraer el token JWT de la cabecera
        jwt = authHeader.substring(7);

        try {
            // Extraer el correo electrónico (nombre de usuario) del token
            userEmail = jwtService.extractUsername(jwt);

            // Verificar si el usuario aún no está autenticado
            if (StringUtils.hasText(userEmail) && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Validar el token
                jwtService.validateToken(jwt, userEmail);
            }

        } catch (AuthenticationException ex) {
            // Si la validación del token falla, devolver un error 401 (No autorizado)
            servletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token JWT no válido");
            return;
        }

        // Continuar con el filtro si el token es válido
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
