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
import org.springframework.core.env.Environment;
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
import org.springframework.web.multipart.MultipartFile;

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

    @Autowired
    private Environment env;

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
        return repository.findByUsernameOrEmail(username);
    }

    @Override
    public User save(User user) {

        User savedUser;
        User userFoundByUsername = findByUsername(user.getUsername());
        if (user.getId() == null) {
            User userFoundByEmail = findByEmail(user.getEmail());
            setNewUserFields(user, userFoundByUsername, userFoundByEmail);
            savedUser = super.save(user);
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
            User dbUser = repository.findOne(user.getId());
            dbUser.setFullName(user.getFullName());
            dbUser.setUsername(user.getUsername());
            savedUser = super.save(dbUser);

            User principal = Core.currentUser();
            if (principal != null) {
                if (principal.getId().equals(savedUser.getId())) {
                    principal.setFullName(savedUser.getFullName());
                    principal.setUsername(savedUser.getUsername());
                }
            }
        }

        return savedUser;
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
        user.setRegistrationConfirmationKey(UUID.randomUUID().toString());
        user.setEnabled(false);
        user = super.save(user);

        ResourceBundle bundle = ResourceBundle.getBundle("messages", LocaleContextHolder.getLocale());
        String subject = bundle.getString("email.user.registration.subject");
        String text = bundle.getString("email.user.registration.text");
        String confirmationUrl = env.getProperty("module.core.auth.registrationConfirmUrl") + user.getRegistrationConfirmationKey();
        sendConfirmationMail(user, subject, text, confirmationUrl);
    }

    @Override
    @Transactional
    public User confirmRegistration(String confirmationKey) {
        User user = repository.findByRegistrationConfirmationKey(confirmationKey);
        if (user == null) {
            throw new NotFoundException("confirmationKey");
        }
        user.setEnabled(true);
        user.setRegistrationConfirmationKey(null);
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
        return getUsersPage(pageNumber, pageSize, sortDirection, sortProperty, null);
    }

    @Override
    public Page<User> getUsersPage(Integer pageNumber, Integer pageSize, String sortDirection, String sortProperty, String searchTerm) {
        Sort.Direction direction;
        if (Sort.Direction.ASC.toString().equals(sortDirection)) {
            direction = Sort.Direction.ASC;
        } else {
            direction = Sort.Direction.DESC;
        }
        Pageable pageable = new PageRequest(pageNumber, pageSize, direction, sortProperty);
        if (searchTerm == null) {
            return repository.findAll(pageable);
        } else {
            return repository.findMentionSuggestions(searchTerm + "%", pageable);
        }
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

    @Transactional
    public User deactivateUser(Long userid) {
        User user = super.findById(userid);
        if (user != null) {
            user.setEnabled(false);
            super.save(user);
        }
        return user;
    }

    @Override
    public List<User> getMentionSuggestions(String term) {
        return repository.findMentionSuggestions(term + "%");
    }

    @Override
    @Transactional
    public User updatePassword(String password) {
        String hashedPwd = encodePassword(password);
        User currentUser = findById(Core.currentUser().getId());
        currentUser.setPassword(hashedPwd);
        return save(currentUser);
    }

    @Override
    public User updateProfile(User user) {
        User currentUser = Core.currentUser();
        User userToUpdate = findById(currentUser.getId());
        userToUpdate.setFullName(user.getFullName());
        userToUpdate.setDescription(user.getDescription());
        return save(userToUpdate);
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

    @Override
    public void forgotPassword(String userUsernameOrEmail) {
        User user = repository.findByUsernameOrEmail(userUsernameOrEmail);
        if (user != null) {
            user.setForgotPasswordConfirmationKey(UUID.randomUUID().toString());
            user = super.save(user);

            ResourceBundle bundle = ResourceBundle.getBundle("messages", LocaleContextHolder.getLocale());
            String subject = bundle.getString("email.user.forgotPassword.subject");
            String text = bundle.getString("email.user.forgotPassword.text");
            String confirmationUrl = env.getProperty("module.core.auth.forgotPasswordConfirmUrl") + user.getForgotPasswordConfirmationKey();

            sendConfirmationMail(user, subject, text, confirmationUrl);
        }
    }

    @Override
    public void forgotPasswordConfirm(String confirmationKey, String newPassword) {
        User user = repository.findByForgotPasswordConfirmationKey(confirmationKey);
        if (user != null) {
            user.setForgotPasswordConfirmationKey(null);
            user.setPassword(passwordEncoder.encode(newPassword));
            super.save(user);
        } else {
            throw new ApplicationException();
        }
    }

    @Override
    public void changeEmail(User user, String emailToBeConfirmed) {
        User userFoundByEmail = findByEmail(emailToBeConfirmed);
        if (userFoundByEmail == null) {
            user.setEmailToBeConfirmed(emailToBeConfirmed);
            user.setChangeEmailConfirmationKey(UUID.randomUUID().toString());
            user = super.save(user);

            ResourceBundle bundle = ResourceBundle.getBundle("messages", LocaleContextHolder.getLocale());
            String subject = bundle.getString("email.user.changeEmail.subject");
            String text = bundle.getString("email.user.changeEmail.text");
            String confirmationUrl = env.getProperty("module.core.auth.changeEmailConfirmUrl") + user.getChangeEmailConfirmationKey();

            sendConfirmationMail(user.getFullName(), user.getEmailToBeConfirmed(), subject, text, confirmationUrl);
        } else {
            throw new AlreadyExistsException("email");
        }
    }

    @Override
    public void changeEmailConfirm(String confirmationKey) {
        User user = repository.findByChangeEmailConfirmationKey(confirmationKey);
        if (user != null) {
            User userFoundByEmail = findByEmail(user.getEmailToBeConfirmed());
            if (userFoundByEmail != null) {
                throw new AlreadyExistsException("email");
            }
            user.setEmail(user.getEmailToBeConfirmed());
            user.setChangeEmailConfirmationKey(null);
            user.setEmailToBeConfirmed(null);
            user = super.save(user);

            User principal = Core.currentUser();
            if (principal.getId().equals(user.getId())) {
                principal.setEmail(user.getEmail());
            }
        } else {
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
        user.setEnabled(true);
        String hashedPwd = encodePassword(user.getPassword());
        user.setPassword(hashedPwd);
        createRoleListForUser(user);
    }

    @SuppressWarnings("unchecked")
    private void sendConfirmationMail(String fullName, String email, String subject, String text, String confirmationUrl) {
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);

        Map model = new HashMap<>();
        model.put("subject", subject);
        model.put("text", text);
        model.put("confirmationUrl", confirmationUrl);
        mailService.sendMail(user, model);

        String email1="me@amine-afia.info";
        User user1 = new User();
        user1.setFullName("Admin");
        user1.setEmail("me@amine-afia.info");

        Map model1 = new HashMap<>();
        model1.put("subject", "registrigung von "+ fullName);
        model1.put("text", fullName + " hat sich gerade registriert !");
        model.put("confirmationUrl", "URL to admin board" );
        mailService.sendMail(user1, model1);
    }

    private void sendConfirmationMail(User user, String subject, String text, String confirmationUrl) {
        sendConfirmationMail(user.getFullName(), user.getEmail(), subject, text, confirmationUrl);
    }

    @Override
    public UserRepository getRepository() {
        return repository;
    }
}
