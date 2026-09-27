package com.vertyll.fastprod.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.vertyll.fastprod.role.entity.Role;
import com.vertyll.fastprod.sharedinfrastructure.enums.RoleType;
import com.vertyll.fastprod.sharedinfrastructure.mapper.MapStructConfig;
import com.vertyll.fastprod.user.dto.UserCreateDto;
import com.vertyll.fastprod.user.dto.UserResponseDto;
import com.vertyll.fastprod.user.entity.User;

@Mapper(config = MapStructConfig.class)
public interface UserMapper {

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "verified", ignore = true)
    @Mapping(target = "active", ignore = true)
    User toEntity(UserCreateDto dto);

    @Mapping(target = "isVerified", source = "verified")
    UserResponseDto toResponseDto(User user);

    default RoleType roleToName(Role role) {
        return role.getName();
    }
}
