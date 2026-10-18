package com.vertyll.fastprod.user.service;

import com.vertyll.fastprod.sharedinfrastructure.enums.RoleType;
import com.vertyll.fastprod.user.entity.Role;

public interface RoleService {
    Role getOrCreateDefaultRole(RoleType roleName);
}
