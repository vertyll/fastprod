package com.vertyll.fastprod.employee.mapper;

import com.vertyll.fastprod.common.mapper.MapStructConfig;
import com.vertyll.fastprod.employee.dto.EmployeeCreateDto;
import com.vertyll.fastprod.employee.dto.EmployeeUpdateDto;
import com.vertyll.fastprod.user.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = MapStructConfig.class)
public interface EmployeeMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "email", source = "email")
    User toUserEntity(EmployeeCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "email", source = "email")
    void updateUserFromDto(EmployeeUpdateDto dto, @MappingTarget User user);
}
