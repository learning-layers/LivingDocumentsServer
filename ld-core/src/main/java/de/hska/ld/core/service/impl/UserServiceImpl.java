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

package de.hska.ld.core.service.impl;

import de.hska.ld.core.persistence.domain.Role;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.persistence.repository.UserRepository;
import de.hska.ld.core.service.RoleService;
import de.hska.ld.core.service.UserService;
import de.hska.ld.core.util.Core;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UserServiceImpl extends AbstractService<User> implements UserService {

    private Role userRole;

    @Autowired
    private UserRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleService roleService;

    @Override
    public User findByUsername(String username) {
        return repository.findByUsername(username);
    }

    @Override
    public List<User> findByRole(String roleName) {
        return repository.findByRole(roleName);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repository.findByUsername(username);
    }

    @Override
    public User save(User user, String newPassword) {
        User dbUser = findByUsername(user.getUsername());
        boolean isNew = dbUser == null;
        if (isNew) {
            user.setId(null);
            user.setRoleList(createRoleListForNewUser());
        } else {
            user.setId(dbUser.getId());
            user.setPassword(dbUser.getPassword());
            user.setRoleList(dbUser.getRoleList());
        }
        if (newPassword != null) {
            String hashedPwd = encodePassword(newPassword);
            user.setPassword(hashedPwd);
        }
        return save(user);
    }

    @Override
    public User addRoles(User user, Role... roles) {
        List<Role> filteredRoleList = filterRolesFromClient(roles);
        for (Role role : filteredRoleList) {
            if (hasRole(user, role.getName())) {
                user.getRoleList().add(role);
            }
        }
        return save(user);
    }

    @Override
    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    @Override
    public Boolean hasRole(User user, String roleName) {
        if (user != null) {
            Collection<Role> roleList = user.getRoleList();
            for (Role role : roleList) {
                if (role.getName().equals(roleName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<Role> filterRolesFromClient(Role... roles) {
        List<Role> dbRoleList = new ArrayList<>();
        for (Role role : roles) {
            Role dbRole = roleService.findByName(role.getName());
            if (dbRole != null) {
                dbRoleList.add(dbRole);
            }
        }
        return dbRoleList;
    }

    private List<Role> createRoleListForNewUser() {
        List<Role> roleList = new ArrayList<>();
        roleList.add(roleService.findByName(Core.ROLE_USER));
        return roleList;
    }

    @Override
    public UserRepository getRepository() {
        return repository;
    }
}
