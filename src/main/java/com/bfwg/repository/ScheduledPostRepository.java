package com.bfwg.repository;

import com.bfwg.model.ScheduledPost;
import com.bfwg.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduledPostRepository extends JpaRepository<ScheduledPost, Long> {
    ScheduledPost findById(Long id);

    List<ScheduledPost> findByUuid(String uuid);

    List<ScheduledPost> findByDate(String date);
}
