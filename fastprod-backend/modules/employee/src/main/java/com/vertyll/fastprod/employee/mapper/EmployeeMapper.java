package com.vertyll.fastprod.employee.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.vertyll.fastprod.employee.dto.EmployeeResponseDto;
import com.vertyll.fastprod.sharedinfrastructure.mapper.MapStructConfig;
import com.vertyll.fastprod.user.api.UserAccounts.Account;

@Mapper(config = MapStructConfig.class)
public interface EmployeeMapper {

    @Mapping(target = "isVerified", source = "verified")
    EmployeeResponseDto toResponseDto(Account account);
}
