package com.vertyll.fastprod.role.service;

import com.vertyll.fastprod.role.entity.Role;
import com.vertyll.fastprod.sharedinfrastructure.enums.RoleType;

public interface RoleService {
    Role getOrCreateDefaultRole(RoleType roleName);
}
