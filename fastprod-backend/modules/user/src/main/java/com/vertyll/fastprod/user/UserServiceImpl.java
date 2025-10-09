package com.vertyll.fastprod.user;

import static com.vertyll.fastprod.user.UserResponseDto.mapToDto;

import com.vertyll.fastprod.common.exception.ApiException;
import com.vertyll.fastprod.role.Role;
import com.vertyll.fastprod.role.RoleService;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponseDto createUser(UserCreateDto dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new ApiException("Email already exists", HttpStatus.BAD_REQUEST);
        }

        Set<Role> roles = new HashSet<>();
        if (dto.roleNames() != null && !dto.roleNames().isEmpty()) {
            for (String roleName : dto.roleNames()) {
                roles.add(roleService.getOrCreateDefaultRole(roleName));
            }
        } else {
            roles.add(roleService.getOrCreateDefaultRole("USER"));
        }

        User user = User
                .builder()
                .firstName(dto.firstName())
                .lastName(dto.lastName())
                .email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .roles(roles)
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);
        return mapToDto(savedUser);
    }

    @Override
    @Transactional
    public UserResponseDto updateUser(Long id, UserUpdateDto dto) {
        User user = userRepository.findById(id).orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        if (dto.firstName() != null) {
            user.setFirstName(dto.firstName());
        }

        if (dto.lastName() != null) {
            user.setLastName(dto.lastName());
        }

        if (dto.email() != null) {
            user.setEmail(dto.email());
        }

        if (dto.roleNames() != null) {
            Set<Role> roles = dto.roleNames().stream().map(roleService::getOrCreateDefaultRole).collect(Collectors.toSet());
            user.setRoles(roles);
        }

        User updatedUser = userRepository.save(user);
        return mapToDto(updatedUser);
    }

    @Override
    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        return mapToDto(user);
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
}
