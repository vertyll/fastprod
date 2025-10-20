package com.vertyll.fastprod.role.mapper;

import com.vertyll.fastprod.common.mapper.MapStructConfig;
import com.vertyll.fastprod.role.dto.RoleCreateDto;
import com.vertyll.fastprod.role.dto.RoleUpdateDto;
import com.vertyll.fastprod.role.entity.Role;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = MapStructConfig.class)
public interface RoleMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    Role toEntity(RoleCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    void updateFromDto(RoleUpdateDto dto, @MappingTarget Role role);
}
