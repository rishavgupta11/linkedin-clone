package com.Assignemnt.Linkedin.Clone.repository;

import com.Assignemnt.Linkedin.Clone.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByUserAndPost(User user, Post post);
    long countByPost(Post post);
    void deleteAllByPost(Post post);
}
