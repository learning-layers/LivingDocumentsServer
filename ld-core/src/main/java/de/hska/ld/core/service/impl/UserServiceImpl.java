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

import de.hska.ld.core.exception.*;
import de.hska.ld.core.persistence.domain.Role;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.persistence.repository.UserRepository;
import de.hska.ld.core.service.MailService;
import de.hska.ld.core.service.RoleService;
import de.hska.ld.core.service.UserService;
import de.hska.ld.core.util.Core;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

public class UserServiceImpl extends AbstractService<User> implements UserService {

    EmailValidator emailValidator = EmailValidator.getInstance();

    @Autowired
    private UserRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleService roleService;

    @Autowired
    private MailService mailService;

    @Override
    public User findByUsername(String username) {
        return repository.findByUsername(username);
    }

    @Override
    public User findByEmail(String email) {
        return repository.findByEmail(email);
    }

    @Override
    public List<User> findByRole(String roleName) {
        return repository.findByRole(roleName);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = repository.findByUsernameOrEmail(username);
        return user;
    }

    @Override
    public User save(User user) {
        boolean isNew = user.getId() == null;
        User userFoundByUsername = findByUsername(user.getUsername());
        User userFoundByEmail = findByEmail(user.getEmail());
        if (isNew) {
            setNewUserFields(user, userFoundByUsername, userFoundByEmail);
        } else {
            // Check if a the current user wants to update an account owned by somebody else
            User currentUser = Core.currentUser();
            boolean isAdmin = hasRole(currentUser, Core.ROLE_ADMIN);
            if (!isAdmin && !currentUser.getId().equals(user.getId())) {
                throw new UserNotAuthorizedException();
            }
            if (userFoundByUsername != null && user.getUsername().equals(userFoundByUsername.getUsername()) &&
                    !user.getId().equals(userFoundByUsername.getId())) {
                throw new AlreadyExistsException("username");
            }
            if (userFoundByEmail != null && user.getEmail().equals(userFoundByEmail.getEmail()) &&
                    !user.getId().equals(userFoundByEmail.getId())) {
                throw new AlreadyExistsException("email");
            }
            User dbUser = repository.findOne(user.getId());
            user.setId(dbUser.getId());
            user.setEnabled(dbUser.isEnabled());
            user.setPassword(dbUser.getPassword());
            user.setRoleList(dbUser.getRoleList());
        }
        user = super.save(user);
        return user;
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
        filteredRoleList.stream().filter(role -> !hasRole(user, role.getName()))
                .forEach(role -> user.getRoleList().add(role));
        return save(user);
    }

    @Override
    public void register(User user) {
        User userFoundByUsername = findByUsername(user.getUsername());
        User userFoundByEmail = findByEmail(user.getEmail());
        setNewUserFields(user, userFoundByUsername, userFoundByEmail);
        user.setConfirmationKey(UUID.randomUUID().toString());
        user.setEnabled(false);
        user = super.save(user);
        //sendConfirmationMail(user);
    }

    @Override
    @Transactional
    public User confirmRegistration(String confirmationKey) {
        User user = repository.findByConfirmationKey(confirmationKey);
        if (user == null) {
            throw new NotFoundException("confirmationKey");
        }
        user.setEnabled(true);
        user.setConfirmationKey(null);
        user = super.save(user);
        return user;
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

    @Override
    public void runAs(User user, Runnable runnable) {
        Authentication authenticationBefore = SecurityContextHolder.getContext().getAuthentication();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user,
                user.getPassword(), user.getAuthorities()));
        try {
            runnable.run();
        } finally {
            SecurityContextHolder.getContext().setAuthentication(authenticationBefore);
        }
    }

    @Override
    public Page<User> getUsersPage(Integer pageNumber, Integer pageSize, String sortDirection, String sortProperty) {
        Sort.Direction direction;
        if (Sort.Direction.ASC.toString().equals(sortDirection)) {
            direction = Sort.Direction.ASC;
        } else {
            direction = Sort.Direction.DESC;
        }
        Pageable pageable = new PageRequest(pageNumber, pageSize, direction, sortProperty);
        User user = Core.currentUser();
        return repository.findAll(pageable);
    }

    @Override
    public Page<User> getUsersDisabledPage(Integer pageNumber, Integer pageSize, String sortDirection, String sortProperty) {
        Sort.Direction direction;
        if (Sort.Direction.ASC.toString().equals(sortDirection)) {
            direction = Sort.Direction.ASC;
        } else {
            direction = Sort.Direction.DESC;
        }
        Pageable pageable = new PageRequest(pageNumber, pageSize, direction, sortProperty);
        User user = Core.currentUser();
        return repository.findDisabledAll(pageable);
    }

    @Override
    @Transactional
    public User activateUser(Long userid) {
        User user = super.findById(userid);
        if (user != null) {
            user.setEnabled(true);
            super.save(user);
        }
        return user;
    }

    @Override
    public List<User> getMentionSuggestions(String term) {
        List<User> userList = repository.findMentionSuggestions(term + "%");
        return userList;
    }

    @Override
    public List<byte[]> getAvatars(String userIdsString) {
        List<byte[]> avatarList = new ArrayList<>();
        if (userIdsString != null) {
            String[] userIds = userIdsString.split(";");
            for (String idString : userIds) {
                User user = findById(Long.parseLong(idString));
                if (user != null) {
                    avatarList.add(user.getAvatar());
                }
            }
        }
        return avatarList;
    }

    @Override
    public void uploadAvatar(MultipartFile file, String name) {
        try {
            //String avatar = name + ";" + new String(file.getBytes());
            User user = Core.currentUser();
            user.setAvatar(file.getBytes());
            super.save(user);
        } catch (IOException e) {
            throw new ApplicationException();
        }
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

    private void setNewUserFields(User user, User userFoundByUsername, User userFoundByEmail) {
        // Check user input
        if (user.getPassword() == null) {
            throw new ValidationException("password");
        }
        if (userFoundByUsername != null) {
            throw new AlreadyExistsException("username");
        }
        if (userFoundByEmail != null) {
            throw new AlreadyExistsException("email");
        }
        if (user.getEmail() != null && !emailValidator.isValid(user.getEmail())) {
            throw new ValidationException("email");
        }
        user.setId(null);
        user.setCreatedAt(new Date());
        user.setConfirmationKey(UUID.randomUUID().toString());
        user.setEnabled(true);
        String hashedPwd = encodePassword(user.getPassword());
        user.setPassword(hashedPwd);
        createRoleListForUser(user);
    }

    @SuppressWarnings("unchecked")
    private void sendConfirmationMail(User user) {
        Locale locale = LocaleContextHolder.getLocale();
        ResourceBundle bundle = ResourceBundle.getBundle("messages", locale);
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        Map model = new HashMap<>();
        model.put("subject", bundle.getString("user.confirmation.subject"));
        model.put("text", bundle.getString("user.confirmation.text"));
        model.put("link", bundle.getString("user.confirmation.link"));
        model.put("confirmationUrl", request.getContextPath() + "/users/confirmRegistration/" + user.getConfirmationKey());

        mailService.sendMail(user, "user_confirmation.vm", model);
    }

    @Override
    public UserRepository getRepository() {
        return repository;
    }
}
