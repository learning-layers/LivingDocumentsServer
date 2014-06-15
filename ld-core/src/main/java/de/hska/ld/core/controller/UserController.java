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
 * <p><b>Resource:</b> {@value Core#USER_RESOURCE}
 */
@RestController
@RequestMapping(Core.USER_RESOURCE)
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * <pre>
     * Gets a list with all users.
     *
     * <b>Required roles:</b> ROLE_ADMIN
     * <b>Path:</b> GET {@value Core#USER_RESOURCE}
     * </pre>
     *
     * @return <b>200 OK</b> and a list with all users or <br>
     *         <b>403 Forbidden</b> if authorization failed or <br>
     *         <b>404 Not Found</b> if no users are in the system
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
     * <pre>
     * Authenticates the current user.
     *
     * <b>Required roles:</b> ROLE_USER
     * <b>Path:</b> GET {@value Core#USER_RESOURCE}/authenticate
     * </pre>
     *
     * @return  <b>200 OK</b> and the current logged in user or <br>
     *          <b>403 Forbidden</b> if authentication failed
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/authenticate")
    public ResponseEntity<User> authenticate(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    /**
     * <pre>
     * Gets a user by its username.
     *
     * <b>Required roles:</b> authorized user
     * <b>Path:</b> GET {@value Core#USER_RESOURCE}/{username}
     * </pre>
     *
     * @param username the unique username as path variable
     * @return <b>200 OK</b> and the user or <br>
     *         <b>403 Forbidden</b> if authorization failed or <br>
     *         <b>404 Not Found</b> if no user with the given username has been found
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
     * <pre>
     * Saves a user. If no ID is provided a new user will be created otherwise an existing user will be updated.
     *
     * <b>Required roles:</b> no role required
     * <b>Path:</b> POST {@value Core#USER_RESOURCE}
     * </pre>
     *
     * @param userDto represents the user to be saved or updated. The transfer object also contains a password
     *                field for registration. Example:<br>
     *                {user: {username: 'jdoe', fullName: 'John Doe'}, password: '&lt;PASSWORD_(ONLY_REQUIRED_FOR_NEW_USER)&gt;'}
     * @return <b>200 OK</b> and the user or <br>
     *         <b>201 Created</b> and the user ID or <br>
     *         <b>400 Bad Request</b> if at least one property was invalid or <br>
     *         <b>403 Forbidden</b> if authorization failed
     */
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<User> saveUser(@RequestBody @Valid UserDto userDto) {
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
        if (isNew) {
            return new ResponseEntity<>(user, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(user, HttpStatus.OK);
        }
    }

    /**
     * <pre>
     * Adds one or more roles to an existing user.
     *
     * <b>Required roles:</b> ROLE_ADMIN
     * <b>Path:</b> POST {@value Core#USER_RESOURCE}/roles/add
     * </pre>
     *
     * @param userRoleDto includes the username of the user to be updated. The transfer object also contains a role list.
     *                    Each role will be added to the user. Example: <br>
     *                    <code>{username: 'jdoe', roleList: [{id: 1, name: 'ROLE_NAME'}]}</code>
     * @return <b>200 OK</b> or <br>
     *         <b>403 Forbidden</b> if authorization failed or <br>
     *         <b>404 Not Found</b> if no user with the given username has been found
     */
    @Secured(Core.ROLE_ADMIN)
    @RequestMapping(method = RequestMethod.POST, value = "/roles/add")
    public ResponseEntity addRolesToUser(@RequestBody @Valid UserRoleDto userRoleDto) {
        // TODO move to role controller
        User user = userService.findByUsername(userRoleDto.getUsername());
        List<Role> roleList = userRoleDto.getRoleList();
        if (user == null) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        userService.addRoles(user, (Role[]) roleList.toArray());
        return new ResponseEntity(HttpStatus.OK);
    }

    /**
     * <pre>
     * Deletes a user.
     *
     * <b>Required roles:</b> ROLE_ADMIN
     * <b>Path:</b> DELETE {@value Core#USER_RESOURCE}/{id}
     * </pre>
     *
     * @param id the user ID as a path variable
     * @return <b>200 OK</b> if deletion was successful, <br>
     *         <b>403 Forbidden</b> if authorization failed or <br>
     *         <b>404 Not Found</b> if no user with the given ID has been found
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