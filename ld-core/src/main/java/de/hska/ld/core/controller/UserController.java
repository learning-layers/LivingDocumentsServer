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

package de.hska.ld.core.controller;

import de.hska.ld.core.dto.IdDto;
import de.hska.ld.core.dto.UserDto;
import de.hska.ld.core.dto.UserRoleDto;
import de.hska.ld.core.persistence.domain.Role;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.UserService;
import de.hska.ld.core.util.Core;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * <p><b>RESOURCE</b> {@code /api/users}
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Gets a list with all users.
     * <p><b>REQUIRES</b> ROLE_ADMIN
     *
     * @return {@code 200 OK} and a list with all users or {@code 404 Not Found} or {@code 403 Forbidden}
     * if authorization failed.
     */
    @Secured(Core.ROLE_ADMIN)
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> userList = userService.findAll();
        if (userList != null) {
            return new ResponseEntity<>(userList, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Gets the current logged in user.
     * <p><b>REQUIRES</b> ROLE_USER
     *
     * @return {@code 200 OK} and the current user or {@code 403 Forbidden} if authorization failed.
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/current-user")
    public ResponseEntity<User> getCurrentUser(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    /**
     * Gets a user by its username.
     * <p><b>REQUIRES</b> ROLE_ADMIN or the current user
     *
     * @param username the unique username as a path variable
     * @return {@code 200 OK} and the current user or {@code 404 Not Found} or {@code 403 Forbidden}
     * if authorization failed.
     */
    @PreAuthorize("hasRole('" + Core.ROLE_ADMIN + "') or (isAuthenticated() and principal.username == #username)")
    @RequestMapping(method = RequestMethod.GET, value = "/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        User user = userService.findByUsername(username);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    /**
     * Saves a user. This means a new user will be created if no ID is specified or a old user will be
     * updated if a ID is specified.
     *
     * @param userDto includes the user to be saved or updated. The transfer object also contains a password
     *                field for registration.
     * @return {@code 201 Created} and the user ID or {@code 200 OK} and the ID of the updated subject or
     * {@code 400 Bad Request} if at least one property was invalid or {@code 403 Forbidden} if authorization failed.
     */
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<IdDto> saveUser(@RequestBody @Valid UserDto userDto) {
        User user = userDto.getUser();
        String password = userDto.getPassword();
        boolean isNew = userService.findByUsername(user.getUsername()) == null;
        if (isNew) {
            // User wants to create a new account without password
            if (password == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        } else {
            // User wants to update a different account
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
            boolean isAdmin = userService.hasRole((User) auth.getPrincipal(), Core.ROLE_ADMIN);
            if (!isAdmin && (!auth.getName().equals(user.getUsername()))) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        }
        user = userService.save(user, password);
        IdDto idDto = new IdDto(user.getId());
        if (isNew) {
            return new ResponseEntity<>(idDto, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(idDto, HttpStatus.OK);
        }
    }

    /**
     * Adds one or more roles to an existing user.
     * <p><b>REQUIRES</b> ROLE_ADMIN
     *
     * @param userRoleDto includes the user to be updated. The transfer object also contains a role list. Each role
     *                    will be added to the user.
     * @return {@code 200 OK} or {@code 400 Bad Request} if at least one property was invalid or {@code 403 Forbidden}
     * if authorization failed.
     */
    @Secured(Core.ROLE_ADMIN)
    @RequestMapping(method = RequestMethod.POST, value = "/{username}/roles")
    public ResponseEntity addRolesToUser(@RequestBody @Valid UserRoleDto userRoleDto) {
        User user = userService.findByUsername(userRoleDto.getUsername());
        List<Role> roleList = userRoleDto.getRoleList();
        if (user == null) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        userService.addRoles(user, (Role[]) roleList.toArray());
        return new ResponseEntity(HttpStatus.OK);
    }

    /**
     * Deletes the user.
     * Requires ROLE_ADMIN
     *
     * @param id the user ID as a path variable
     * @return {@code 200 OK} if deletion was successful, {@code 404 Not Found} or {@code 403 Forbidden}
     * if authorization failed.
     */
    @Secured(Core.ROLE_ADMIN)
    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    public ResponseEntity deleteUser(@PathVariable Long id) {
        User user = userService.findById(id);
        if (user != null) {
            userService.delete(user);
            return new ResponseEntity(HttpStatus.OK);
        } else {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
    }
}