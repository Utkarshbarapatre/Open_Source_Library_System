package com.buzzword.osls.repository;

import com.buzzword.osls.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByResourceId(Long resourceId);
    List<Comment> findByUserId(Long userId);
}
