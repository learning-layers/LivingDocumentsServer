/*
 *  Code contributed to the Learning Layers project
 *  http://www.learning-layers.eu
 *  Development is partly funded by the FP7 Programme of the European
 *  Commission under Grant Agreement FP7-ICT-318209.
 *  Copyright (c) 2016, Karlsruhe University of Applied Sciences.
 *  For a list of contributors see the AUTHORS file at the top-level directory
 *  of this distribution.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package de.hska.ld.core.service;

import de.hska.ld.core.persistence.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.Callable;

public interface UserService extends UserDetailsService, Service<User> {

    User findByUsername(String username);

    User findByEmail(String email);

    User addRoles(String username, String... roleNames);

    String encodePassword(String password);

    Boolean hasRole(User user, String roleName);

    List<User> findByRole(String roleName);

    List<byte[]> getAvatars(String userIdsString);

    void uploadAvatar(MultipartFile file, String name);

    void register(User user);

    User confirmRegistration(String confirmationKey);

    void runAs(User user, Runnable runnable);

    Callable callAs(User user, Callable callable);

    Page<User> getUsersPage(Integer pageNumber, Integer pageSize, String sortDirection, String sortProperty);

    Page<User> getUsersPage(Integer pageNumber, Integer pageSize, String sortDirection, String sortProperty, String searchTerm);

    Page<User> getUsersDisabledPage(Integer pageNumber, Integer pageSize, String sortDirection, String sortProperty);

    User activateUser(Long userid);

    User deactivateUser(Long userid);

    List<User> getMentionSuggestions(String term);

    User updatePassword(String password);

    User updateProfile(User user);

    void forgotPassword(String userKey);

    void forgotPasswordConfirm(String confirmationKey, String newPassword);

    void changeEmail(User user, String emailToBeConfirmed);

    void changeEmailConfirm(String confirmationKey);

    User findBySubIdAndIssuer(String subId, String issuer);
}
