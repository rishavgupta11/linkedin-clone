package com.Assignemnt.Linkedin.Clone.repository;

import com.Assignemnt.Linkedin.Clone.model.Comment;
import com.Assignemnt.Linkedin.Clone.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByPostOrderByCreatedAtAsc(Post post);
}