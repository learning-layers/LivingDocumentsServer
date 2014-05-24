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
    public Node getDocumentNode(Session session, String nodeId) throws RepositoryException {
        Node rootNode = session.getRootNode();
        return rootNode.getNode(Core.LD_DOCUMENTS + "/" + nodeId);
    }

    @Override
    public Node createDocumentNode(Session session, String nodeId) throws RepositoryException {
        Node rootNode = session.getRootNode();
        Node documentsNode = rootNode.getNode(Core.LD_DOCUMENTS);
        Node documentNode = documentsNode.addNode(nodeId, Core.LD_DOCUMENT);
        session.save();
        return documentNode;
    }

    @Override
    public void addAllPrivileges(Session session, Node node) throws RepositoryException {
        addAllPrivileges(session, node.getPath());
    }

    @Override
    public void addAllPrivileges(Session session, String path) throws RepositoryException {
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
    public Node addComment(Session session, Node documentOrCommentNode, String comment) throws RepositoryException {
        Node commentsNode;
        if (documentOrCommentNode.getName().equals(Core.LD_COMMENTS_NODE)) {
            commentsNode = documentOrCommentNode;
        } else if (documentOrCommentNode.hasNode(Core.LD_COMMENTS_NODE)) {
            commentsNode = documentOrCommentNode.getNode(Core.LD_COMMENTS_NODE);
        } else {
            commentsNode = documentOrCommentNode.addNode(Core.LD_COMMENTS_NODE, JcrConstants.NT_UNSTRUCTURED);
        }
        Calendar currentTime = Calendar.getInstance();
        currentTime.setTimeInMillis(System.currentTimeMillis());
        Node commentNode = commentsNode.addNode(UUID.randomUUID().toString(), JcrConstants.NT_UNSTRUCTURED);
        commentNode.setProperty(Core.LD_MESSAGE_PROPERTY, comment);
        commentNode.addMixin(NodeType.MIX_CREATED);
        commentNode.setProperty(JcrConstants.JCR_LASTMODIFIED, currentTime);
        commentNode.setProperty(Core.JCR_LASTMODIFIED_BY, session.getUserID());
        session.save();
        return commentNode;
    }

    @Override
    public Node updateComment(Session session, Node commentNode, String comment) throws RepositoryException {
        if (!commentNode.getParent().getName().equals(Core.LD_COMMENTS_NODE)) {
            throw new InvalidNodeTypeDefinitionException("Parent of node '" +
                    commentNode.getName() + "' is not a " + Core.LD_COMMENTS_NODE + ".");
        }
        Calendar currentTime = Calendar.getInstance();
        currentTime.setTimeInMillis(System.currentTimeMillis());
        commentNode.setProperty(Core.LD_MESSAGE_PROPERTY, comment);
        commentNode.setProperty(JcrConstants.JCR_LASTMODIFIED, currentTime);
        commentNode.setProperty(Core.JCR_LASTMODIFIED_BY, session.getUserID());
        session.save();
        return commentNode;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Node> getComments(Node node) throws RepositoryException {
        Node commentsNode = node.getNode(Core.LD_COMMENTS_NODE);
        NodeIterator nodeIterator = commentsNode.getNodes();
        return IteratorUtils.toList(nodeIterator);
    }

    @Override
    public Node addFileNode(Session session, Node documentNode, InputStream inputStream, String fileName, String cmd) throws RepositoryException {
        if (!documentNode.getPrimaryNodeType().getName().equals(Core.LD_DOCUMENT)) {
            throw new RepositoryException("Argument is not a document node.");
        }

        ValueFactory factory = session.getValueFactory();
        Binary binary = factory.createBinary(inputStream);

        // create file node
        Node fileNode = null;
        Node docOrAttachNode = null;
        if ("attachment".equals(cmd)) {
            Node attachmentsNode = null;
            if (!documentNode.hasNode(Core.LD_ATTACHMENTS_NODE)) {
                attachmentsNode = documentNode.addNode(Core.LD_ATTACHMENTS_NODE, JcrConstants.NT_FOLDER);
            } else {
                attachmentsNode = documentNode.getNode(Core.LD_ATTACHMENTS_NODE);
            }
            docOrAttachNode = attachmentsNode;
        } else if ("main".equals(cmd)) {
            docOrAttachNode = documentNode;
        }
        if (docOrAttachNode != null) {
            if (docOrAttachNode.hasNode(fileName)) {
                // if file node exists but the content shall be updated
                fileNode = docOrAttachNode.getNode(fileName);
            } else {
                fileNode = docOrAttachNode.addNode(fileName, JcrConstants.NT_FILE);
            }
        }

        if (fileNode == null) {
            throw new RepositoryException("Could not create file node with cmd=[" + cmd + "] and filename=[" + fileName + "]");
        }

        // create resource node
        Node resourceNode = fileNode.addNode(JcrConstants.JCR_CONTENT, JcrConstants.NT_RESOURCE);
        resourceNode.setProperty(JcrConstants.JCR_DATA, binary);
        try {
            String mimeType = URLConnection.guessContentTypeFromStream(inputStream);
            resourceNode.setProperty(JcrConstants.JCR_MIMETYPE, mimeType);
        } catch (IOException e) {
            // do not set mimeType
        }
        Calendar currentTime = Calendar.getInstance();
        currentTime.setTimeInMillis(System.currentTimeMillis());
        resourceNode.setProperty(JcrConstants.JCR_LASTMODIFIED, currentTime);
        resourceNode.setProperty(Core.JCR_LASTMODIFIED_BY, session.getUserID());

        session.save();

        return fileNode;
    }

    @SuppressWarnings("unchecked")
    private JackrabbitSession adminLogin() throws RepositoryException {
        return (JackrabbitSession) repository.login(Core.ADMIN_CREDENTIALS);
    }
}
