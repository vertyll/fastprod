package com.vertyll.fastprod.modules.user.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.vertyll.fastprod.modules.user.dto.ProfileUpdateDto;
import com.vertyll.fastprod.modules.user.dto.UserProfileDto;
import com.vertyll.fastprod.shared.security.AuthTokenProvider;
import com.vertyll.fastprod.shared.service.BaseHttpService;

import tools.jackson.databind.ObjectMapper;

import static java.util.Objects.requireNonNull;

@Service
public class UserService extends BaseHttpService {

    private static final String USER_ENDPOINT = "/users";

    public UserService(
        @Value("${api.backend.url}") String backendUrl,
        ObjectMapper objectMapper,
        AuthTokenProvider authTokenProvider
    ) {
        super(backendUrl, objectMapper, authTokenProvider);
    }

    public UserProfileDto getCurrentUser() {
        return requireNonNull(get(USER_ENDPOINT + "/me", UserProfileDto.class));
    }

    public UserProfileDto updateProfile(ProfileUpdateDto dto) {
        return requireNonNull(put(USER_ENDPOINT + "/me/profile", dto, UserProfileDto.class));
    }
}
