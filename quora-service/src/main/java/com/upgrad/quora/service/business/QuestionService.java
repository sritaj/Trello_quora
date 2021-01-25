package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.QuestionRepository;
import com.upgrad.quora.service.dao.UserAuthRepository;
import com.upgrad.quora.service.dao.UserRepository;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepo;

    @Autowired
    private UserAuthRepository userAuthRepo;

    @Autowired
    private UserRepository userRepo;

    /**
     * Business logic to authorize user who wants to create question and create a question
     *
     * @param authorization
     * @param questionEntity
     * @return QuestionEntity
     * @throws AuthorizationFailedException
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity createQuestion(String authorization, QuestionEntity questionEntity)
            throws AuthorizationFailedException {
        UserAuthEntity userAuthEntity = userAuthRepo.findByAccessToken(authorization);
        if (userAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        } else {
            // Retrieve logout_at attribute value of UserAuthEntity to check if user has already signed
            // out
            ZonedDateTime logoutAt = userAuthEntity.getLogoutAt();
            if (logoutAt != null) {
                throw new AuthorizationFailedException(
                        "ATHR-002", "User is signed out.Sign in first to post a question");
            } else {
                // Assign a UUID to the question that is being created.
                questionEntity.setUuid(UUID.randomUUID().toString());
                questionEntity.setUserEntity(userAuthEntity.getUserEntity());
                return questionRepo.save(questionEntity);
            }
        }
    }

    /**
     * Business logic to authorize user who wants to get a list of all questions and return list of
     * questions
     *
     * @param authorization
     * @return list of all questions
     * @throws AuthorizationFailedException
     */
    public List<QuestionEntity> getAllQuestions(final String authorization)
            throws AuthorizationFailedException {
        UserAuthEntity userAuthEntity = userAuthRepo.findByAccessToken(authorization);
        if (userAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        } else {
            // Retrieve logout_at attribute value of UserAuthEntity to check if user has already signed
            // out
            ZonedDateTime logoutAt = userAuthEntity.getLogoutAt();
            if (logoutAt != null) {
                throw new AuthorizationFailedException(
                        "ATHR-002", "User is signed out.Sign in first to get all questions");
            } else {
                return questionRepo.findAll();
            }
        }
    }

    /**
     * Business logic to check whether user is authorized to edit question and edit the question
     *
     * @param authorization
     * @param questionUuid
     * @return edited question
     * @throws AuthorizationFailedException
     * @throws InvalidQuestionException
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity editQuestionContent(
            final String authorization, String questionUuid, String content)
            throws AuthorizationFailedException, InvalidQuestionException {
        UserAuthEntity userAuthEntity = userAuthRepo.findByAccessToken(authorization);
        if (userAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        } else {
            // Retrieve logout_at attribute value of UserAuthEntity to check if user has already signed
            // out
            ZonedDateTime logoutAt = userAuthEntity.getLogoutAt();
            if (logoutAt != null) {
                throw new AuthorizationFailedException(
                        "ATHR-002", "User is signed out.Sign in first to edit the question");
            } else {
                // Get question by questionUuid passed by user
                QuestionEntity question = questionRepo.findQuestionByUuid(questionUuid);
                if (question == null) {
                    throw new InvalidQuestionException("QUES-001", "Entered question uuid does not exist");
                } else {
                    // Compare the userId to check if the user trying to edit question is the owner of the
                    // question
                    Integer questionEditorId = userAuthEntity.getUserEntity().getId();
                    Integer questionOwnerId = question.getUserEntity().getId();
                    if (!questionEditorId.equals(questionOwnerId)) {
                        throw new AuthorizationFailedException(
                                "ATHR-003", "Only the question owner can edit the question");
                    } else {
                        question.setContent(content);
                        return questionRepo.save(question);
                    }
                }
            }
        }
    }

    /**
     * Service method to get all questions posted by a user
     *
     * @param userId
     * @param accessToken
     * @return list of all posted questions by user
     * @throws AuthorizationFailedException
     * @throws UserNotFoundException
     */
    public List<QuestionEntity> getAllQuestionsByUser(final String userId, final String accessToken)
            throws AuthorizationFailedException, UserNotFoundException {
        UserAuthEntity userAuthEntity = userAuthRepo.findByAccessToken(accessToken);
        if (userAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        } else if (userAuthEntity.getLogoutAt() != null) {
            throw new AuthorizationFailedException(
                    "ATHR-002",
                    "User is signed out.Sign in first to get all questions posted by a specific user");
        }
        UserEntity user = userRepo.findByUuid(userId);
        if (user == null) {
            throw new UserNotFoundException(
                    "USR-001", "User with entered uuid whose question details are to be seen does not exist");
        }
        return questionRepo.findAllQuestionByUserEntity(user);
    }

    /**
     * Business logic to authorize user and delete question
     *
     * @param accessToken
     * @param questionId
     * @return deleted question
     * @throws AuthorizationFailedException
     * @throws InvalidQuestionException
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity deleteQuestion(final String accessToken, final String questionId)
            throws AuthorizationFailedException, InvalidQuestionException {
        UserAuthEntity userAuthEntity = userAuthRepo.findByAccessToken(accessToken);
        if (userAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        } else if (userAuthEntity.getLogoutAt() != null) {
            throw new AuthorizationFailedException(
                    "ATHR-002", "User is signed out.Sign in first to delete the question");
        }
        QuestionEntity questionEntity = questionRepo.findQuestionByUuid(questionId);
        if (questionEntity == null) {
            throw new InvalidQuestionException("QUES-001", "Entered question uuid does not exist");
        }
        if (!questionEntity.getUserEntity().getUuid().equals(userAuthEntity.getUserEntity().getUuid())
                && !userAuthEntity.getUserEntity().getRole().equals("admin")) {
            throw new AuthorizationFailedException(
                    "ATHR-003", "Only the question owner or admin can delete the question");
        }

        questionRepo.delete(questionEntity);
        return questionEntity;
    }
}
