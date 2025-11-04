package com.vertyll.fastprod.employee.service.impl;

import com.vertyll.fastprod.common.exception.ApiException;
import com.vertyll.fastprod.employee.dto.EmployeeCreateDto;
import com.vertyll.fastprod.employee.dto.EmployeeResponseDto;
import com.vertyll.fastprod.employee.dto.EmployeeUpdateDto;
import com.vertyll.fastprod.employee.mapper.EmployeeMapper;
import com.vertyll.fastprod.employee.service.EmployeeService;
import com.vertyll.fastprod.role.entity.Role;
import com.vertyll.fastprod.role.enums.RoleType;
import com.vertyll.fastprod.role.service.RoleService;
import com.vertyll.fastprod.user.entity.User;
import com.vertyll.fastprod.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class EmployeeServiceImpl implements EmployeeService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeMapper employeeMapper;

    @Override
    @Transactional
    public EmployeeResponseDto createEmployee(EmployeeCreateDto dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new ApiException("Email already exists", HttpStatus.BAD_REQUEST);
        }

        User user = employeeMapper.toUserEntity(dto);
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setVerified(true);

        assignRolesToUser(user, dto.roleNames());

        User savedUser = userRepository.save(user);
        return employeeMapper.toResponseDto(savedUser);
    }

    @Override
    @Transactional
    public EmployeeResponseDto updateEmployee(Long id, EmployeeUpdateDto dto) {
        User user = userRepository.findById(id).orElseThrow(() -> new ApiException("Employee not found", HttpStatus.NOT_FOUND));

        if (dto.email() != null && !dto.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(dto.email())) {
                throw new ApiException("Email already exists", HttpStatus.BAD_REQUEST);
            }
        }

        employeeMapper.updateUserFromDto(dto, user);

        if (dto.password() != null && !dto.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.password()));
        }

        if (dto.roleNames() != null && !dto.roleNames().isEmpty()) {
            user.getRoles().clear();
            assignRolesToUser(user, dto.roleNames());
        }

        User updatedUser = userRepository.save(user);
        return employeeMapper.toResponseDto(updatedUser);
    }

    @Override
    public EmployeeResponseDto getEmployeeById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ApiException("Employee not found", HttpStatus.NOT_FOUND));
        return employeeMapper.toResponseDto(user);
    }

    @Override
    public List<EmployeeResponseDto> getAllEmployees() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRoles().stream()
                        .anyMatch(role -> role.getName().equals(RoleType.EMPLOYEE.name())))
                .map(employeeMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    private void assignRolesToUser(User user, java.util.Set<String> roleNames) {
        if (roleNames != null && !roleNames.isEmpty()) {
            roleNames.forEach(roleName -> {
                Role role = roleService.getOrCreateDefaultRole(roleName);
                user.getRoles().add(role);
            });
        } else {
            Role employeeRole = roleService.getOrCreateDefaultRole(RoleType.EMPLOYEE.name());
            user.getRoles().add(employeeRole);
        }
    }
}
