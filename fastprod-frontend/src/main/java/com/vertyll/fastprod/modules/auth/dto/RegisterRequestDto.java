package com.vertyll.fastprod.modules.auth.dto;

public record RegisterRequestDto(String firstName, String lastName, String email, String password) {
    @SuppressWarnings("PMD.DataClass")
    public static class FormBuilder {
        private String firstName = "";
        private String lastName = "";
        private String email = "";
        private String password = "";

        public RegisterRequestDto toDto() {
            return new RegisterRequestDto(firstName, lastName, email, password);
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
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
