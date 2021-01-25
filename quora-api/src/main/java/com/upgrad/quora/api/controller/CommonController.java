package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.UserDetailsResponse;
import com.upgrad.quora.service.business.CommonService;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class CommonController {

    @Autowired private CommonService commonService;

    /**
     * Get userProfile
     *
     * @param userUuid
     * @param authorization
     * @return User profile of a user
     * @throws AuthorizationFailedException
     * @throws UserNotFoundException
     */
    @GetMapping(
            path = "/userprofile/{userId}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UserDetailsResponse> getUserProfile(
            @PathVariable("userId") final String userUuid,
            @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException, UserNotFoundException {

        UserAuthEntity userAuthEntity = commonService.authorizeUser(authorization);

        UserEntity existingUser = commonService.getUserByUuid(userUuid);

        UserDetailsResponse userDetailsResponse = new UserDetailsResponse();
        userDetailsResponse
                .firstName(existingUser.getFirstName())
                .lastName(existingUser.getLastName())
                .userName(existingUser.getUserName())
                .emailAddress(existingUser.getEmail())
                .country(existingUser.getCountry())
                .aboutMe(existingUser.getAboutMe())
                .dob(existingUser.getDob())
                .contactNumber(existingUser.getContactNumber());
        return new ResponseEntity<UserDetailsResponse>(userDetailsResponse, HttpStatus.OK);
    }
}
