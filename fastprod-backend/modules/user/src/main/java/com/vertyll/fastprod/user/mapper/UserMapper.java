package com.vertyll.fastprod.user.mapper;

import com.vertyll.fastprod.common.mapper.MapStructConfig;
import com.vertyll.fastprod.user.dto.UserCreateDto;
import com.vertyll.fastprod.user.dto.UserUpdateDto;
import com.vertyll.fastprod.user.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = MapStructConfig.class)
public interface UserMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "email", source = "email")
    User toEntity(UserCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "email", source = "email")
    void updateFromDto(UserUpdateDto dto, @MappingTarget User user);
}
