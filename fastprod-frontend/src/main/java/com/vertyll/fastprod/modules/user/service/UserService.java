package com.vertyll.fastprod.modules.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vertyll.fastprod.modules.user.dto.*;
import com.vertyll.fastprod.shared.dto.ApiResponse;
import com.vertyll.fastprod.shared.service.BaseHttpService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UserService extends BaseHttpService {

    private static final String USER_ENDPOINT = "/users";

    public UserService(@Value("${api.backend.url}") String backendUrl, ObjectMapper objectMapper) {
        super(backendUrl, objectMapper);
    }

    public ApiResponse<UserProfileDto> getCurrentUser() throws Exception {
        return get(USER_ENDPOINT + "/me", UserProfileDto.class);
    }

    public ApiResponse<UserProfileDto> updateProfile(ProfileUpdateDto dto) throws Exception {
        return put(USER_ENDPOINT + "/me/profile", dto, UserProfileDto.class);
    }
}
