package com.upgrad.quora.api.controller;


import com.upgrad.quora.api.model.UserDeleteResponse;
import com.upgrad.quora.service.business.AdminService;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/*This class implements the userDelete - "/admin/user/{userId}"*/
@RestController
@RequestMapping("/")
public class AdminController {

    @Autowired
    private AdminService adminService;

    /*This endpoint is used to delete a user from the Quora application if the user has signed in and has valid user access token
    and has admin role. If any of these conditions fail, the corresponding exception is thrown.
    This endpoint (a DELETE request), requests path variable userId as a string for the corresponding user that needs
    to be deleted and access token of the signed in user as a String in authorization Request Header. It returns the uuid
    of the user that has been deleted and message in the JSON response with the corresponding HTTP status*/

    /**
     * Get the user detail by user id.
     *
     * @param userId : user id of the user
     * @param accessToken : access-token to authenticate the user
     * @throws AuthorizationFailedException : user authentication exception
     * @throws UserNotFoundException : will through a user not found exception
     * @return UserDeleteResponse
     */
    @DeleteMapping(path = "/admin/user/{userId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UserDeleteResponse> deleteUser(@PathVariable("userId") final String userId,
                                                         @RequestHeader("authorization") final String accessToken) throws AuthorizationFailedException, UserNotFoundException {
        UserEntity deletedUser = adminService.deleteUser(userId, accessToken);
        UserDeleteResponse userDeleteResponse =
                new UserDeleteResponse().id(deletedUser.getUuid()).status("USER SUCCESSFULLY DELETED");
        return new ResponseEntity<UserDeleteResponse>(userDeleteResponse, HttpStatus.OK);
    }
}
