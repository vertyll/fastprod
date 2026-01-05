package com.vertyll.fastprod.modules.auth.dto;

import lombok.Data;

public record RegisterRequestDto(String firstName, String lastName, String email, String password) {

    @Data
    public static class FormBuilder {
        private String firstName;
        private String lastName;
        private String email;
        private String password;

        public RegisterRequestDto toDto() {
            return new RegisterRequestDto(firstName, lastName, email, password);
        }
    }
}
