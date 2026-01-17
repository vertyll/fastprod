package com.vertyll.fastprod.user.mapper;

import org.jspecify.annotations.Nullable;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.vertyll.fastprod.common.enums.RoleType;
import com.vertyll.fastprod.common.mapper.MapStructConfig;
import com.vertyll.fastprod.role.entity.Role;
import com.vertyll.fastprod.user.dto.UserCreateDto;
import com.vertyll.fastprod.user.dto.UserResponseDto;
import com.vertyll.fastprod.user.dto.UserUpdateDto;
import com.vertyll.fastprod.user.entity.User;

@Mapper(config = MapStructConfig.class)
public interface UserMapper {

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "verified", ignore = true)
    @Mapping(target = "active", ignore = true)
    User toEntity(UserCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(UserUpdateDto dto, @MappingTarget User user);

    @Mapping(target = "isVerified", source = "verified")
    UserResponseDto toResponseDto(User user);

    default @Nullable RoleType roleToName(Role role) {
        return role != null ? role.getName() : null;
    }
}
