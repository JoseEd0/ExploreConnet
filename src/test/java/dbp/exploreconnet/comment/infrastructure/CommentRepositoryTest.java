package dbp.exploreconnet.comment.infrastructure;

import dbp.exploreconnet.AbstractContainerBaseTest;
import dbp.exploreconnet.comment.domain.Comment;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CommentRepositoryTest extends AbstractContainerBaseTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private User user;
    private Place place;
    private Post post;
    private Comment comment1;
    private Comment comment2;

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

        // Crear un Post asociado al Place
        post = new Post();
        post.setDescription("This is a sample post description.");
        post.setLikes(10);
        post.setCreatedAt(LocalDateTime.now());
        post.setUser(user);
        post.setPlace(place); // Asignar el Place al Post
        testEntityManager.persist(post);

        // Crear dos Comments asociados al Post
        comment1 = new Comment();
        comment1.setContent("Great post!");
        comment1.setLikes(5);
        comment1.setCreatedAt(LocalDateTime.now());
        comment1.setUser(user);
        comment1.setPost(post);

        comment2 = new Comment();
        comment2.setContent("Interesting perspective.");
        comment2.setLikes(2);
        comment2.setCreatedAt(LocalDateTime.now());
        comment2.setUser(user);
        comment2.setPost(post);

        testEntityManager.persist(comment1);
        testEntityManager.persist(comment2);
        testEntityManager.flush();
    }

    @Test
    void testSaveComment() {
        Comment newComment = new Comment();
        newComment.setContent("Nice article!");
        newComment.setLikes(3);
        newComment.setCreatedAt(LocalDateTime.now());
        newComment.setUser(user);
        newComment.setPost(post);

        Comment savedComment = commentRepository.save(newComment);
        assertNotNull(savedComment.getId());
        assertEquals("Nice article!", savedComment.getContent());
        assertEquals(3, savedComment.getLikes());
        assertEquals(user.getId(), savedComment.getUser().getId());
        assertEquals(post.getId(), savedComment.getPost().getId());
    }

    @Test
    void testFindById() {
        Optional<Comment> foundComment = commentRepository.findById(comment1.getId());
        assertTrue(foundComment.isPresent());
        assertEquals("Great post!", foundComment.get().getContent());
        assertEquals(5, foundComment.get().getLikes());
    }

    @Test
    void testFindAll() {
        var comments = commentRepository.findAll();
        assertEquals(2, comments.size());
    }

    @Test
    void testFindByPostId() {
        Page<Comment> commentsPage = commentRepository.findByPostId(post.getId(), PageRequest.of(0, 10));
        assertEquals(2, commentsPage.getTotalElements());
        assertEquals("Great post!", commentsPage.getContent().get(0).getContent());
    }

    @Test
    void testUpdateComment() {
        comment1.setContent("Updated comment content");
        comment1.setLikes(10);
        commentRepository.save(comment1);

        Comment updatedComment = commentRepository.findById(comment1.getId()).orElse(null);
        assertNotNull(updatedComment);
        assertEquals("Updated comment content", updatedComment.getContent());
        assertEquals(10, updatedComment.getLikes());
    }

    @Test
    void testDeleteComment() {
        commentRepository.delete(comment1);
        Optional<Comment> foundComment = commentRepository.findById(comment1.getId());
        assertFalse(foundComment.isPresent());
    }
}
