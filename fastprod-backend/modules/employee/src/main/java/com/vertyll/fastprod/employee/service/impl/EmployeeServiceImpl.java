package com.vertyll.fastprod.employee.service.impl;

import com.vertyll.fastprod.sharedinfrastructure.exception.ApiException;
import com.vertyll.fastprod.employee.dto.EmployeeCreateDto;
import com.vertyll.fastprod.employee.dto.EmployeeResponseDto;
import com.vertyll.fastprod.employee.dto.EmployeeFilterDto;
import com.vertyll.fastprod.employee.dto.EmployeeUpdateDto;
import com.vertyll.fastprod.employee.mapper.EmployeeMapper;
import com.vertyll.fastprod.employee.service.EmployeeService;
import com.vertyll.fastprod.role.entity.Role;
import com.vertyll.fastprod.sharedinfrastructure.enums.RoleType;
import com.vertyll.fastprod.role.service.RoleService;
import com.vertyll.fastprod.user.entity.User;
import com.vertyll.fastprod.user.repository.UserRepository;
import jakarta.persistence.criteria.Join;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
class EmployeeServiceImpl implements EmployeeService {

    private static final String EMPLOYEE_NOT_FOUND_MESSAGE = "errors.employee.notFound";
    private static final String EMAIL_ALREADY_EXISTS = "errors.user.emailTaken";
    private static final String CANNOT_UPDATE_INACTIVE_EMPLOYEE = "errors.employee.inactive";
    private static final String EMPLOYEE_ALREADY_DELETED = "errors.employee.alreadyDeleted";

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeMapper employeeMapper;

    @Override
    @Transactional
    @SuppressWarnings("java:S4449")
    public EmployeeResponseDto createEmployee(EmployeeCreateDto dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new ApiException(EMAIL_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
        }

        User user = employeeMapper.toUserEntity(dto);
        user.changePassword(passwordEncoder.encode(dto.password()));
        user.markVerified();

        assignRolesToUser(user, dto.roleNames());

        User savedUser = userRepository.save(user);
        return employeeMapper.toResponseDto(savedUser);
    }

    @Override
    @Transactional
    public EmployeeResponseDto updateEmployee(Long id, EmployeeUpdateDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(EMPLOYEE_NOT_FOUND_MESSAGE, HttpStatus.NOT_FOUND));

        if (!user.isActive()) {
            throw new ApiException(CANNOT_UPDATE_INACTIVE_EMPLOYEE, HttpStatus.BAD_REQUEST);
        }

        if (!dto.email().equals(user.getEmail()) && userRepository.existsByEmail(dto.email())) {
            throw new ApiException(EMAIL_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
        }

        user.updateDetails(dto.firstName(), dto.lastName(), dto.email());

        String password = dto.password();
        if (password != null && !password.isBlank()) {
            user.changePassword(passwordEncoder.encode(password));
        }

        Set<String> roleNames = dto.roleNames();
        if (!CollectionUtils.isEmpty(roleNames)) {
            user.getRoles().clear();
            assignRolesToUser(user, roleNames);
        }

        User updatedUser = userRepository.save(user);
        return employeeMapper.toResponseDto(updatedUser);
    }

