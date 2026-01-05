package com.vertyll.fastprod.auth.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.vertyll.fastprod.auth.dto.AuthResponseDto;
import com.vertyll.fastprod.auth.dto.RegisterRequestDto;
import com.vertyll.fastprod.auth.dto.SessionInfoDto;
import com.vertyll.fastprod.auth.dto.SessionResponseDto;
import com.vertyll.fastprod.auth.util.UserAgentUtils;
import com.vertyll.fastprod.common.mapper.MapStructConfig;
import com.vertyll.fastprod.user.entity.User;

@Mapper(config = MapStructConfig.class, imports = UserAgentUtils.class)
public interface AuthMapper {

    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "verified", ignore = true)
    @Mapping(target = "active", ignore = true)
    User toUserEntity(RegisterRequestDto dto);

    AuthResponseDto toAuthResponseDto(String token, String type);

    @Mapping(target = "id", source = "info.id")
    @Mapping(target = "deviceInfo", source = "info.deviceInfo")
    @Mapping(target = "ipAddress", source = "info.ipAddress")
    @Mapping(target = "userAgent", source = "info.userAgent")
    @Mapping(target = "browser", expression = "java(UserAgentUtils.parseBrowser(info.userAgent()))")
    @Mapping(target = "os", expression = "java(UserAgentUtils.parseOs(info.userAgent()))")
    @Mapping(target = "createdAt", source = "info.createdAt")
    @Mapping(target = "lastUsedAt", source = "info.lastUsedAt")
    @Mapping(target = "expiresAt", source = "info.expiresAt")
    @Mapping(target = "isCurrent", source = "isCurrent")
    SessionResponseDto toSessionResponseDto(SessionInfoDto info, boolean isCurrent);
}
