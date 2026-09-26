package com.vertyll.fastprod.modules.user.service;

import org.springframework.stereotype.Service;

import com.vertyll.fastprod.modules.user.dto.ProfileUpdateDto;
import com.vertyll.fastprod.modules.user.dto.UserProfileDto;
import com.vertyll.fastprod.shared.config.BackendApiProperties;
import com.vertyll.fastprod.shared.security.AuthTokenProvider;
import com.vertyll.fastprod.shared.service.BaseHttpService;

import tools.jackson.databind.ObjectMapper;

import static java.util.Objects.requireNonNull;

@Service
public class UserService extends BaseHttpService {

    private static final String USER_ENDPOINT = "/users";

    public UserService(
        BackendApiProperties backendApi,
        ObjectMapper objectMapper,
        AuthTokenProvider authTokenProvider
    ) {
        super(backendApi.url(), objectMapper, authTokenProvider);
    }

    public UserProfileDto getCurrentUser() {
        return requireNonNull(get(USER_ENDPOINT + "/me", UserProfileDto.class));
    }

    public UserProfileDto updateProfile(ProfileUpdateDto dto) {
        return requireNonNull(put(USER_ENDPOINT + "/me/profile", dto, UserProfileDto.class));
    }
}
