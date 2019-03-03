package com.bfwg.service;

import com.bfwg.model.InstagramAccount;
import com.bfwg.model.ScheduledPost;

import java.util.List;

public interface ScheduledPostService {
    ScheduledPost findById(Long id);

    void save(ScheduledPost scheduledPost);

    List<ScheduledPost> findByUuid(String uuid);

    List<ScheduledPost> findByDate(String date);

    void deleteById(Long id);
}
