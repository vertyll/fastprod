package com.vertyll.fastprod.user.service;

import org.springframework.security.oauth2.jwt.Jwt;

import com.vertyll.fastprod.user.dto.ProfileUpdateDto;
import com.vertyll.fastprod.user.dto.UserResponseDto;

public interface UserService {
    UserResponseDto currentUser(Jwt token);

    UserResponseDto updateCurrentUserProfile(Jwt token, ProfileUpdateDto dto);
}
