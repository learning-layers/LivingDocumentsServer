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

package de.hska.ld.core.util;

import de.hska.ld.core.persistence.domain.User;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.jcr.Credentials;
import javax.jcr.SimpleCredentials;

public class Core {

    public static final Credentials ADMIN_CREDENTIALS = new SimpleCredentials("admin", "admin".toCharArray());

    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_USER = "ROLE_USER";

    public static final String API_RESOURCE = "/api";
    public static final String RESOURCE_USER = API_RESOURCE + "/users";
    public static final String RESOURCE_ROLE = API_RESOURCE + "/roles";
    public static final String RESOURCE_INFO = API_RESOURCE + "/info";

    public static final String BOOTSTRAP_USER = "user";
    public static final String BOOTSTRAP_ADMIN = "admin";

    public static boolean isAuthenticated() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return securityContext != null && securityContext.getAuthentication() != null &&
                securityContext.getAuthentication().isAuthenticated();
    }

    public static User currentUser() {
        if (isAuthenticated()) {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof User) {
                return (User) principal;
            }
        }
        return null;
    }

    public static boolean isAdmin() {
        User currentUser = currentUser();
        return currentUser != null && currentUser.getRoleList().stream().anyMatch(r -> r.getName().equals(ROLE_ADMIN));
    }
}
