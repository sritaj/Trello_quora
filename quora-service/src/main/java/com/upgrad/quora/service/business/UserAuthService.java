package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserAuthRepository;
import com.upgrad.quora.service.dao.UserRepository;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthenticationFailedException;
import com.upgrad.quora.service.exception.SignOutRestrictedException;
import com.upgrad.quora.service.exception.SignUpRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.UUID;

@Service
public class UserAuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAuthRepository userAuthRepository;

    @Autowired
    private PasswordCryptographyProvider passwordCryptographyProvider;

    /**
     *
     * @param userEntity
     * @return uuid of created user
     * @throws SignUpRestrictedException
     */
    public String signup(UserEntity userEntity) throws SignUpRestrictedException {
        if (isUserNameInUse(userEntity.getUserName())) {
            throw new SignUpRestrictedException(
                    "SGR-001", "Try any other Username, this Username has already been taken");
        }

        if (isEmailInUse(userEntity.getEmail())) {
            throw new SignUpRestrictedException(
                    "SGR-002", "This user has already been registered, try with any other emailId");
        }
        // Assign a UUID to the user that is being created.
        userEntity.setUuid(UUID.randomUUID().toString());
        // Assign encrypted password and salt to the user that is being created.
        String[] encryptedText = passwordCryptographyProvider.encrypt(userEntity.getPassword());
        userEntity.setSalt(encryptedText[0]);
        userEntity.setPassword(encryptedText[1]);
        UserEntity createdUserEntity = userRepository.save(userEntity);
        return createdUserEntity.getUuid();
    }

    /**
     * checks whether the username exist in the database
     * @param userName
     * @return boolean
     */
    private boolean isUserNameInUse(final String userName) {
        return null!= userRepository.findByUserName(userName);
    }

    /**
     * checks whether the email exist in the database
     * @param email
     * @return boolean
     */
    private boolean isEmailInUse(final String email) {
        return null!= userRepository.findByEmail(email);
    }

    /**
     * the signin user method
     *
     * @param username : Username that you want to signin
     * @param password : Password of user
     * @throws AuthenticationFailedException : If user not found or invalid password
     * @return UserAuthEntity access-token and singin response.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public UserAuthEntity signIn(final String username, final String password)
            throws AuthenticationFailedException {

        UserEntity userEntity = userRepository.findByUserName(username);
        if (userEntity == null) {
            throw new AuthenticationFailedException("ATH-001", "This username does not exist");
        }
        final String encryptedPassword =
                passwordCryptographyProvider.encrypt(password, userEntity.getSalt());
        if (!encryptedPassword.equals(userEntity.getPassword())) {
            throw new AuthenticationFailedException("ATH-002", "Password failed");
        }

        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(encryptedPassword);
        UserAuthEntity userAuthEntity = new UserAuthEntity();
        userAuthEntity.setUuid(UUID.randomUUID().toString());
        userAuthEntity.setUserEntity(userEntity);
        final ZonedDateTime now = ZonedDateTime.now();
        final ZonedDateTime expiresAt = now.plusHours(8);
        userAuthEntity.setAccessToken(
                jwtTokenProvider.generateToken(userEntity.getUuid(), now, expiresAt));
        userAuthEntity.setLoginAt(now);
        userAuthEntity.setExpiresAt(expiresAt);

        userAuthRepository.save(userAuthEntity);
        userRepository.save(userEntity);

        return userAuthEntity;
    }

    /**
     *
     * @param accessToken : required to signout the user
     * @throws SignOutRestrictedException : if the access-token is not found in the DB.
     * @return UserEntity : that user is signed out.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public UserEntity signOut(final String accessToken) throws SignOutRestrictedException {
        UserAuthEntity userAuthEntity = userAuthRepository.findByAccessToken(accessToken);
        if (userAuthEntity == null) {
            throw new SignOutRestrictedException("SGR-001", "User is not Signed in");
        }
        userAuthEntity.setLogoutAt(ZonedDateTime.now());
        userAuthRepository.save(userAuthEntity);
        return userAuthEntity.getUserEntity();
    }


}
