package com.vertyll.fastprod.employee.service.impl;

import static com.vertyll.fastprod.employee.dto.EmployeeResponseDto.mapToDto;

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

        Role employeeRole = roleService.getOrCreateDefaultRole(RoleType.EMPLOYEE.name());

        User user = employeeMapper.toUserEntity(dto);
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setVerified(true);
        user.getRoles().add(employeeRole);

        User savedUser = userRepository.save(user);
        return mapToDto(savedUser);
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

        if (dto.password() != null) {
            user.setPassword(passwordEncoder.encode(dto.password()));
        }

        User updatedUser = userRepository.save(user);
        return mapToDto(updatedUser);
    }

    @Override
    public EmployeeResponseDto getEmployeeById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ApiException("Employee not found", HttpStatus.NOT_FOUND));
        return mapToDto(user);
    }
}
