package com.Assignemnt.Linkedin.Clone.controller;

import com.Assignemnt.Linkedin.Clone.model.Post;
import com.Assignemnt.Linkedin.Clone.repository.PostRepository;
import com.Assignemnt.Linkedin.Clone.repository.UserRepository;
import com.Assignemnt.Linkedin.Clone.repository.CommentRepository;
import com.Assignemnt.Linkedin.Clone.repository.LikeRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class PostController {

    private final PostRepository postRepo;
    private final UserRepository userRepo;
    private final CommentRepository commentRepo;
    private final LikeRepository likeRepo;

    // Create a new post (requires login session)
    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody Map<String, String> body, HttpSession session) {
        String content = body.get("content");
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Post content cannot be empty"));
        }

        Object emailObj = session.getAttribute("USER_EMAIL");
        if (emailObj == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not logged in"));

        String email = emailObj.toString();
        var user = userRepo.findByEmail(email).orElseThrow();

        Post post = Post.builder()
                .user(user)
                .content(content.trim())
                .createdAt(LocalDateTime.now())
                .build();

        postRepo.save(post);
        return ResponseEntity.ok(Map.of("message", "Post created successfully", "post", post));
    }

    // Get all posts (public feed)
    @GetMapping
    public ResponseEntity<?> getAllPosts() {
        var posts = postRepo.findAllByOrderByCreatedAtDesc();

        var response = posts.stream()
                .map(p -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", p.getId());
                    m.put("userName", p.getUser() != null ? p.getUser().getName() : null);
                    m.put("userId", p.getUser() != null ? p.getUser().getId() : null);
                    m.put("content", p.getContent());
                    m.put("createdAt", p.getCreatedAt());
                    m.put("updatedAt", p.getUpdatedAt()); // may be null — allowed here
                    return m;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // Edit your own post
    @PutMapping("/{id}")
    public ResponseEntity<?> editPost(@PathVariable Long id,
                                      @RequestBody Map<String, String> body,
                                      HttpSession session) {

        Object emailObj = session.getAttribute("USER_EMAIL");
        if (emailObj == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not logged in"));

        String email = emailObj.toString();
        var post = postRepo.findById(id).orElse(null);
        if (post == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Post not found"));

        if (!post.getUser().getEmail().equals(email))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You can edit only your posts"));

        post.setContent(body.get("content"));
        post.setUpdatedAt(LocalDateTime.now());
        postRepo.save(post);

        return ResponseEntity.ok(Map.of("message", "Post updated successfully", "post", post));
    }

    // Delete your own post
//    @DeleteMapping("/{id}")
//    public ResponseEntity<?> deletePost(@PathVariable Long id, HttpSession session) {
//        Object emailObj = session.getAttribute("USER_EMAIL");
//        if (emailObj == null)
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not logged in"));
//
//        String email = emailObj.toString();
//        var post = postRepo.findById(id).orElse(null);
//        if (post == null)
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Post not found"));
//
//        if (!post.getUser().getEmail().equals(email))
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You can delete only your posts"));
//
//        // With cascade, this will automatically delete comments and likes
//        postRepo.delete(post);
//
//        return ResponseEntity.ok(Map.of("message", "Post deleted successfully"));
//    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id, HttpSession session) {
        //  Check authentication
        Object emailObj = session.getAttribute("USER_EMAIL");
        if (emailObj == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "You must be logged in"));
        }

        String email = emailObj.toString();

        //  Find post by ID
        var post = postRepo.findById(id).orElse(null);
        if (post == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Post not found"));
        }

        //  Ensure user owns the post
        if (!post.getUser().getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You can delete only your posts"));
        }

        //  Delete the post
        postRepo.delete(post);

        return ResponseEntity.ok(Map.of("message", "Post deleted successfully"));
    }

}