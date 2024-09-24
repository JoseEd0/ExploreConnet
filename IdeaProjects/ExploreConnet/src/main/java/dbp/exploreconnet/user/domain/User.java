package dbp.exploreconnet.user.domain;

import dbp.exploreconnet.itinerary.domain.Itinerary;
import dbp.exploreconnet.notification.domain.Notification;
import dbp.exploreconnet.place.domain.Place;
import dbp.exploreconnet.reservation.domain.Reservation;
import dbp.exploreconnet.review.domain.Review;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "users")
public class User implements UserDetails {  // Implementa UserDetails

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;  // Enum for roles (GUEST, USER, OWNER)

    @OneToOne(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private Place place;  // Owner's local (if applicable)

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reservation> reservations;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Itinerary> itineraries;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notification> notifications;

    private LocalDateTime createdAt;

    // Métodos de UserDetails

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Convertimos el rol a una autoridad
        return List.of(() -> role.name());
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;  // getEmail() actúa como el username
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;  // Puedes implementar la lógica según tus reglas
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;  // Puedes implementar la lógica según tus reglas
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;  // Puedes implementar la lógica según tus reglas
    }

    @Override
    public boolean isEnabled() {
        return true;  // Puedes implementar la lógica según tus reglas
    }
}
