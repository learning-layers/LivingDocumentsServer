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

package de.hska.livingdocuments.core.service.impl;

import de.hska.livingdocuments.core.persistence.domain.User;
import de.hska.livingdocuments.core.service.JcrService;
import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.jackrabbit.core.security.principal.EveryonePrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import javax.jcr.*;
import javax.jcr.security.AccessControlEntry;
import javax.jcr.security.AccessControlList;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.Privilege;
import java.util.NoSuchElementException;

public class JackrabbitService implements JcrService {
    private static final Logger LOGGER = LoggerFactory.getLogger(JackrabbitService.class);

    private static final Credentials ADMIN_CREDENTIALS = new SimpleCredentials("admin", "admin".toCharArray());

    @Autowired
    private Repository repository;

    @Autowired
    private Environment env;

    @Override
    @SuppressWarnings("unchecked")
    public JackrabbitSession login(User user) {
        String pwd = env.getProperty("module.core.repository.password");
        Credentials credentials = new SimpleCredentials(user.getUsername(), pwd.toCharArray());
        try {
            return (JackrabbitSession) repository.login(credentials);
        } catch (RepositoryException e) {
            if (e instanceof LoginException) {
                LOGGER.info("Create user {}", user);
                JackrabbitSession adminSession = null;
                try {
                    adminSession = (JackrabbitSession) repository.login(ADMIN_CREDENTIALS);
                    UserManager userManager = adminSession.getUserManager();
                    userManager.createUser(user.getUsername(), pwd);
                    adminSession.save();
                    return (JackrabbitSession) repository.login(credentials);
                } catch (RepositoryException e1) {
                    LOGGER.error("Login failed", e);
                    return null;
                } finally {
                    if (adminSession != null) {
                        adminSession.logout();
                    }
                }
            } else {
                LOGGER.error("Login failed", e);
                return null;
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public JackrabbitSession adminLogin() throws RepositoryException {
        return (JackrabbitSession) repository.login(ADMIN_CREDENTIALS);
    }

    @Override
    public void addAllPrivileges(Node node, Session adminSession) throws RepositoryException {
        addAllPrivileges(node.getPath(), adminSession);
    }

    @Override
    public void addAllPrivileges(String path, Session adminSession) throws RepositoryException {
        AccessControlManager aMgr = adminSession.getAccessControlManager();

        // create a privilege set with jcr:all
        Privilege[] privileges = new Privilege[]{aMgr.privilegeFromName(Privilege.JCR_ALL)};
        AccessControlList acl;
        try {
            // get first applicable policy (for nodes w/o a policy)
            acl = (AccessControlList) aMgr.getApplicablePolicies(path).nextAccessControlPolicy();
        } catch (NoSuchElementException e) {
            // else node already has a policy, get that one
            acl = (AccessControlList) aMgr.getPolicies(path)[0];
        }
        // remove all existing entries
        for (AccessControlEntry e : acl.getAccessControlEntries()) {
            acl.removeAccessControlEntry(e);
        }
        // add a new one for the special "everyone" principal
        acl.addAccessControlEntry(EveryonePrincipal.getInstance(), privileges);

        // the policy must be re-set
        aMgr.setPolicy(path, acl);
    }
}
