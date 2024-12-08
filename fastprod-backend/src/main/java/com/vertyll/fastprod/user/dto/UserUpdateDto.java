package com.vertyll.fastprod.user.dto;

import lombok.Data;

import java.util.Set;

@Data
public class UserUpdateDto {
    private String firstName;
    private String lastName;
    private String email;
    private Set<String> roleNames;
}