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

import de.hska.ld.core.exception.AlreadyExistsException;
import de.hska.ld.core.exception.NotFoundException;
import de.hska.ld.core.exception.UserNotAuthorizedException;
import de.hska.ld.core.exception.ValidationException;
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
import java.util.Date;
import java.util.List;

public class UserServiceImpl extends AbstractService<User> implements UserService {

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
    public User save(User user) {
        boolean isNew = user.getId() == null;
        User userFoundByUsername = findByUsername(user.getUsername());
        if (isNew) {
            // Check user input
            if (user.getPassword() == null) {
                throw new ValidationException("password");
            }
            if (userFoundByUsername != null) {
                throw new AlreadyExistsException("username");
            }
            user.setId(null);
            user.setCreatedAt(new Date());
            String hashedPwd = encodePassword(user.getPassword());
            user.setPassword(hashedPwd);
            createRoleListForUser(user);
        } else {
            // Check if a the current user wants to update an account owned by somebody else
            User currentUser = Core.currentUser();
            if (currentUser == null) {
                throw new UserNotAuthorizedException();
            } else {
                boolean isAdmin = hasRole(currentUser, Core.ROLE_ADMIN);
                if (!isAdmin && !currentUser.getId().equals(user.getId())) {
                    throw new UserNotAuthorizedException();
                }
            }
            if (userFoundByUsername != null && user.getUsername().equals(userFoundByUsername.getUsername()) &&
                    !user.getId().equals(userFoundByUsername.getId())) {
                throw new AlreadyExistsException("username");
            }
            User dbUser = repository.findOne(user.getId());
            user.setId(dbUser.getId());
            user.setPassword(dbUser.getPassword());
            user.setRoleList(dbUser.getRoleList());
        }
        return super.save(user);
    }

    @Override
    public void delete(Long id) {
        User currentUser = Core.currentUser();
        if (!currentUser.getId().equals(id) && !hasRole(currentUser, Core.ROLE_ADMIN)) {
            throw new UserNotAuthorizedException();
        }
        User userToBeDeleted = findById(id);
        if (userToBeDeleted == null) {
            throw new NotFoundException("id");
        }
        super.save(userToBeDeleted);
    }

    @Override
    public User addRoles(String username, String... roleNames) {
        User user = findByUsername(username);
        if (user == null) {
            throw new ValidationException("username");
        }
        List<Role> filteredRoleList = filterRolesFromClient(roleNames);
        filteredRoleList.stream().filter(role -> !hasRole(user, role.getName())).forEach(role -> {
            user.getRoleList().add(role);
        });
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

    private void createRoleListForUser(User user) {
        Collection<Role> roleList;
        boolean adminAvailable = findByUsername(Core.BOOTSTRAP_ADMIN) != null;
        // Trust role list if the current user is a admin
        if (Core.isAdmin() || !adminAvailable) {
            roleList = user.getRoleList();
        } else {
            roleList = new ArrayList<>();
        }
        // Add user role if not exists
        if (!roleList.stream().anyMatch(r -> Core.ROLE_USER.equals(r.getName()))) {
            Role userRole = roleService.findByName(Core.ROLE_USER);
            roleList.add(userRole);
        }
        user.setRoleList(roleList);
    }

    private List<Role> filterRolesFromClient(String... roleNames) {
        List<Role> dbRoleList = new ArrayList<>();
        for (String roleName : roleNames) {
            Role dbRole = roleService.findByName(roleName);
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
