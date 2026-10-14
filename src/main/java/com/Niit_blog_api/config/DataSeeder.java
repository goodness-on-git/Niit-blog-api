package com.Niit_blog_api.config;

import java.util.List;

import com.Niit_blog_api.post.Post;
import com.Niit_blog_api.post.PostRepository;
import com.Niit_blog_api.user.User;
import com.Niit_blog_api.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Loads sample data on first startup, only when the users table is empty. */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final String SEED_PASSWORD = "password123";

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PostRepository postRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        String hash = passwordEncoder.encode(SEED_PASSWORD);

        List<User> users = userRepository.saveAll(List.of(
                user("ada", "Ada L.", "Writing about Java and math.", hash),
                user("grace", "Grace H.", "Compilers and good habits.", hash),
                user("linus", "Linus T.", "Kernels and version control.", hash),
                user("margaret", "Margaret H.", "Software that has to work.", hash),
                user("alan", "Alan T.", "Thinking about thinking machines.", hash),
                user("katherine", "Katherine J.", "Numbers, orbits, and accuracy.", hash),
                user("dennis", "Dennis R.", "Small tools, sharp edges.", hash),
                user("barbara", "Barbara L.", "Abstractions that hold up.", hash)
        ));

        postRepository.saveAll(List.of(
                post("Why dependency injection finally clicked", "Passing dependencies in "
                        + "instead of creating them inside a class made my code much easier to test.", users.get(0)),
                post("Debugging is a skill", "Read the error from the top, find the first line "
                        + "that is your code, and start there.", users.get(1)),
                post("Small commits save lives", "Commit one idea at a time. Your future self "
                        + "will thank you when something breaks.", users.get(2)),
                post("Write it down", "If a decision lives only in your head, it will be gone "
                        + "next week. Put it in a note.", users.get(3)),
                post("Start with the simplest thing", "Get the happy path working first, then "
                        + "handle the errors, then make it pretty.", users.get(4))
        ));
    }

    private User user(String username, String displayName, String bio, String hash) {
        User u = new User();
        u.setUsername(username);
        u.setEmail(username + "@example.com");
        u.setPasswordHash(hash);
        u.setDisplayName(displayName);
        u.setBio(bio);
        return u;
    }

    private Post post(String title, String body, User author) {
        Post p = new Post();
        p.setTitle(title);
        p.setBody(body);
        p.setAuthor(author);
        return p;
    }
}
