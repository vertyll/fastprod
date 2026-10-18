package com.vertyll.fastprod.modules.user.service;

import org.springframework.stereotype.Component;

import com.vertyll.fastprod.base.ui.component.SignedInPerson;
import com.vertyll.fastprod.modules.user.dto.UserProfileDto;

@Component
class SignedInUser implements SignedInPerson {

    private final UserService users;

    SignedInUser(UserService users) {
        this.users = users;
    }

    @Override
    public Profile current() {
        UserProfileDto user = users.getCurrentUser();
        return new Profile(user.firstName() + " " + user.lastName(), user.email());
    }
}
