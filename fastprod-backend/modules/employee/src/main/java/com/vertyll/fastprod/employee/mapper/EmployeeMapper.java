package com.vertyll.fastprod.employee.mapper;

import com.vertyll.fastprod.common.mapper.MapStructConfig;
import com.vertyll.fastprod.employee.dto.EmployeeCreateDto;
import com.vertyll.fastprod.employee.dto.EmployeeResponseDto;
import com.vertyll.fastprod.employee.dto.EmployeeUpdateDto;
import com.vertyll.fastprod.role.entity.Role;
import com.vertyll.fastprod.user.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(config = MapStructConfig.class)
public interface EmployeeMapper {

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "verified", ignore = true)
    @Mapping(target = "active", ignore = true)
    User toUserEntity(EmployeeCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromDto(EmployeeUpdateDto dto, @MappingTarget User user);

    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToNames")
    @Mapping(target = "isVerified", source = "verified")
    EmployeeResponseDto toResponseDto(User user);

    @Named("rolesToNames")
    default Set<String> rolesToNames(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return Set.of();
        }
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }
}
