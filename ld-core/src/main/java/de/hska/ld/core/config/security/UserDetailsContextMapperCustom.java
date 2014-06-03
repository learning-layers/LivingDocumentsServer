/**
 * Code contributed to the Learning Layers project
 * http://www.learning-layers.eu
 * Development is partly funded by the FP7 Programme of the European
 * Commission under Grant Agreement FP7-ICT-318209.
 * Copyright (c) 2014, Karlsruhe University of Applied Sciences.
 * For a list of contributors see the AUTHORS file at the top-level directory
 * of this distribution.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.hska.ld.core.config.security;

import de.hska.ld.core.persistence.domain.Role;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.RoleService;
import de.hska.ld.core.service.UserService;
import de.hska.ld.core.util.Core;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Operations to map a UserDetails object from the Spring LDAP implementation.
 */
public class UserDetailsContextMapperCustom implements UserDetailsContextMapper {

    @Autowired
    private Environment env;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    private String[] groupRoleMappingCache;

    @Autowired
    private void init() {
        String groupRoleMapping = env.getProperty("module.core.auth.ldap.groupRoleMapping");
        groupRoleMappingCache = groupRoleMapping.split(";");
    }

    @Override
    public UserDetails mapUserFromContext(DirContextOperations ctx, String username,
                                          Collection<? extends GrantedAuthority> authorities) {
        User user = userService.findByUsername(username);
        String fullName = ctx.getStringAttribute(env.getProperty("module.core.auth.ldap.fullNameAttribute"));
        List<Role> roleList = mapLdapGroupsToApplicationRoles(authorities);

        if (user == null) {
            // Create a new user
            user = userService.save(new User(username, fullName, roleList));
        } else {
            // or update the user
            boolean upToDate = true;

            if (!user.getFullName().equals(fullName)) {
                user.setFullName(fullName);
                upToDate = false;
            }

            for (Role mappedRole : roleList) {
                for (Role role : user.getRoleList()) {
                    upToDate = role.getName().equals(mappedRole.getName());
                    if (upToDate) {
                        break;
                    }
                }
                if (!upToDate) {
                    user.setRoleList(roleList);
                    break;
                }
            }

            if (!upToDate) {
                userService.save(user);
            }
        }

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword("MGD_BY_LDAP");
        }

        return user;
    }

    @Override
    public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
    }

    private List<Role> mapLdapGroupsToApplicationRoles(Collection<? extends GrantedAuthority> authorities) {
        List<Role> roleList = new ArrayList<>();
        List<Role> allRoles = roleService.findAll();

        for (GrantedAuthority authority : authorities) {
            Role role = null;
            boolean mapped = false;
            for (String groupRoleMapping : groupRoleMappingCache) {
                // The mapping looks like this: 'groupName:roleName'
                String[] split = groupRoleMapping.split(":");
                String groupName = split[0];
                String roleName = split[1];
                if (groupName.equals(authority.getAuthority())) {
                    // Group is mapped to a role
                    role = findRoleByName(allRoles, roleName);
                    if (role == null) {
                        role = roleService.save(new Role(roleName));
                    }
                    mapped = true;
                }
            }
            if (!mapped) {
                // Group is not mapped to a role
                role = findRoleByName(allRoles, authority.getAuthority());
                if (role == null) {
                    role = roleService.save(new Role(authority.getAuthority()));
                }
            }
            roleList.add(role);
        }

        boolean hasUserRole = false;
        for (Role role : roleList) {
            if (Core.ROLE_USER.equals(role.getName())) {
                hasUserRole = true;
            }
        }
        if (!hasUserRole) {
            Role userRole = roleService.findByName(Core.ROLE_USER);
            roleList.add(userRole);
        }

        return roleList;
    }

    private Role findRoleByName(List<Role> roleList, String name) {
        for (Role role : roleList) {
            if (name != null && name.equals(role.getName())) {
                return role;
            }
        }
        return null;
    }
}
