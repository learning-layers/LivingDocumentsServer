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
import de.hska.livingdocuments.core.service.UserService;
import de.hska.livingdocuments.core.util.Core;
import org.apache.commons.collections.IteratorUtils;
import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.jackrabbit.core.security.principal.EveryonePrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import javax.jcr.*;
import javax.jcr.nodetype.InvalidNodeTypeDefinitionException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.security.AccessControlEntry;
import javax.jcr.security.AccessControlList;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.Privilege;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

public class JackrabbitService implements JcrService {
    private static final Logger LOGGER = LoggerFactory.getLogger(JackrabbitService.class);

    @Autowired
    private Repository repository;

    @Autowired
    private UserService userService;

    @Autowired
    private Environment env;

    @Override
    @SuppressWarnings("unchecked")
    public JackrabbitSession login(User user) throws RepositoryException {
        if (userService.hasRole(user, Core.ROLE_ADMIN)) {
            return adminLogin();
        }
        String pwd = env.getProperty("module.core.repository.password");
        Credentials credentials = new SimpleCredentials(user.getUsername(), pwd.toCharArray());
        try {
            return (JackrabbitSession) repository.login(credentials);
        } catch (RepositoryException e) {
            if (e instanceof LoginException) {
                LOGGER.info("Create user {}", user);
                JackrabbitSession adminSession = null;
                try {
                    adminSession = (JackrabbitSession) repository.login(Core.ADMIN_CREDENTIALS);
                    UserManager userManager = adminSession.getUserManager();
                    userManager.createUser(user.getUsername(), pwd);
                    adminSession.save();
                    return (JackrabbitSession) repository.login(credentials);
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
    public void addAllPrivileges(Node node, Session session) throws RepositoryException {
        addAllPrivileges(node.getPath(), session);
    }

    @Override
    public void addAllPrivileges(String path, Session session) throws RepositoryException {
        AccessControlManager aMgr = session.getAccessControlManager();

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

    @Override
    public Node addComment(Session session, Node documentNode, String comment) throws RepositoryException {
        Node commentsNode;
        if (documentNode.getName().equals(Core.LD_COMMENTS_NODE)) {
            commentsNode = documentNode;
        } else if (documentNode.hasNode(Core.LD_COMMENTS_NODE)) {
            commentsNode = documentNode.getNode(Core.LD_COMMENTS_NODE);
        } else {
            commentsNode = documentNode.addNode(Core.LD_COMMENTS_NODE, JcrConstants.NT_UNSTRUCTURED);
        }
        Calendar currentTime = Calendar.getInstance();
        currentTime.setTimeInMillis(System.currentTimeMillis());
        Node commentNode = commentsNode.addNode(UUID.randomUUID().toString(), JcrConstants.NT_UNSTRUCTURED);
        commentNode.setProperty(Core.LD_MESSAGE_PROPERTY, comment);
        commentNode.addMixin(NodeType.MIX_CREATED);
        commentNode.setProperty(JcrConstants.JCR_LASTMODIFIED, currentTime);
        commentNode.setProperty(Core.JCR_LASTMODIFIED_BY, session.getUserID());
        return commentNode;
    }

    @Override
    public Node updateComment(Session session, Node commentNode, String comment) throws RepositoryException {
        if (commentNode.getParent().getName().equals(Core.LD_COMMENTS_NODE)) {
            throw new InvalidNodeTypeDefinitionException("Parent of node '" +
                    commentNode.getName() + "' is not a " + Core.LD_COMMENTS_NODE + ".");
        }
        Calendar currentTime = Calendar.getInstance();
        currentTime.setTimeInMillis(System.currentTimeMillis());
        commentNode.setProperty(Core.LD_MESSAGE_PROPERTY, comment);
        commentNode.setProperty(JcrConstants.JCR_LASTMODIFIED, currentTime);
        commentNode.setProperty(Core.JCR_LASTMODIFIED_BY, session.getUserID());
        return commentNode;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Node> getComments(Session documentNode) throws RepositoryException {
        Node commentsNode = documentNode.getNode(Core.LD_COMMENTS_NODE);
        NodeIterator nodeIterator = commentsNode.getNodes();
        return IteratorUtils.toList(nodeIterator);
    }

    @Override
    public Node addFileNode(Session session, Node documentNode, InputStream inputStream) throws RepositoryException {
        ValueFactory factory = session.getValueFactory();
        Binary binary = factory.createBinary(inputStream);

        Node fileNode;
        if (documentNode.getName().equals(Core.LD_FILE_NODE)) {
            fileNode = documentNode;
        } else if (documentNode.hasNode(Core.LD_FILE_NODE)) {
            fileNode = documentNode.getNode(Core.LD_FILE_NODE);
        } else {
            fileNode = documentNode.addNode(Core.LD_FILE_NODE, JcrConstants.NT_FILE);
        }

        Node resourceNode = fileNode.addNode(JcrConstants.JCR_CONTENT, JcrConstants.NT_RESOURCE);
        try {
            String mimeType = URLConnection.guessContentTypeFromStream(inputStream);
            resourceNode.setProperty(JcrConstants.JCR_MIMETYPE, mimeType);
        } catch (IOException e) {
            // do not set mimeType
        }
        resourceNode.setProperty(JcrConstants.JCR_DATA, binary);
        Calendar currentTime = Calendar.getInstance();
        currentTime.setTimeInMillis(System.currentTimeMillis());
        resourceNode.addMixin(NodeType.MIX_CREATED);
        resourceNode.setProperty(JcrConstants.JCR_LASTMODIFIED, currentTime);
        resourceNode.setProperty(Core.JCR_LASTMODIFIED_BY, session.getUserID());

        return fileNode;
    }

    @SuppressWarnings("unchecked")
    private JackrabbitSession adminLogin() throws RepositoryException {
        return (JackrabbitSession) repository.login(Core.ADMIN_CREDENTIALS);
    }
}
