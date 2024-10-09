package dbp.exploreconnet.user.infraestructure;


import dbp.exploreconnet.AbstractContainerBaseTest;
import dbp.exploreconnet.user.domain.User;
import dbp.exploreconnet.user.domain.Role;
import dbp.exploreconnet.user.infrastructure.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryTest extends AbstractContainerBaseTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    User user1;
    User user2;
    User user3;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setFullName("John Doe");
        user1.setEmail("john@example.com");
        user1.setPassword("password123");
        user1.setRole(Role.USER);  // Role from Enum
        user1.setCreatedAt(LocalDateTime.now());

        user2 = new User();
        user2.setFullName("Jane Smith");
        user2.setEmail("jane@example.com");
        user2.setPassword("password456");
        user2.setRole(Role.OWNER);  // Role OWNER
        user2.setCreatedAt(LocalDateTime.now());

        user3 = new User();
        user3.setFullName("Alice Brown");
        user3.setEmail("alice@example.com");
        user3.setPassword("password789");
        user3.setRole(Role.GUEST);  // Role GUEST
        user3.setCreatedAt(LocalDateTime.now());

        testEntityManager.persist(user1);
        testEntityManager.persist(user2);
        testEntityManager.persist(user3);
        testEntityManager.flush();
    }

    @Test
    void testSaveUser() {
        User newUser = new User();
        newUser.setFullName("Charlie Johnson");
        newUser.setEmail("charlie@example.com");
        newUser.setPassword("password123");
        newUser.setRole(Role.USER);  // Using Role Enum
        newUser.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(newUser);

        assertNotNull(savedUser.getId());
        assertEquals("Charlie Johnson", savedUser.getFullName());
        assertEquals("charlie@example.com", savedUser.getEmail());
        assertEquals(Role.USER, savedUser.getRole());  // Verifying role
    }

    @Test
    void testFindByEmail() {
        Optional<User> foundUser = userRepository.findByEmail("jane@example.com");

        assertTrue(foundUser.isPresent());
        assertEquals("Jane Smith", foundUser.get().getFullName());
        assertEquals(Role.OWNER, foundUser.get().getRole());  // Verifying role OWNER
    }

    @Test
    void testFindAllUsers() {
        List<User> users = userRepository.findAll();
        assertEquals(3, users.size());  // We have 3 users from setUp()
    }

    @Test
    void testDeleteUser() {
        userRepository.delete(user1);
        Optional<User> foundUser = userRepository.findById(user1.getId());

        assertFalse(foundUser.isPresent());  // User should be deleted
    }

    @Test
    void testUpdateUser() {
        user2.setFullName("Jane Doe");
        userRepository.save(user2);

        Optional<User> updatedUser = userRepository.findByEmail("jane@example.com");
        assertTrue(updatedUser.isPresent());
        assertEquals("Jane Doe", updatedUser.get().getFullName());  // Name should be updated
    }

    @Test
    void testRoleAssignment() {
        Optional<User> foundUser = userRepository.findByEmail("alice@example.com");

        assertTrue(foundUser.isPresent());
        assertEquals(Role.GUEST, foundUser.get().getRole());  // Verifying role GUEST
    }
}
