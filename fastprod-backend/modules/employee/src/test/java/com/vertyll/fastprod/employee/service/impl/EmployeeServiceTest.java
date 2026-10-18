package com.vertyll.fastprod.employee.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import com.vertyll.fastprod.employee.dto.EmployeeCreateDto;
import com.vertyll.fastprod.employee.dto.EmployeeFilterDto;
import com.vertyll.fastprod.employee.dto.EmployeeResponseDto;
import com.vertyll.fastprod.employee.dto.EmployeeUpdateDto;
import com.vertyll.fastprod.employee.mapper.EmployeeMapper;
import com.vertyll.fastprod.sharedinfrastructure.enums.RoleType;
import com.vertyll.fastprod.sharedinfrastructure.exception.ApiException;
import com.vertyll.fastprod.user.api.UserAccounts;
import com.vertyll.fastprod.user.api.UserAccounts.Account;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressFBWarnings(
    value = "URF_UNREAD_FIELD",
    justification = "Test class: employeeMapper is used by Mockito injection"
)
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    private static final Account ACTIVE =
            new Account(1L, "John", "Doe", "john@example.com", Set.of(RoleType.EMPLOYEE), true, true);
    private static final Account INACTIVE =
            new Account(1L, "John", "Doe", "john@example.com", Set.of(RoleType.EMPLOYEE), true, false);

    @Mock
    private UserAccounts accounts;

    @Spy
    @SuppressWarnings("UnusedVariable")
    private final EmployeeMapper employeeMapper = Mappers.getMapper(EmployeeMapper.class);

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @Captor
    private ArgumentCaptor<UserAccounts.NewAccount> newAccount;

    @Captor
    private ArgumentCaptor<UserAccounts.AccountChanges> changes;

    @Captor
    private ArgumentCaptor<UserAccounts.AccountSearch> search;

    @Test
    void createEmployee_ShouldCreateTheAccountWithTheGivenRoles() {
        when(accounts.create(any())).thenReturn(ACTIVE);

        EmployeeResponseDto result = employeeService
            .createEmployee(new EmployeeCreateDto("John", "Doe", "john@example.com", "password123", Set.of("ADMIN")));

        verify(accounts).create(newAccount.capture());
        assertEquals(
            new UserAccounts.NewAccount("John", "Doe", "john@example.com", "password123", Set.of(RoleType.ADMIN)),
            newAccount.getValue()
        );
        assertEquals("john@example.com", result.email());
        assertTrue(result.isVerified());
        assertEquals(Set.of(RoleType.EMPLOYEE), result.roles());
    }

    @Test
    void createEmployee_WhenNoRolesProvided_ShouldGiveTheEmployeeRole() {
        when(accounts.create(any())).thenReturn(ACTIVE);

        employeeService.createEmployee(new EmployeeCreateDto("Jane", "Doe", "jane@example.com", "password123", null));

        verify(accounts).create(newAccount.capture());
        assertEquals(Set.of(RoleType.EMPLOYEE), newAccount.getValue().roles());
    }

    @Test
    void createEmployee_WhenEmptyRolesProvided_ShouldGiveTheEmployeeRole() {
        when(accounts.create(any())).thenReturn(ACTIVE);

        employeeService
            .createEmployee(new EmployeeCreateDto("Jane", "Doe", "jane@example.com", "password123", Set.of()));

        verify(accounts).create(newAccount.capture());
        assertEquals(Set.of(RoleType.EMPLOYEE), newAccount.getValue().roles());
    }

    @Test
    void updateEmployee_ShouldPassTheChangesOn() {
        when(accounts.find(1L)).thenReturn(Optional.of(ACTIVE));
        when(accounts.update(anyLong(), any())).thenReturn(ACTIVE);

        employeeService.updateEmployee(
            1L,
            new EmployeeUpdateDto("John", "Doe", "john@example.com", "newPassword123", Set.of("EMPLOYEE", "ADMIN"))
        );

        verify(accounts).update(eq(1L), changes.capture());
        assertEquals(
            new UserAccounts.AccountChanges(
                "John",
                "Doe",
                "john@example.com",
                "newPassword123",
                Set.of(RoleType.EMPLOYEE, RoleType.ADMIN)
            ),
            changes.getValue()
        );
    }

    @Test
    void updateEmployee_WhenNoRolesProvided_ShouldLeaveTheRoles() {
        when(accounts.find(1L)).thenReturn(Optional.of(ACTIVE));
        when(accounts.update(anyLong(), any())).thenReturn(ACTIVE);

        employeeService.updateEmployee(1L, new EmployeeUpdateDto("John", "Doe", "john@example.com", null, null));

        verify(accounts).update(eq(1L), changes.capture());
        assertNull(changes.getValue().roles());
    }

    @Test
    void updateEmployee_WhenEmployeeNotFound_ShouldThrowException() {
        when(accounts.find(1L)).thenReturn(Optional.empty());
        EmployeeUpdateDto dto = new EmployeeUpdateDto("John", "Doe", "john@example.com", null, null);

        ApiException exception = assertThrows(ApiException.class, () -> employeeService.updateEmployee(1L, dto));

        assertEquals("errors.employee.notFound", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void updateEmployee_WhenEmployeeInactive_ShouldThrowException() {
        when(accounts.find(1L)).thenReturn(Optional.of(INACTIVE));
        EmployeeUpdateDto dto = new EmployeeUpdateDto("John", "Doe", "john@example.com", null, null);

        ApiException exception = assertThrows(ApiException.class, () -> employeeService.updateEmployee(1L, dto));

        assertEquals("errors.employee.inactive", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(accounts, never()).update(anyLong(), any());
    }

    @Test
    void getEmployeeById_WhenEmployeeExists_ShouldReturnEmployee() {
        when(accounts.find(1L)).thenReturn(Optional.of(ACTIVE));

        EmployeeResponseDto result = employeeService.getEmployeeById(1L);

        assertEquals("John", result.firstName());
        assertEquals("Doe", result.lastName());
        assertEquals(Set.of(RoleType.EMPLOYEE), result.roles());
    }

    @Test
    void getEmployeeById_WhenEmployeeNotFound_ShouldThrowException() {
        when(accounts.find(1L)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> employeeService.getEmployeeById(1L));

        assertEquals("errors.employee.notFound", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void getEmployeeById_WhenEmployeeInactive_ShouldThrowException() {
        when(accounts.find(1L)).thenReturn(Optional.of(INACTIVE));

        ApiException exception = assertThrows(ApiException.class, () -> employeeService.getEmployeeById(1L));

        assertEquals("errors.employee.notFound", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void getAllEmployees_ShouldSearchWithTheFilterAndItsRoleNames() {
        Page<Account> page = new PageImpl<>(List.of(ACTIVE));
        when(accounts.findActive(any(), any(Pageable.class))).thenReturn(page);

        Page<EmployeeResponseDto> result = employeeService.getAllEmployees(
            new EmployeeFilterDto(0, 10, null, null, null, null, null, true, "admin, employee ,", "jo")
        );

        verify(accounts).findActive(search.capture(), any(Pageable.class));
        assertEquals(Set.of("admin", "employee"), search.getValue().roleNames());
        assertEquals("jo", search.getValue().text());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void deleteEmployee_WhenEmployeeExists_ShouldDeactivateTheAccount() {
        when(accounts.find(1L)).thenReturn(Optional.of(ACTIVE));

        employeeService.deleteEmployee(1L);

        verify(accounts).deactivate(1L);
    }

    @Test
    void deleteEmployee_WhenEmployeeNotFound_ShouldThrowException() {
        when(accounts.find(1L)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> employeeService.deleteEmployee(1L));

        assertEquals("errors.employee.notFound", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void deleteEmployee_WhenEmployeeAlreadyDeleted_ShouldThrowException() {
        when(accounts.find(1L)).thenReturn(Optional.of(INACTIVE));

        ApiException exception = assertThrows(ApiException.class, () -> employeeService.deleteEmployee(1L));

        assertEquals("errors.employee.alreadyDeleted", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(accounts, never()).deactivate(anyLong());
    }
}
