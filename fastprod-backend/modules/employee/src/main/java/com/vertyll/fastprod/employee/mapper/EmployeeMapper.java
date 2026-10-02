package com.vertyll.fastprod.employee.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.vertyll.fastprod.employee.dto.EmployeeResponseDto;
import com.vertyll.fastprod.role.entity.Role;
import com.vertyll.fastprod.sharedinfrastructure.enums.RoleType;
import com.vertyll.fastprod.sharedinfrastructure.mapper.MapStructConfig;
import com.vertyll.fastprod.user.entity.User;

@Mapper(config = MapStructConfig.class)
public interface EmployeeMapper {

    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToNames")
    @Mapping(target = "isVerified", source = "verified")
    EmployeeResponseDto toResponseDto(User user);

    @Named("rolesToNames")
    default Set<RoleType> rolesToNames(Set<Role> roles) {
        return roles.stream().map(Role::getName).collect(Collectors.toSet());
    }
}
