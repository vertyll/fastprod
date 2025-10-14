package com.vertyll.fastprod.user.service;

import com.vertyll.fastprod.user.dto.UserCreateDto;
import com.vertyll.fastprod.user.dto.UserResponseDto;
import com.vertyll.fastprod.user.dto.UserUpdateDto;
import com.vertyll.fastprod.user.entity.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface UserService {
    @Transactional
    UserResponseDto createUser(UserCreateDto dto);

    @Transactional
    UserResponseDto updateUser(Long id, UserUpdateDto dto);

    UserResponseDto getUserById(Long id);

    boolean existsByEmail(String email);

    @Transactional
    User saveUser(User user);

    Optional<User> findByEmailWithRoles(String email);
}
