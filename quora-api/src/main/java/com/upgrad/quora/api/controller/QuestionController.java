package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.*;
import com.upgrad.quora.service.business.QuestionService;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/")
public class QuestionController {

    @Autowired private QuestionService questionService;

    /**
     * Create Question
     *
     * @param questionRequest
     * @param authorization
     * @return QuestionResponse
     * @throws AuthorizationFailedException
     */
    @PostMapping("/question/create")
    public ResponseEntity<QuestionResponse> createQuestion(
            QuestionRequest questionRequest, @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException {
        // Create new Question Entity
        QuestionEntity questionEntity = new QuestionEntity();
        questionEntity.setContent(questionRequest.getContent());
        questionEntity.setDate(ZonedDateTime.now());

        // Authorize the user
        final QuestionEntity createdQuestion =
                questionService.createQuestion(authorization, questionEntity);


        QuestionResponse questionResponse =
                new QuestionResponse().id(questionEntity.getUuid()).status("QUESTION CREATED");
        return new ResponseEntity<QuestionResponse>(questionResponse, HttpStatus.OK);
    }

    /**
     * Fetch all questions
     *
     * @param authorization
     * @return List of all questions
     * @throws AuthorizationFailedException
     */
    @GetMapping("/question/all")
    public ResponseEntity<List<QuestionDetailsResponse>> getAllQuestions(
            @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException {
        List<QuestionEntity> allQuestions = questionService.getAllQuestions(authorization);

        final List<QuestionDetailsResponse> questionResponseList = new ArrayList<>();

        // Extract Uuid and content from each QuestionResponse entity
        for (QuestionEntity question : allQuestions) {
            String uuid = question.getUuid();
            String content = question.getContent();
            questionResponseList.add(new QuestionDetailsResponse().id(uuid).content(content));
        }
        return new ResponseEntity<List<QuestionDetailsResponse>>(questionResponseList, HttpStatus.OK);
    }

    /**
     * update existing question
     *
     * @param questionEditRequest
     * @param questionUuid
     * @param authorization
     * @return QuestionEditResponse
     * @throws AuthorizationFailedException
     * @throws InvalidQuestionException
     */
    @PutMapping("/question/edit/{questionId}")
    public ResponseEntity<QuestionEditResponse> editQuestionContent(
            QuestionEditRequest questionEditRequest,
            @PathVariable("questionId") final String questionUuid,
            @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException, InvalidQuestionException {
        String content = questionEditRequest.getContent();

        QuestionEntity questionEntity =
                questionService.editQuestionContent(authorization, questionUuid, content);

        QuestionEditResponse questionEditResponse =
                new QuestionEditResponse().id(questionEntity.getUuid()).status("QUESTION EDITED");
        return new ResponseEntity<QuestionEditResponse>(questionEditResponse, HttpStatus.OK);
    }

    /**
     * Fetch all questions posted by a user
     *
     * @param accessToken
     * @param userId
     * @return List<QuestionDetailsResponse>
     * @throws AuthorizationFailedException
     * @throws UserNotFoundException
     */
    @GetMapping("question/all/{userId}")
    public ResponseEntity<List<QuestionDetailsResponse>> getQuestionByUserId(
            @RequestHeader("authorization") final String accessToken,
            @PathVariable("userId") String userId)
            throws AuthorizationFailedException, UserNotFoundException {
        List<QuestionEntity> questions = questionService.getAllQuestionsByUser(userId, accessToken);
        List<QuestionDetailsResponse> questionDetailResponses = new ArrayList<>();
        for (QuestionEntity questionEntity : questions) {
            QuestionDetailsResponse questionDetailResponse = new QuestionDetailsResponse();
            questionDetailResponse.setId(questionEntity.getUuid());
            questionDetailResponse.setContent(questionEntity.getContent());
            questionDetailResponses.add(questionDetailResponse);
        }
        return new ResponseEntity<List<QuestionDetailsResponse>>(
                questionDetailResponses, HttpStatus.OK);
    }

    /**
     * Delete question
     *
     * @param accessToken
     * @param questionId
     * @return
     * @throws AuthorizationFailedException
     * @throws InvalidQuestionException
     */
    @DeleteMapping("/question/delete/{questionId}")
    public ResponseEntity<QuestionDeleteResponse> deleteQuestion(
            @RequestHeader("authorization") final String accessToken,
            @PathVariable("questionId") final String questionId)
            throws AuthorizationFailedException, InvalidQuestionException {

        QuestionEntity questionEntity = questionService.deleteQuestion(accessToken, questionId);
        QuestionDeleteResponse questionDeleteResponse = new QuestionDeleteResponse();
        questionDeleteResponse.setId(questionEntity.getUuid());
        questionDeleteResponse.setStatus("QUESTION DELETED");
        return new ResponseEntity<QuestionDeleteResponse>(questionDeleteResponse, HttpStatus.OK);
    }
}