    @Override
    public EmployeeResponseDto getEmployeeById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(EMPLOYEE_NOT_FOUND_MESSAGE, HttpStatus.NOT_FOUND));

        if (!user.isActive()) {
            throw new ApiException(EMPLOYEE_NOT_FOUND_MESSAGE, HttpStatus.NOT_FOUND);
        }

        return employeeMapper.toResponseDto(user);
    }

    @Override
    public Page<EmployeeResponseDto> getAllEmployees(EmployeeFilterDto filterDto) {
        Pageable pageable = filterDto.toPageable();
        Specification<User> spec = buildEmployeeSpecification(filterDto);
        Page<User> page = userRepository.findAll(spec, pageable);
        return page.map(employeeMapper::toResponseDto);
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(EMPLOYEE_NOT_FOUND_MESSAGE, HttpStatus.NOT_FOUND));

        if (!user.isActive()) {
            throw new ApiException(EMPLOYEE_ALREADY_DELETED, HttpStatus.BAD_REQUEST);
        }

        user.deactivate();
        userRepository.save(user);
    }

    private Specification<User> buildEmployeeSpecification(EmployeeFilterDto filterDto) {
        Specification<User> spec = Specification.where((root, _, cb) -> cb.isTrue(root.get("active")));

        spec = addFirstNameFilter(spec, filterDto.firstName());
        spec = addLastNameFilter(spec, filterDto.lastName());
        spec = addEmailFilter(spec, filterDto.email());
        spec = addVerifiedFilter(spec, filterDto.isVerified());
        spec = addRolesFilter(spec, filterDto.roles());
        spec = addSearchFilter(spec, filterDto.search());

        return spec;
    }

    private Specification<User> addFirstNameFilter(Specification<User> spec, @Nullable String firstName) {
        if (firstName == null || firstName.isBlank()) {
            return spec;
        }
        String likePattern = "%" + firstName.trim().toLowerCase(Locale.ROOT) + "%";
        return spec.and((root, _, cb) -> cb.like(cb.lower(root.get("firstName")), likePattern));
    }

    private Specification<User> addLastNameFilter(Specification<User> spec, @Nullable String lastName) {
        if (lastName == null || lastName.isBlank()) {
            return spec;
        }
        String likePattern = "%" + lastName.trim().toLowerCase(Locale.ROOT) + "%";
        return spec.and((root, _, cb) -> cb.like(cb.lower(root.get("lastName")), likePattern));
    }

    private Specification<User> addEmailFilter(Specification<User> spec, @Nullable String email) {
        if (email == null || email.isBlank()) {
            return spec;
        }
        String likePattern = "%" + email.trim().toLowerCase(Locale.ROOT) + "%";
        return spec.and((root, _, cb) -> cb.like(cb.lower(root.get("email")), likePattern));
    }

    private Specification<User> addVerifiedFilter(Specification<User> spec, @Nullable Boolean isVerified) {
        if (isVerified == null) {
            return spec;
        }
        return spec.and((root, _, cb) -> cb.equal(root.get("verified"), isVerified));
    }

    private Specification<User> addRolesFilter(Specification<User> spec, @Nullable String roles) {
        if (roles == null || roles.isBlank()) {
            return spec;
        }

        List<String> roleNames = Arrays.stream(roles.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(String::toUpperCase)
                .toList();

        if (roleNames.isEmpty()) {
            return spec;
        }

        return spec.and((root, _, cb) -> {
            Join<User, Role> rolesJoin = root.join("roles");
            return cb.upper(rolesJoin.get("name")).in(roleNames);
        });
    }

    private Specification<User> addSearchFilter(Specification<User> spec, @Nullable String searchTerm) {
        if (searchTerm == null || searchTerm.isBlank()) {
            return spec;
        }
        String likePattern = "%" + searchTerm.trim().toLowerCase(Locale.ROOT) + "%";
        return spec.and((root, _, cb) -> cb.or(
                cb.like(cb.lower(root.get("firstName")), likePattern),
                cb.like(cb.lower(root.get("lastName")), likePattern),
                cb.like(cb.lower(root.get("email")), likePattern)
        ));
    }

    private void assignRolesToUser(User user, @Nullable Set<String> roleNames) {
        if (CollectionUtils.isEmpty(roleNames)) {
            Role employeeRole = roleService.getOrCreateDefaultRole(RoleType.EMPLOYEE);
            user.getRoles().add(employeeRole);
        } else {
            roleNames.forEach(roleName -> {
                Role role = roleService.getOrCreateDefaultRole(RoleType.fromValue(roleName));
                user.getRoles().add(role);
            });
        }
    }
}
