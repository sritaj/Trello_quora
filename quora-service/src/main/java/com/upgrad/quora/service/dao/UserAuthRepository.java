package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.UserAuthEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface  UserAuthRepository extends JpaRepository<UserAuthEntity, Integer> {
    UserAuthEntity findByAccessToken(String accessToken);
}