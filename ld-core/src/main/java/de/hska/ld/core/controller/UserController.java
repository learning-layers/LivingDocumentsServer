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
import de.hska.ld.core.exception.NotFoundException;
import de.hska.ld.core.exception.ValidationException;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.UserService;
import de.hska.ld.core.util.AsyncExecutor;
import de.hska.ld.core.util.Core;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * <p><b>Resource:</b> {@value Core#RESOURCE_USER}
 */
@RestController
@RequestMapping(Core.RESOURCE_USER)
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AsyncExecutor asyncExecutor;

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
    public Callable getAllUsers() {
        return () -> {
            List<User> userList = userService.findAll();
            if (userList != null) {
                return new ResponseEntity<>(userList, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        };
    }


    //TODO done with default parameters
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
    public Callable getUsersPage(@RequestParam(value = "page-number", defaultValue = "0") Integer pageNumber,
                                 @RequestParam(value = "page-size", defaultValue = "10") Integer pageSize,
                                 @RequestParam(value = "sort-direction", defaultValue = "DESC") String sortDirection,
                                 @RequestParam(value = "sort-property", defaultValue = "createdAt") String sortProperty) {
        return () -> {
            Page<User> usersPage = userService.getUsersPage(pageNumber, pageSize, sortDirection, sortProperty);
            if (usersPage != null) {
                return new ResponseEntity<>(usersPage, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        };
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
    @Transactional(readOnly = true)
    public Callable getUsersDisabledPage(@RequestParam(value = "page-number", defaultValue = "0") Integer pageNumber,
                                         @RequestParam(value = "page-size", defaultValue = "10") Integer pageSize,
                                         @RequestParam(value = "sort-direction", defaultValue = "DESC") String sortDirection,
                                         @RequestParam(value = "sort-property", defaultValue = "createdAt") String sortProperty) {
        return () -> {
            Page<User> usersPage = userService.getUsersDisabledPage(pageNumber, pageSize, sortDirection, sortProperty);
            if (usersPage != null) {
                return new ResponseEntity<>(usersPage, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        };
    }

    //TODO Description
    @Secured(Core.ROLE_ADMIN)
    @RequestMapping(method = RequestMethod.POST, value = "/activate/{userid}")
    public Callable activateUser(@PathVariable Long userid) {
        return () -> {
            User user = userService.activateUser(userid);
            if (user != null) {
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        };
    }

    //TODO Description
    @Secured(Core.ROLE_ADMIN)
    @RequestMapping(method = RequestMethod.POST, value = "/deactivate/{userid}")
    public Callable deactivateUser(@PathVariable Long userid) {
        return () -> {
            User user = userService.deactivateUser(userid);
            if (user != null) {
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        };
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
    public Callable authenticate(@AuthenticationPrincipal User user) {
        return () -> new ResponseEntity<>(user, HttpStatus.OK);
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
    @Transactional(readOnly = true)
    public Callable getUserByUsername(@PathVariable String username) {
        return () -> {
            User user = userService.findByUsername(username);
            if (user == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(user, HttpStatus.OK);
        };
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
    public Callable saveUser(@RequestBody @Valid final User user) {
        return () -> {
            boolean isNew = user.getId() == null;
            User savedUser = userService.save(user);
            if (isNew) {
                return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
            } else {
                return new ResponseEntity<>(savedUser, HttpStatus.OK);
            }
        };
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
    public Callable register(@RequestBody @Valid User user) {
        return () -> {
            userService.register(user);
            return new ResponseEntity<>(HttpStatus.CREATED);
        };
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
    @SuppressWarnings("unchecked")
    @RequestMapping(method = RequestMethod.GET, value = "/confirmRegistration/{confirmationKey}")
    public Callable confirmRegistration(@PathVariable String confirmationKey) {
        return () -> {
            User user = userService.confirmRegistration(confirmationKey);
            return new ResponseEntity(user, HttpStatus.OK);
        };
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
    public Callable addRolesToUser(@RequestBody @Valid UserRoleDto userRoleDto) {
        return () -> {
            userService.addRoles(userRoleDto.getUsername(), userRoleDto.getRoleNames());
            return new ResponseEntity(HttpStatus.OK);
        };
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
    public Callable deleteUser(@PathVariable Long id) {
        return () -> {
            userService.delete(id);
            return new ResponseEntity(HttpStatus.OK);
        };
    }

    //TODO: describe functionality of Methode
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/avatars")
    @Transactional(readOnly = true)
    public Callable loadAvatars(@RequestParam String userIdsString) {
        return () -> userService.getAvatars(userIdsString);
    }

    //TODO: describe functionality of Methode
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/avatar")
    @Transactional(readOnly = true)
    public void loadAvatar(HttpServletResponse response) {
        try {
            User user = Core.currentUser();
            String userIdString = user.getId().toString();
            List<byte[]> avatars = userService.getAvatars(userIdString);
            if (avatars != null && avatars.size() > 0) {
                byte[] source = avatars.get(0);
                if (source != null) {
                    InputStream is = new ByteArrayInputStream(source);
                    //response.setContentType(attachment.getMimeType());
                    OutputStream outputStream = response.getOutputStream();
                    IOUtils.copy(is, outputStream);
                } else {
                    String fileName = "Portrait_placeholder.png";
                    InputStream is = null;
                    try {
                        is = UserController.class.getResourceAsStream("/" + fileName);
                        //response.setContentType(attachment.getMimeType());
                        OutputStream outputStream = response.getOutputStream();
                        IOUtils.copy(is, outputStream);
                    } finally {
                        IOUtils.closeQuietly(is);
                    }
                }
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    //TODO: describe functionality of Methode
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/avatar/{id}")
    @Transactional(readOnly = true)
    public void loadAvatarForUser(@PathVariable Long id, HttpServletResponse response) {
        try {
            String userIdString = id.toString();
            List<byte[]> avatars = userService.getAvatars(userIdString);
            if (avatars != null && avatars.size() > 0) {
                byte[] source = avatars.get(0);
                if (source != null) {
                    InputStream is = new ByteArrayInputStream(source);
                    //response.setContentType(attachment.getMimeType());
                    OutputStream outputStream = response.getOutputStream();
                    IOUtils.copy(is, outputStream);
                } else {
                    String fileName = "Portrait_placeholder.png";
                    InputStream is = null;
                    try {
                        is = UserController.class.getResourceAsStream("/" + fileName);
                        //response.setContentType(attachment.getMimeType());
                        OutputStream outputStream = response.getOutputStream();
                        IOUtils.copy(is, outputStream);
                    } finally {
                        IOUtils.closeQuietly(is);
                    }
                }
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }


    //TODO: describe functionality of Methode
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST, value = "/avatar")
    public Callable uploadAvatar(@RequestParam MultipartFile file) {
        return () -> {
            String name = file.getOriginalFilename();
            if (!file.isEmpty()) {
                userService.uploadAvatar(file, name);
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                throw new ValidationException("file");
            }
        };
    }

    //TODO: describe functionality of Methode
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/suggestions/list")
    @Transactional(readOnly = true)
    public Callable getMentionSuggestionsList(@RequestParam(required = false) String term) {
        return () -> {
            if (!"".equals(term) || term.contains("%")) {
                List<User> userSuggestionList = userService.getMentionSuggestions(term);
                return new ResponseEntity<>(userSuggestionList, HttpStatus.OK);
            } else {
                List<User> userSuggestionList = userService.getMentionSuggestions(term);
                return new ResponseEntity<>(userSuggestionList, HttpStatus.BAD_REQUEST);
            }
        };
    }


    //TODO: describe functionality of Methode
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/suggestions")
    @Transactional(readOnly = true)
    public Callable getMentionSuggestions(@RequestParam(value = "page-number", defaultValue = "0") Integer pageNumber,
                                          @RequestParam(value = "page-size", defaultValue = "10") Integer pageSize,
                                          @RequestParam(value = "sort-direction", defaultValue = "DESC") String sortDirection,
                                          @RequestParam(value = "sort-property", defaultValue = "username") String sortProperty,
                                          @RequestParam(value = "search-term", required = false) String searchTerm) {
        return () -> {
            Page<User> usersPage = userService.getUsersPage(pageNumber, pageSize, sortDirection, sortProperty, searchTerm);
            if (usersPage != null) {
                return new ResponseEntity<>(usersPage, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        };
    }


    //TODO: describe functionality of Methode
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/username")
    @Transactional(readOnly = true)
    public Callable getUserByUserName(@RequestParam String term) {
        return () -> {
            User user = userService.findByUsername(term);
            if (user == null) {
                throw new NotFoundException();
            }
            return new ResponseEntity<>(user, HttpStatus.OK);
        };
    }


    //TODO: describe functionality of Methode
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/id")
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public Callable getUser(@RequestParam Long id) {
        return () -> {
            User user = userService.findById(id);
            if (user == null) {
                throw new NotFoundException();
            }
            return new ResponseEntity(user, HttpStatus.OK);
        };
    }

    //TODO: describe functionality of Methode (Update password for current user)
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.PUT, value = "/password")
    public Callable updatePassword(@RequestBody User user) {
        return () -> {
            User dbUser = userService.updatePassword(user.getPassword());
            return new ResponseEntity<>(dbUser, HttpStatus.OK);
        };
    }

    //TODO: describe functionality of Methode
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.PUT, value = "/profile")
    public Callable updateProfile(@RequestBody User user) {
        return () -> {
            User dbUser = userService.updateProfile(user);
            return new ResponseEntity<>(dbUser, HttpStatus.OK);
        };
    }

    /**
     * <pre>
     * Forgot Password workflow (Step 1)
     *
     * <b>Required roles:</b> no role required
     * <b>Path:</b> POST {@value Core#RESOURCE_USER}/forgotPassword
     * </pre>
     *
     * @param userUsernameOrEmail username or the user's email address
     * @return always <b>200 OK</b>
     */
    @RequestMapping(method = RequestMethod.POST, value = "/forgotPassword")
    public ResponseEntity forgotPassword(@RequestBody String userUsernameOrEmail) {
        asyncExecutor.run(() -> userService.forgotPassword(userUsernameOrEmail));
        return new ResponseEntity(HttpStatus.OK);
    }

    /**
     * <pre>
     * Forgot Password workflow (Step 2)
     *
     * <b>Required roles:</b> no role required
     * <b>Path:</b> POST {@value Core#RESOURCE_USER}/forgotPasswordConfirm/{confirmationKey}
     * </pre>
     *
     * @param confirmationKey forgot password confirmation key
     * @param newPassword     the new password
     * @return <b>200 OK</b> if everything goes well or <br>
     * <b>500 Bad RequestInternal Server Error</b> if an error occurred
     */
    @RequestMapping(method = RequestMethod.POST, value = "/forgotPasswordConfirm/{confirmationKey}")
    public ResponseEntity forgotPasswordConfirm(@PathVariable String confirmationKey, @RequestBody String newPassword) {
        userService.forgotPasswordConfirm(confirmationKey, newPassword);
        return new ResponseEntity(HttpStatus.OK);
    }

    /**
     * <pre>
     * Change email workflow (Step 1)
     *
     * <b>Required roles:</b> ROLE_USER
     * <b>Path:</b> POST {@value Core#RESOURCE_USER}/changeEmail
     * </pre>
     *
     * @param emailToBeConfirmed the new email address to be confirmed
     * @return <b>200 OK</b> if everything goes well or <br>
     * <b>409 Conflict</b> if a user with the given username or email already exists
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST, value = "/changeEmail")
    public ResponseEntity changeEmail(@AuthenticationPrincipal User user, @RequestBody String emailToBeConfirmed) {
        userService.changeEmail(user, emailToBeConfirmed);
        return new ResponseEntity(HttpStatus.OK);
    }

    /**
     * <pre>
     * Change email workflow (Step 2)
     *
     * <b>Required roles:</b> no role required
     * <b>Path:</b> GET {@value Core#RESOURCE_USER}/changeEmailConfirm/{confirmationKey}
     * </pre>
     *
     * @param confirmationKey change email confirmation key
     * @return <b>200 OK</b> if everything goes well or <br>
     * <b>409 Conflict</b> if a user with the given username or email already exists or <br>
     * <b>500 Bad RequestInternal Server Error</b> if an error occurred
     */
    @RequestMapping(method = RequestMethod.GET, value = "/changeEmailConfirm/{confirmationKey}")
    public ResponseEntity changeEmailConfirm(@PathVariable String confirmationKey) {
        userService.changeEmailConfirm(confirmationKey);
        return new ResponseEntity(HttpStatus.OK);
    }
}