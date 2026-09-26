package com.vertyll.fastprod.modules.auth.dto;

public record LoginRequestDto(String email, String password) {
    @SuppressWarnings("PMD.DataClass")
    public static class FormBuilder {
        private String email = "";
        private String password = "";

        public LoginRequestDto toDto() {
            return new LoginRequestDto(email, password);
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
