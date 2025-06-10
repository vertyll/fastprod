package com.vertyll.fastprod.user;

import java.util.Optional;

public interface UserService {
    UserResponseDto createUser(UserCreateDto dto);
    UserResponseDto updateUser(Long id, UserUpdateDto dto);
    UserResponseDto getUserById(Long id);
    boolean existsByEmail(String email);
    User saveUser(User user);
    Optional<User> findByEmailWithRoles(String email);
}
