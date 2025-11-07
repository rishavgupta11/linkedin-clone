package com.Assignemnt.Linkedin.Clone.controller;

import com.Assignemnt.Linkedin.Clone.model.*;
import com.Assignemnt.Linkedin.Clone.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class CommentController {

    private final CommentRepository commentRepo;
    private final UserRepository userRepo;
    private final PostRepository postRepo;

    // Add Comment (Session-based)
    @PostMapping("/{postId}")
    public ResponseEntity<?> addComment(@PathVariable Long postId,
                                        @RequestBody Map<String, String> body,
                                        HttpSession session) {
        String content = body.get("content");

        // Validate content
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Comment cannot be empty"));
        }

        // Get logged-in user email from session
        Object emailObj = session.getAttribute("USER_EMAIL");
        if (emailObj == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "You must be logged in to comment"));
        }

        String email = emailObj.toString();
        User user = userRepo.findByEmail(email).orElseThrow();
        Post post = postRepo.findById(postId).orElseThrow();

        Comment comment = Comment.builder()
                .content(content.trim())
                .user(user)
                .post(post)
                .createdAt(LocalDateTime.now())
                .build();

        commentRepo.save(comment);
        return ResponseEntity.ok(Map.of("message", "Comment added", "comment", comment));
    }

    // Get Comments for a Post
    @GetMapping("/{postId}")
    public ResponseEntity<?> getComments(@PathVariable Long postId) {
        Post post = postRepo.findById(postId).orElseThrow();
        var comments = commentRepo.findAllByPostOrderByCreatedAtAsc(post);

        var response = comments.stream().map(c -> Map.of(
                "id", c.getId(),
                "userName", c.getUser().getName(),
                "content", c.getContent(),
                "createdAt", c.getCreatedAt()
        ));
        return ResponseEntity.ok(response);
    }
}
