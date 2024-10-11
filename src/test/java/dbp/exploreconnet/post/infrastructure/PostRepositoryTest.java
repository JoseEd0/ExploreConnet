package dbp.exploreconnet.post.infrastructure;

import dbp.exploreconnet.AbstractContainerBaseTest;
import dbp.exploreconnet.post.domain.Post;
import dbp.exploreconnet.place.domain.Place;
import dbp.exploreconnet.place.domain.PlaceCategory;
import dbp.exploreconnet.user.domain.User;
import dbp.exploreconnet.user.domain.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PostRepositoryTest extends AbstractContainerBaseTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private User user;
    private Place place;
    private Post post1;
    private Post post2;

    @BeforeEach
    void setUp() {
        // Crear un User como owner
        user = new User();
        user.setFullName("Jane Doe");
        user.setEmail("jane@example.com");
        user.setPassword("password123");
        user.setRole(Role.USER);
        user.setCreatedAt(LocalDateTime.now());
        testEntityManager.persist(user);

        // Crear un Place
        place = new Place();
        place.setName("Sample Place");
        place.setAddress("123 Main St");
        place.setDescription("A cozy place for social gatherings.");
        place.setCategory(PlaceCategory.CAFETERIA);
        place.setOpeningHours("08:00 - 22:00");
        place.setOwner(user);
        testEntityManager.persist(place);

        // Crear dos Posts
        post1 = new Post();
        post1.setDescription("First post description.");
        post1.setLikes(10);
        post1.setCreatedAt(LocalDateTime.now().minusDays(1));
        post1.setUser(user);
        post1.setPlace(place);

        post2 = new Post();
        post2.setDescription("Second post description.");
        post2.setLikes(20);
        post2.setCreatedAt(LocalDateTime.now());
        post2.setUser(user);
        post2.setPlace(place);

        testEntityManager.persist(post1);
        testEntityManager.persist(post2);
        testEntityManager.flush();
    }

    @Test
    void testSavePost() {
        Post newPost = new Post();
        newPost.setDescription("New post description.");
        newPost.setLikes(5);
        newPost.setCreatedAt(LocalDateTime.now());
        newPost.setUser(user);
        newPost.setPlace(place);

        Post savedPost = postRepository.save(newPost);
        assertNotNull(savedPost.getId());
        assertEquals("New post description.", savedPost.getDescription());
        assertEquals(5, savedPost.getLikes());
        assertEquals(user.getId(), savedPost.getUser().getId());
        assertEquals(place.getId(), savedPost.getPlace().getId());
    }

    @Test
    void testFindById() {
        Optional<Post> foundPost = postRepository.findById(post1.getId());
        assertTrue(foundPost.isPresent());
        assertEquals("First post description.", foundPost.get().getDescription());
        assertEquals(10, foundPost.get().getLikes());
    }

    @Test
    void testFindByPlaceId() {
        List<Post> posts = postRepository.findByPlaceId(place.getId());
        assertEquals(2, posts.size());
        assertEquals("First post description.", posts.get(0).getDescription());
    }

    @Test
    void testFindByUserId() {
        Page<Post> userPosts = postRepository.findByUserId(user.getId(), PageRequest.of(0, 10));
        assertEquals(2, userPosts.getTotalElements());
        assertEquals("First post description.", userPosts.getContent().get(0).getDescription());
    }

    @Test
    void testFindAllByOrderByCreatedAtDesc() {
        Page<Post> posts = postRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 10));
        assertEquals(2, posts.getTotalElements());
        assertEquals("Second post description.", posts.getContent().get(0).getDescription()); // Más reciente
    }

    @Test
    void testFindByUserIdOrderByCreatedAtDesc() {
        Page<Post> userPosts = postRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), PageRequest.of(0, 10));
        assertEquals(2, userPosts.getTotalElements());
        assertEquals("Second post description.", userPosts.getContent().get(0).getDescription()); // Más reciente
    }

    @Test
    void testUpdatePost() {
        post1.setDescription("Updated post description");
        post1.setLikes(15);
        postRepository.save(post1);

        Post updatedPost = postRepository.findById(post1.getId()).orElse(null);
        assertNotNull(updatedPost);
        assertEquals("Updated post description", updatedPost.getDescription());
        assertEquals(15, updatedPost.getLikes());
    }

    @Test
    void testDeletePost() {
        postRepository.delete(post1);
        Optional<Post> foundPost = postRepository.findById(post1.getId());
        assertFalse(foundPost.isPresent());
    }
}
