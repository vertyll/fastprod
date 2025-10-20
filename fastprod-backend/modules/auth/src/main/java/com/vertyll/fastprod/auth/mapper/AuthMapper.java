package com.vertyll.fastprod.auth.mapper;

import com.vertyll.fastprod.common.mapper.MapStructConfig;
import com.vertyll.fastprod.auth.dto.RegisterRequestDto;
import com.vertyll.fastprod.user.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface AuthMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "email", source = "email")
    User toUserEntity(RegisterRequestDto dto);
}
