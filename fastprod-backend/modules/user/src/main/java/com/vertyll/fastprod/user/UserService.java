package com.vertyll.fastprod.user;

import com.vertyll.fastprod.user.dto.UserCreateDto;
import com.vertyll.fastprod.user.dto.UserResponseDto;
import com.vertyll.fastprod.user.dto.UserUpdateDto;
import com.vertyll.fastprod.user.entity.User;

import java.util.Optional;

public interface UserService {
    UserResponseDto createUser(UserCreateDto dto);

    UserResponseDto updateUser(Long id, UserUpdateDto dto);

    UserResponseDto getUserById(Long id);

    boolean existsByEmail(String email);

    User saveUser(User user);

    Optional<User> findByEmailWithRoles(String email);
}
