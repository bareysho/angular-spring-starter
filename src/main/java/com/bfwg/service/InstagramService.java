package com.bfwg.service;

import com.bfwg.model.InstagramAccount;

import java.util.List;

public interface InstagramService {
    InstagramAccount findByUuid(String uuid);

    void save(InstagramAccount instagramAccount);

    List<InstagramAccount> findByOwner_id(Long id);
}
