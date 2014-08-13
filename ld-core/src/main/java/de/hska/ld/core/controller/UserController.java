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

import de.hska.ld.core.dto.UserRoleDto;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.UserService;
import de.hska.ld.core.util.Core;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * <p><b>Resource:</b> {@value Core#RESOURCE_USER}
 */
@RestController
@RequestMapping(Core.RESOURCE_USER)
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * <pre>
     * Gets a list with all users.
     *
     * <b>Required roles:</b> ROLE_ADMIN
     * <b>Path:</b> GET {@value Core#RESOURCE_USER}
     * </pre>
     *
     * @return <b>200 OK</b> and a list with all users or <br>
     * <b>403 Forbidden</b> if authorization failed or <br>
     * <b>404 Not Found</b> if no users are in the system
     */
    @Secured(Core.ROLE_ADMIN)
    @RequestMapping(method = RequestMethod.GET, value = "/userlist")
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
     * Gets a list with all users.
     *
     * <b>Required roles:</b> ROLE_ADMIN
     * <b>Path:</b> GET {@value Core#RESOURCE_USER}
     * </pre>
     *
     * @return <b>200 OK</b> and a list with all users or <br>
     * <b>403 Forbidden</b> if authorization failed or <br>
     * <b>404 Not Found</b> if no users are in the system
     */
    @Secured(Core.ROLE_ADMIN)
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<Page<User>> getUsersPage(@RequestParam(value = "page-number", defaultValue = "0") Integer pageNumber,
                                                   @RequestParam(value = "page-size", defaultValue = "10") Integer pageSize,
                                                   @RequestParam(value = "sort-direction", defaultValue = "DESC") String sortDirection,
                                                   @RequestParam(value = "sort-property", defaultValue = "createdAt") String sortProperty) {
        Page<User> usersPage = userService.getUsersPage(pageNumber, pageSize, sortDirection, sortProperty);
        if (usersPage != null) {
            return new ResponseEntity<>(usersPage, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * <pre>
     * Gets a list with all users.
     *
     * <b>Required roles:</b> ROLE_ADMIN
     * <b>Path:</b> GET {@value Core#RESOURCE_USER}
     * </pre>
     *
     * @return <b>200 OK</b> and a list with all users or <br>
     * <b>403 Forbidden</b> if authorization failed or <br>
     * <b>404 Not Found</b> if no users are in the system
     */
    @Secured(Core.ROLE_ADMIN)
    @RequestMapping(method = RequestMethod.GET, value = "/disabled")
    public ResponseEntity<Page<User>> getUsersDisabledPage(@RequestParam(value = "page-number", defaultValue = "0") Integer pageNumber,
                                                           @RequestParam(value = "page-size", defaultValue = "10") Integer pageSize,
                                                           @RequestParam(value = "sort-direction", defaultValue = "DESC") String sortDirection,
                                                           @RequestParam(value = "sort-property", defaultValue = "createdAt") String sortProperty) {
        Page<User> usersPage = userService.getUsersDisabledPage(pageNumber, pageSize, sortDirection, sortProperty);
        if (usersPage != null) {
            return new ResponseEntity<>(usersPage, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Secured(Core.ROLE_ADMIN)
    @RequestMapping(method = RequestMethod.POST, value = "/activate/{userid}")
    public ResponseEntity activateUser(@PathVariable Long userid) {
        User user = userService.activateUser(userid);
        if (user != null) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * <pre>
     * Authenticates the current user.
     *
     * <b>Required roles:</b> ROLE_USER
     * <b>Path:</b> GET {@value Core#RESOURCE_USER}/authenticate
     * </pre>
     *
     * @return <b>200 OK</b> and the current logged in user or <br>
     * <b>403 Forbidden</b> if authentication failed
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
     * <b>Path:</b> GET {@value Core#RESOURCE_USER}/{username}
     * </pre>
     *
     * @param username the unique username as path variable
     * @return <b>200 OK</b> and the user or <br>
     * <b>403 Forbidden</b> if authorization failed or <br>
     * <b>404 Not Found</b> if no user with the given username has been found
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
     * Saves a user. This means a new user will be created if no ID is specified or an old user will be
     * updated if ID is specified.
     *
     * <b>Required roles:</b> ROLE_ADMIN
     * <b>Path:</b> POST {@value Core#RESOURCE_USER}
     * </pre>
     *
     * @param user includes the user to be saved or updated. Example:<br>
     *             {username: 'jdoe', email: 'jdoe@jdoe.org', fullName: 'John Doe'}
     * @return <b>201 Created</b> and the user or <br>
     * <b>200 OK</b> and the user or <br>
     * <b>400 Bad Request</b> if at least one property was invalid or <br>
     * <b>403 Forbidden</b> if authorization failed
     */
    @Secured(Core.ROLE_ADMIN)
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<User> saveUser(@RequestBody @Valid User user) {
        boolean isNew = user.getId() == null;
        user = userService.save(user);
        if (isNew) {
            return new ResponseEntity<>(user, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(user, HttpStatus.OK);
        }
    }

    /**
     * <pre>
     * Register a new user.
     *
     * <b>Required roles:</b> ROLE_ADMIN
     * <b>Path:</b> POST {@value Core#RESOURCE_USER}
     * </pre>
     *
     * @param user includes the user to be saved or updated. Example:<br>
     *             {username: 'jdoe', email: 'jdoe@jdoe.org', fullName: 'John Doe'}
     * @return <b>201 Created</b> and the user or <br>
     * <b>400 Bad Request</b> if at least one property was invalid
     */
    @RequestMapping(method = RequestMethod.POST, value = "/register")
    public ResponseEntity register(@RequestBody @Valid User user) {
        userService.register(user);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * <pre>
     * Enables the user.
     *
     * <b>Required roles:</b> no role required
     * <b>Path:</b> GET {@value Core#RESOURCE_USER}/confirmRegistration/{confirmationKey}
     * </pre>
     *
     * @param confirmationKey the confirmation key as path variable
     * @return <b>200 OK</b> if confirmation was successful or <br>
     * <b>400 Bad Request</b> if at least one property was invalid or <br>
     * <b>404 Not Found</b> if no user with the given confirmation key has been found
     */
    @RequestMapping(method = RequestMethod.GET, value = "/confirmRegistration/{confirmationKey}")
    public ResponseEntity<User> confirmRegistration(@PathVariable String confirmationKey) {
        User user = userService.confirmRegistration(confirmationKey);
        return new ResponseEntity(user, HttpStatus.OK);
    }

    /**
     * <pre>
     * Adds one or more roles to an existing user.
     *
     * <b>Required roles:</b> ROLE_ADMIN
     * <b>Path:</b> POST {@value Core#RESOURCE_USER}/roles/add
     * </pre>
     *
     * @param userRoleDto includes the username of the user to be updated. The transfer object also contains a role list.
     *                    Each role will be added to the user. Example: <br>
     *                    <code>{username: 'jdoe', roleList: [{id: 1, name: 'ROLE_NAME'}]}</code>
     * @return <b>200 OK</b> or <br>
     * <b>403 Forbidden</b> if authorization failed or <br>
     * <b>404 Not Found</b> if no user with the given username has been found
     */
    @Secured(Core.ROLE_ADMIN)
    @RequestMapping(method = RequestMethod.POST, value = "/roles/add")
    public ResponseEntity addRolesToUser(@RequestBody @Valid UserRoleDto userRoleDto) {
        userService.addRoles(userRoleDto.getUsername(), userRoleDto.getRoleNames());
        return new ResponseEntity(HttpStatus.OK);
    }

    /**
     * <pre>
     * Deletes a user.
     *
     * <b>Required roles:</b> ROLE_ADMIN
     * <b>Path:</b> DELETE {@value Core#RESOURCE_USER}/{id}
     * </pre>
     *
     * @param id the user ID as a path variable
     * @return <b>200 OK</b> if deletion was successful, <br>
     * <b>403 Forbidden</b> if authorization failed or <br>
     * <b>404 Not Found</b> if no user with the given ID has been found
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    public ResponseEntity deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return new ResponseEntity(HttpStatus.OK);
    }

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/avatars")
    public List<byte[]> loadAvatars(@RequestParam String userIdsString) {
        return userService.getAvatars(userIdsString);
    }
}