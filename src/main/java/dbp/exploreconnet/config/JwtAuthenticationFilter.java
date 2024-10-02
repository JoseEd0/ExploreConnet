package dbp.exploreconnet.config;

import dbp.exploreconnet.user.domain.Role;
import dbp.exploreconnet.user.domain.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;

    @Autowired
    public JwtAuthenticationFilter(JwtService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest servletRequest, HttpServletResponse servletResponse, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = servletRequest.getHeader("Authorization");
        String jwt;
        String userEmail;


        if (!StringUtils.hasText(authHeader) || !StringUtils.startsWithIgnoreCase(authHeader, "Bearer ")) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        jwt = authHeader.substring(7);

        try {
            userEmail = jwtService.extractUsername(jwt);

            if (StringUtils.hasText(userEmail) && SecurityContextHolder.getContext().getAuthentication() == null) {
                jwtService.validateToken(jwt, userEmail);

                List<Role> roles = jwtService.extractRoles(jwt);

                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority(role.name()))
                        .collect(Collectors.toList());

                UserDetails userDetails = userService.userDetailsService().loadUserByUsername(userEmail);

                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, authorities);

                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(servletRequest));

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }

        } catch (AuthenticationException ex) {
            servletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token JWT no válido");
            return;
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}