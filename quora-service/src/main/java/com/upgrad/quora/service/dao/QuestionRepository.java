package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *  QuestionRepository, used to perform CRUD operation on QuestionEntity
 */
@Repository
public interface QuestionRepository  extends JpaRepository<QuestionEntity, Integer> {
    QuestionEntity findQuestionByUuid(String uuid);
    List<QuestionEntity> findAllQuestionByUserEntity(UserEntity user);
}
