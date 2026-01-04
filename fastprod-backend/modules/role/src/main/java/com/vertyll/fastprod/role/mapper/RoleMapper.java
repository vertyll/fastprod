package com.vertyll.fastprod.role.mapper;

import com.vertyll.fastprod.common.mapper.MapStructConfig;
import com.vertyll.fastprod.role.dto.RoleCreateDto;
import com.vertyll.fastprod.role.dto.RoleResponseDto;
import com.vertyll.fastprod.role.dto.RoleUpdateDto;
import com.vertyll.fastprod.role.entity.Role;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = MapStructConfig.class)
public interface RoleMapper {

    @Mapping(target = "active", ignore = true)
    Role toEntity(RoleCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(RoleUpdateDto dto, @MappingTarget Role role);

    RoleResponseDto toResponseDto(Role role);
}
