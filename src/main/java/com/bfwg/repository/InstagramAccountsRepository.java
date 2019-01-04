package com.bfwg.repository;

import com.bfwg.model.InstagramAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InstagramAccountsRepository extends JpaRepository<InstagramAccount, Long> {
    InstagramAccount findByUuid(String uuid);

    List<InstagramAccount> findByOwner_Id(Long id);
}
