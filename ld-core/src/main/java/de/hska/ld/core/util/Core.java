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
import de.hska.ld.core.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class Core {

    @Autowired
    private UserService userService;

    private static UserService userServiceStatic;

    private static Core INSTANCE = null;

    @Autowired
    public Core(UserService userService) {
        this.userService = userService;
    }

    @PostConstruct
    public void postConstruct() {
        userServiceStatic = this.userService;
        if (INSTANCE == null) {
            INSTANCE = this;
        }
    }

    public UserService getUserService() {
        return userService;
    }

    public static Core getInstance() {
        return Core.INSTANCE;
    }

    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_USER = "ROLE_USER";

    public static final String RESOURCE_API = "/api";
    public static final String RESOURCE_USER = RESOURCE_API + "/users";
    public static final String RESOURCE_ROLE = RESOURCE_API + "/roles";
    public static final String RESOURCE_INFO = RESOURCE_API + "/info";
    public static final String RESOURCE_LOG = RESOURCE_API + "/log";

    public static final String BOOTSTRAP_USER = "user";
    public static final String BOOTSTRAP_ADMIN = "admin";

    public static boolean isAuthenticated() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return securityContext != null && securityContext.getAuthentication() != null &&
                securityContext.getAuthentication().isAuthenticated();
    }

    private static Map<String, User> currentUserMap = new HashMap<>();

    public static User currentUser() {
        if (isAuthenticated()) {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof User) {
                return (User) principal;
            } else {
                Map principalMap = (Map) principal;
                User user = null;
                String subId = (String) principalMap.get("sub");
                if (!currentUserMap.containsKey(subId)) {
                    user = currentUserBySubIdAndIssuer(subId, (String) principalMap.get("iss"));
                    if (user != null) {
                        currentUserMap.put(subId, user);
                    }
                } else {
                    user = currentUserMap.get(subId);
                    Long currentTime = (new Date()).getTime();
                    Long dbLoadTime = user.getFetchedFromDB().getTime();
                    if (currentTime - dbLoadTime > 20000) {
                        currentUserMap.remove(subId);
                        return Core.currentUser();
                    }
                }
                return user;
            }
        }
        return null;
    }

    public static User currentUserBySubIdAndIssuer(String subId, String issuer) {
        return Core.userServiceStatic.findBySubIdAndIssuer(subId, issuer);
    }

    public static boolean isAdmin() {
        User currentUser = currentUser();
        return currentUser != null && currentUser.getRoleList().stream().anyMatch(r -> r.getName().equals(ROLE_ADMIN));
    }
}
