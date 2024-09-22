package dbp.exploreconnet.user.infrastructure;

import dbp.exploreconnet.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
}
