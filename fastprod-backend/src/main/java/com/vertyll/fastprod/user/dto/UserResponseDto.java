package com.vertyll.fastprod.user.dto;

import lombok.Data;

import java.util.Set;

@Data
public class UserResponseDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Set<String> roles;
    private boolean enabled;
}