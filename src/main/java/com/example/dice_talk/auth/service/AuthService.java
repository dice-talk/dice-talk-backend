package com.example.dice_talk.auth.service;


import com.example.dice_talk.auth.jwt.JwtTokenizer;
import com.example.dice_talk.exception.BusinessLogicException;
import com.example.dice_talk.exception.ExceptionCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {
    private final JwtTokenizer jwtTokenizer;

    public AuthService(JwtTokenizer jwtTokenizer) {
        this.jwtTokenizer = jwtTokenizer;
    }

    public void logout(String username){
        boolean isDeleted = jwtTokenizer.deleteRegisterToken(username);
        if(!isDeleted){
            throw new BusinessLogicException(ExceptionCode.LOGOUT_FAILED);
        }
    }
}
