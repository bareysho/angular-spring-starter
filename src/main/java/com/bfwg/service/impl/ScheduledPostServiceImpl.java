package com.bfwg.service.impl;

import com.bfwg.model.ScheduledPost;
import com.bfwg.repository.ScheduledPostRepository;
import com.bfwg.service.ScheduledPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScheduledPostServiceImpl implements ScheduledPostService {

    @Autowired
    private ScheduledPostRepository scheduledPostRepository;

    @Override
    public ScheduledPost findById(Long id) {
        return scheduledPostRepository.findById(id);
    }

    @Override
    public void save(ScheduledPost scheduledPost) {
        scheduledPostRepository.save(scheduledPost);
    }

    @Override
    public List<ScheduledPost> findByUuid(String uuid) {
        return scheduledPostRepository.findByUuid(uuid);
    }

    @Override
    public List<ScheduledPost> findByDate(String date) {
        return scheduledPostRepository.findByDate(date);
    }

    @Override
    public void deleteById(Long id) {
        scheduledPostRepository.delete(id);
    }
}
