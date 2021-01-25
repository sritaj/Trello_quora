package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserAuthRepository;
import com.upgrad.quora.service.dao.UserRepository;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    @Autowired
    private UserAuthRepository authRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Delete user endpoint
     *
     * @param userId      : userId of which you want to delete
     * @param accessToken : access-token for authorization
     * @throws AuthorizationFailedException : If token is invalid you get authorization failed
     *                                      response
     * @throws UserNotFoundException        : If userid is invalid or not found
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public UserEntity deleteUser(final String userId, final String accessToken)
            throws AuthorizationFailedException, UserNotFoundException {
        UserAuthEntity userAuthEntity = this.authRepository.findByAccessToken(accessToken);

        if (userAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }

        if (userAuthEntity.getLogoutAt() != null) {
            throw new AuthorizationFailedException("ATHR-002", "User is signed out");
        }

        if (!userAuthEntity.getUserEntity().getRole().equals("admin")) {
            throw new AuthorizationFailedException(
                    "ATHR-003", "Unauthorized Access, Entered user is not an admin");
        }

        UserEntity existingUser = this.userRepository.findByUuid(userId);

        if (existingUser == null) {
            throw new UserNotFoundException(
                    "USR-001", "User with entered uuid to be deleted does not exist");
        }
        this.userRepository.delete(existingUser);
        return existingUser;
    }
}
