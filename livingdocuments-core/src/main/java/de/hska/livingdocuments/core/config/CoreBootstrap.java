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

package de.hska.livingdocuments.core.config;

import de.hska.livingdocuments.core.persistence.domain.Role;
import de.hska.livingdocuments.core.persistence.domain.User;
import de.hska.livingdocuments.core.service.RoleService;
import de.hska.livingdocuments.core.service.UserService;
import de.hska.livingdocuments.core.util.Core;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
public class CoreBootstrap {

    private String adminPassword;

    @Autowired
    public void init(Environment env, UserService userService, RoleService roleService) {
        List<Role> roleList = roleService.findAll();
        if (roleList == null || roleList.isEmpty()) {
            Role userRole = roleService.save(new Role(Core.ROLE_USER));
            Role adminRole = roleService.save(new Role(Core.ROLE_ADMIN));

            adminPassword = userService.encodePassword(env.getProperty("module.core.bootstrap.admin.password"));

            userService.save(getUser("user", "user", userRole));
            userService.save(getUser("admin", "admin", userRole, adminRole));
        }
    }

    /**
     * Creates a new bootstrap user.
     *
     * @param username the username
     * @param fullName the full name
     * @param roles    the user roles
     * @return
     */
    public User getUser(String username, String fullName, Role... roles) {
        User user = new User();
        user.setUsername(username);
        user.setFullName(fullName);
        user.setPassword(adminPassword);
        user.setRoleList(convertToRoleList(roles));
        return user;
    }

    private Collection<Role> convertToRoleList(Role... roles) {
        Collection<Role> roleList = new ArrayList<>();
        Collections.addAll(roleList, roles);
        return roleList;
    }
}