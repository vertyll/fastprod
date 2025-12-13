package com.vertyll.fastprod.employee.service.impl;

import com.vertyll.fastprod.common.exception.ApiException;
import com.vertyll.fastprod.employee.dto.EmployeeCreateDto;
import com.vertyll.fastprod.employee.dto.EmployeeResponseDto;
import com.vertyll.fastprod.employee.dto.EmployeeFilterDto;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException("Employee not found", HttpStatus.NOT_FOUND));

        if (!user.isActive()) {
            throw new ApiException("Cannot update inactive employee", HttpStatus.BAD_REQUEST);
        }

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
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException("Employee not found", HttpStatus.NOT_FOUND));

        if (!user.isActive()) {
            throw new ApiException("Employee not found", HttpStatus.NOT_FOUND);
        }

        return employeeMapper.toResponseDto(user);
    }

    @Override
    public Page<EmployeeResponseDto> getAllEmployees(EmployeeFilterDto filterDto) {
        Pageable pageable = filterDto.toPageable();

        Specification<User> spec = Specification.where((root, _, cb) -> cb.isTrue(root.get("isActive")));

        if (filterDto.firstName() != null && !filterDto.firstName().isBlank()) {
            String like = "%" + filterDto.firstName().trim().toLowerCase() + "%";
            spec = spec.and((root, _, cb) -> cb.like(cb.lower(root.get("firstName")), like));
        }
        if (filterDto.lastName() != null && !filterDto.lastName().isBlank()) {
            String like = "%" + filterDto.lastName().trim().toLowerCase() + "%";
            spec = spec.and((root, _, cb) -> cb.like(cb.lower(root.get("lastName")), like));
        }
        if (filterDto.email() != null && !filterDto.email().isBlank()) {
            String like = "%" + filterDto.email().trim().toLowerCase() + "%";
            spec = spec.and((root, _, cb) -> cb.like(cb.lower(root.get("email")), like));
        }
        if (filterDto.isVerified() != null) {
            boolean v = filterDto.isVerified();
            spec = spec.and((root, _, cb) -> cb.equal(root.get("isVerified"), v));
        }
        if (filterDto.roles() != null && !filterDto.roles().isBlank()) {
            java.util.List<String> roleNames = java.util.Arrays.stream(filterDto.roles().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .map(String::toUpperCase)
                    .toList();
            if (!roleNames.isEmpty()) {
                spec = spec.and((root, _, cb) -> {
                    var rolesJoin = root.join("roles");
                    return cb.upper(rolesJoin.get("name")).in(roleNames);
                });
            }
        }
        if (filterDto.hasSearchTerm()) {
            String like = "%" + filterDto.search().trim().toLowerCase() + "%";
            spec = spec.and((root, _, cb) -> cb.or(
                    cb.like(cb.lower(root.get("firstName")), like),
                    cb.like(cb.lower(root.get("lastName")), like),
                    cb.like(cb.lower(root.get("email")), like)
            ));
        }

        Page<User> page = userRepository.findAll(spec, pageable);
        return page.map(employeeMapper::toResponseDto);
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException("Employee not found", HttpStatus.NOT_FOUND));

        if (!user.isActive()) {
            throw new ApiException("Employee already deleted", HttpStatus.BAD_REQUEST);
        }

        user.setActive(false);
        userRepository.save(user);
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
