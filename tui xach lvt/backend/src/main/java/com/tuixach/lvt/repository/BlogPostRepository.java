package com.tuixach.lvt.repository;

import com.tuixach.lvt.entity.BlogPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {

    Page<BlogPost> findByStatusOrderByPublishedAtDesc(BlogPost.Status status, Pageable pageable);

    Page<BlogPost> findAllByOrderByUpdatedAtDesc(Pageable pageable);

    Page<BlogPost> findByStatusOrderByUpdatedAtDesc(BlogPost.Status status, Pageable pageable);

    Optional<BlogPost> findBySlugAndStatus(String slug, BlogPost.Status status);

    Optional<BlogPost> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
