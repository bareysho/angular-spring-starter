package com.bfwg.service.impl;

import com.bfwg.model.InstagramAccount;
import com.bfwg.repository.InstagramAccountsRepository;
import com.bfwg.repository.UserRepository;
import com.bfwg.service.InstagramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InstagramServiceImpl implements InstagramService {

    @Autowired
    private InstagramAccountsRepository instagramAccountsRepository;

    @Override
    public InstagramAccount findByUuid(String uuid) {
        return instagramAccountsRepository.findByUuid(uuid);
    }

    @Override
    public void save(InstagramAccount instagramAccount) {
        this.instagramAccountsRepository.save(instagramAccount);
    }

    @Override
    public List<InstagramAccount> findByOwner_id(Long id) {
        return this.instagramAccountsRepository.findByOwner_Id(id);
    }
}
