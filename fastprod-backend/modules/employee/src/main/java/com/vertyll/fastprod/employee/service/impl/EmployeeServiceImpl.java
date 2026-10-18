package com.vertyll.fastprod.employee.service.impl;

import com.vertyll.fastprod.sharedinfrastructure.exception.ApiException;
import com.vertyll.fastprod.employee.dto.EmployeeCreateDto;
import com.vertyll.fastprod.employee.dto.EmployeeResponseDto;
import com.vertyll.fastprod.employee.dto.EmployeeFilterDto;
import com.vertyll.fastprod.employee.dto.EmployeeUpdateDto;
import com.vertyll.fastprod.employee.mapper.EmployeeMapper;
import com.vertyll.fastprod.employee.service.EmployeeService;
import com.vertyll.fastprod.sharedinfrastructure.enums.RoleType;
import com.vertyll.fastprod.user.api.UserAccounts;
import com.vertyll.fastprod.user.api.UserAccounts.Account;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class EmployeeServiceImpl implements EmployeeService {

    private static final String EMPLOYEE_NOT_FOUND_MESSAGE = "errors.employee.notFound";
    private static final String CANNOT_UPDATE_INACTIVE_EMPLOYEE = "errors.employee.inactive";
    private static final String EMPLOYEE_ALREADY_DELETED = "errors.employee.alreadyDeleted";

    private final UserAccounts accounts;
    private final EmployeeMapper employeeMapper;

    @Override
    @Transactional
    public EmployeeResponseDto createEmployee(EmployeeCreateDto dto) {
        Account account = accounts.create(new UserAccounts.NewAccount(
            dto.firstName(),
            dto.lastName(),
            dto.email(),
            dto.password(),
            roleTypes(dto.roleNames())
        ));
        return employeeMapper.toResponseDto(account);
    }

    @Override
    @Transactional
    public EmployeeResponseDto updateEmployee(Long id, EmployeeUpdateDto dto) {
        Account account = accounts.find(id)
                .orElseThrow(() -> new ApiException(EMPLOYEE_NOT_FOUND_MESSAGE, HttpStatus.NOT_FOUND));

        if (!account.active()) {
            throw new ApiException(CANNOT_UPDATE_INACTIVE_EMPLOYEE, HttpStatus.BAD_REQUEST);
        }

        Set<String> roleNames = dto.roleNames();
        Account updated = accounts.update(id, new UserAccounts.AccountChanges(
            dto.firstName(),
            dto.lastName(),
            dto.email(),
            dto.password(),
            CollectionUtils.isEmpty(roleNames) ? null : roleTypes(roleNames)
        ));
        return employeeMapper.toResponseDto(updated);
    }

    @Override
    public EmployeeResponseDto getEmployeeById(Long id) {
        return accounts.find(id)
                .filter(Account::active)
                .map(employeeMapper::toResponseDto)
                .orElseThrow(() -> new ApiException(EMPLOYEE_NOT_FOUND_MESSAGE, HttpStatus.NOT_FOUND));
    }

    @Override
    public Page<EmployeeResponseDto> getAllEmployees(EmployeeFilterDto filterDto) {
        UserAccounts.AccountSearch search = new UserAccounts.AccountSearch(
            filterDto.firstName(),
            filterDto.lastName(),
            filterDto.email(),
            filterDto.isVerified(),
            roleNames(filterDto.roles()),
            filterDto.search()
        );
        return accounts.findActive(search, filterDto.toPageable()).map(employeeMapper::toResponseDto);
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        Account account = accounts.find(id)
                .orElseThrow(() -> new ApiException(EMPLOYEE_NOT_FOUND_MESSAGE, HttpStatus.NOT_FOUND));

        if (!account.active()) {
            throw new ApiException(EMPLOYEE_ALREADY_DELETED, HttpStatus.BAD_REQUEST);
        }

        accounts.deactivate(id);
    }

    private static Set<RoleType> roleTypes(@Nullable Set<String> roleNames) {
        if (CollectionUtils.isEmpty(roleNames)) {
            return Set.of(RoleType.EMPLOYEE);
        }
        return roleNames.stream().map(RoleType::fromValue).collect(Collectors.toSet());
    }

    private static Set<String> roleNames(@Nullable String roles) {
        if (roles == null || roles.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(roles.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet());
    }
}
