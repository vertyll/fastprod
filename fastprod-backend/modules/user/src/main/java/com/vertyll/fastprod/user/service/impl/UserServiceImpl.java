package com.vertyll.fastprod.user.service.impl;

import com.vertyll.fastprod.common.exception.ApiException;
import com.vertyll.fastprod.role.entity.Role;
import com.vertyll.fastprod.role.service.RoleService;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.vertyll.fastprod.user.dto.UserCreateDto;
import com.vertyll.fastprod.user.dto.UserResponseDto;
import com.vertyll.fastprod.user.dto.UserUpdateDto;
import com.vertyll.fastprod.user.dto.ProfileUpdateDto;
import com.vertyll.fastprod.user.entity.User;
import com.vertyll.fastprod.user.mapper.UserMapper;
import com.vertyll.fastprod.user.repository.UserRepository;
import com.vertyll.fastprod.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.vertyll.fastprod.common.enums.RoleType;

@Service
@RequiredArgsConstructor
class UserServiceImpl implements UserService {

    private static final String USER_NOT_FOUND_MESSAGE = "User not found";
    private static final String EMAIL_ALREADY_EXISTS = "Email already exists";

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponseDto createUser(UserCreateDto dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new ApiException(EMAIL_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
        }

        Set<Role> roles = new HashSet<>();
        Set<RoleType> roleNames = dto.roleNames();
        if (roleNames != null && !roleNames.isEmpty()) {
            for (RoleType roleName : roleNames) {
                roles.add(roleService.getOrCreateDefaultRole(roleName));
            }
        } else {
            roles.add(roleService.getOrCreateDefaultRole(RoleType.USER));
        }

        User user = userMapper.toEntity(dto);
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setRoles(roles);
        user.setVerified(true);

        User savedUser = userRepository.save(user);
        return userMapper.toResponseDto(savedUser);
    }

    @Override
    @Transactional
    public UserResponseDto updateUser(Long id, UserUpdateDto dto) {
        User user = userRepository.findById(id).orElseThrow(() -> new ApiException(USER_NOT_FOUND_MESSAGE, HttpStatus.NOT_FOUND));

        String email = dto.email();
        if (email != null && !email.equals(user.getEmail()) && userRepository.existsByEmail(email)) {
            throw new ApiException(EMAIL_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
        }

        userMapper.updateFromDto(dto, user);

        String password = dto.password();
        if (password != null) {
            user.setPassword(passwordEncoder.encode(password));
        }

        Set<RoleType> roleNames = dto.roleNames();
        if (roleNames != null) {
            Set<Role> roles = roleNames.stream()
                    .map(roleService::getOrCreateDefaultRole)
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }

        User updatedUser = userRepository.save(user);
        return userMapper.toResponseDto(updatedUser);
    }

    @Override
    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ApiException(USER_NOT_FOUND_MESSAGE, HttpStatus.NOT_FOUND));
        return userMapper.toResponseDto(user);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findByEmailWithRoles(String email) {
        return userRepository.findByEmailWithRoles(email);
    }

    @Override
    public UserResponseDto getCurrentUser(String email) {
        User user = userRepository.findByEmailWithRoles(email)
                .orElseThrow(() -> new ApiException(USER_NOT_FOUND_MESSAGE, HttpStatus.NOT_FOUND));
        return userMapper.toResponseDto(user);
    }

    @Override
    @Transactional
    public UserResponseDto updateCurrentUserProfile(String email, ProfileUpdateDto dto) {
        User user = userRepository.findByEmailWithRoles(email)
                .orElseThrow(() -> new ApiException(USER_NOT_FOUND_MESSAGE, HttpStatus.NOT_FOUND));

        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());

        User updatedUser = userRepository.save(user);
        return userMapper.toResponseDto(updatedUser);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
