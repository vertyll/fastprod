package com.vertyll.fastprod.modules.auth.dto;

import lombok.Data;

public record LoginRequestDto(String email, String password) {

    @Data
    public static class FormBuilder {
        private String email;
        private String password;

        public LoginRequestDto toDto() {
            return new LoginRequestDto(email, password);
        }
    }
}
