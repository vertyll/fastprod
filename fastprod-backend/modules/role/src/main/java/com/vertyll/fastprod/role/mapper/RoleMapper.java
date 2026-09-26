package com.vertyll.fastprod.role.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.vertyll.fastprod.role.dto.RoleCreateDto;
import com.vertyll.fastprod.role.dto.RoleResponseDto;
import com.vertyll.fastprod.role.entity.Role;
import com.vertyll.fastprod.sharedinfrastructure.mapper.MapStructConfig;

@Mapper(config = MapStructConfig.class)
public interface RoleMapper {

    @Mapping(target = "active", ignore = true)
    Role toEntity(RoleCreateDto dto);

    RoleResponseDto toResponseDto(Role role);
}
