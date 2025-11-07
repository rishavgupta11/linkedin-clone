package com.Assignemnt.Linkedin.Clone.controller;

import com.Assignemnt.Linkedin.Clone.model.*;
import com.Assignemnt.Linkedin.Clone.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:63342", allowCredentials = "true")
public class LikeController {

    private final LikeRepository likeRepo;
    private final PostRepository postRepo;
    private final UserRepository userRepo;

    // ✅ Toggle Like (Like or Unlike)
    @PostMapping("/{postId}")
    public ResponseEntity<?> toggleLike(@PathVariable Long postId, HttpSession session) {
        Object emailObj = session.getAttribute("USER_EMAIL");
        if (emailObj == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "You must be logged in to like posts"));
        }

        String email = emailObj.toString();
        User user = userRepo.findByEmail(email).orElseThrow();
        Post post = postRepo.findById(postId).orElseThrow();

        var existing = likeRepo.findByUserAndPost(user, post);

        if (existing.isPresent()) {
            // Unlike
            likeRepo.delete(existing.get());
            return ResponseEntity.ok(Map.of("message", "Unliked post"));
        } else {
            // Like
            Like like = Like.builder()
                    .user(user)
                    .post(post)
                    .build();
            likeRepo.save(like);
            return ResponseEntity.ok(Map.of("message", "Post liked"));
        }
    }

    // ✅ Get likes count for a post
    @GetMapping("/{postId}/count")
    public ResponseEntity<?> countLikes(@PathVariable Long postId) {
        Post post = postRepo.findById(postId).orElseThrow();
        long count = likeRepo.countByPost(post);
        return ResponseEntity.ok(Map.of("likes", count));
    }
}
