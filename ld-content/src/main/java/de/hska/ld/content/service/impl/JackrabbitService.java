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

package de.hska.ld.content.service.impl;

import de.hska.ld.content.service.JcrService;
import de.hska.ld.content.util.Content;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.UserService;
import de.hska.ld.core.util.Core;
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
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.InvalidNodeTypeDefinitionException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.security.AccessControlEntry;
import javax.jcr.security.AccessControlList;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.Privilege;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.*;

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
            return (JackrabbitSession) repository.login(Core.ADMIN_CREDENTIALS);
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
    public Node getNode(Session session, String nodeId) throws RepositoryException {
        Node documentsNode = getDocumentsNode(session);
        return documentsNode.getNode(nodeId);
    }

    @Override
    public Node createDocumentNode(Session session, String title, String description) throws RepositoryException {
        Node documentsNode = getDocumentsNode(session);
        String documentNodeId = UUID.randomUUID().toString();
        if (documentsNode.hasNode(documentNodeId)) {
            throw new ItemExistsException("Document already exists.");
        }
        Node documentNode = documentsNode.addNode(documentNodeId, Content.LD_DOCUMENT);
        documentNode.setProperty(Content.LD_TITLE_PROPERTY, title);
        if (description != null) {
            documentNode.setProperty(Content.LD_DESCRIPTION_PROPERTY, description);
        }
        session.save();
        return documentNode;
    }

    @Override
    public void removeDocumentNode(Session session, String documentNodeId) throws RepositoryException {
        // TODO check permission
        Node documentNode = getNode(session, documentNodeId);
        if (!documentNode.getPrimaryNodeType().getName().equals(Content.LD_DOCUMENT)) {
            throw new ConstraintViolationException("Remove tag from global tag container is not allowed.");
        }
        documentNode.remove();
    }

    @Override
    public void addAllPrivileges(Session session, Node node) throws RepositoryException {
        /*String path = node.getPath();
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
    public Node addComment(Session session, Node node, String comment) throws RepositoryException {
        Node commentsNode;
        if (node.getName().equals(Content.LD_COMMENTS_NODE)) {
            commentsNode = node;
        } else if (node.hasNode(Content.LD_COMMENTS_NODE)) {
            commentsNode = node.getNode(Content.LD_COMMENTS_NODE);
        } else {
            commentsNode = node.addNode(Content.LD_COMMENTS_NODE, JcrConstants.NT_UNSTRUCTURED);
        }
        Calendar currentTime = Calendar.getInstance();
        currentTime.setTimeInMillis(System.currentTimeMillis());

        Node commentNode = commentsNode.addNode(UUID.randomUUID().toString(), JcrConstants.NT_UNSTRUCTURED);
        commentNode.setProperty(Content.LD_MESSAGE_PROPERTY, comment);
        commentNode.addMixin(NodeType.MIX_CREATED);
        commentNode.setProperty(JcrConstants.JCR_LASTMODIFIED, currentTime);
        commentNode.setProperty(Content.JCR_LASTMODIFIED_BY, session.getUserID());
        session.save();

        return commentNode;
    }

    @Override
    public Node updateComment(Session session, Node commentNode, String comment) throws RepositoryException {
        if (!commentNode.getParent().getName().equals(Content.LD_COMMENTS_NODE)) {
            throw new InvalidNodeTypeDefinitionException("Parent of node '" +
                    commentNode.getName() + "' is not a " + Content.LD_COMMENTS_NODE + ".");
        }
        Calendar currentTime = Calendar.getInstance();
        currentTime.setTimeInMillis(System.currentTimeMillis());
        commentNode.setProperty(Content.LD_MESSAGE_PROPERTY, comment);
        commentNode.setProperty(JcrConstants.JCR_LASTMODIFIED, currentTime);
        commentNode.setProperty(Content.JCR_LASTMODIFIED_BY, session.getUserID());
        session.save();
        return commentNode;
    }

    @Override
    public Node addTag(Session session, Node nodeToBeTagged, String tagName, String description) throws RepositoryException {
        // Get global tags container
        Node globalTagsNode = getNode(session, Content.LD_TAGS_NODE);

        // See if the needed tag node is available yet
        Node globalTagNode = searchDocumentsTagNode(session, tagName);

        if (globalTagNode == null) {
            // tag node has not been found, create a new one
            globalTagNode = globalTagsNode.addNode(UUID.randomUUID().toString(), JcrConstants.NT_UNSTRUCTURED);
            globalTagNode.setProperty(Content.LD_NAME_PROPERTY, tagName);
            globalTagNode.setProperty(Content.LD_DESCRIPTION_PROPERTY, description);
        }

        // Get nodes tags container
        Node tagsNode;
        if (!nodeToBeTagged.hasNode(Content.LD_TAGS_NODE)) {
            // if tags container isn't present add it
            tagsNode = nodeToBeTagged.addNode(Content.LD_TAGS_NODE, JcrConstants.NT_UNSTRUCTURED);
        } else {
            tagsNode = nodeToBeTagged.getNode(Content.LD_TAGS_NODE);
        }

        Node tagNode;
        String identifier = globalTagNode.getIdentifier();
        if (!tagsNode.hasNode(identifier)) {
            tagNode = tagsNode.addNode(identifier, JcrConstants.NT_UNSTRUCTURED);
            tagNode.setProperty(Content.LD_NAME_PROPERTY, tagName);
        } else {
            throw new ItemExistsException("Tag already exists.");
        }

        session.save();
        return tagNode;
    }

    @Override
    public void removeTag(Session session, Node taggedNode, String tagId) throws RepositoryException {
        // Do not remove the tag from the global tag container
        if (taggedNode.getPrimaryNodeType().getName().equals(Content.LD_DOCUMENT)) {
            throw new ConstraintViolationException("Remove tag from global tag container is not allowed.");
        }
        // Check if the tagged node has a tag container
        if (taggedNode.hasNode(Content.LD_TAGS_NODE)) {
            throw new ItemNotFoundException("The tagged node has no tag container.");
        }
        Node tagsNode = taggedNode.getNode(Content.LD_TAGS_NODE);
        if (tagsNode.hasNode(tagId)) {
            tagsNode.getNode(tagId).remove();
            session.save();
        } else {
            throw new ItemNotFoundException("No tag with id=" + tagId + " found in node=" + taggedNode.getName());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Node> getComments(Node node) throws RepositoryException {
        Node commentsNode = node.getNode(Content.LD_COMMENTS_NODE);
        NodeIterator nodeIterator = commentsNode.getNodes();
        return IteratorUtils.toList(nodeIterator);
    }

    @Override
    public Node addFileNode(Session session, Node documentNode, InputStream inputStream, String fileName, String cmd)
            throws RepositoryException {
        if (!documentNode.getPrimaryNodeType().getName().equals(Content.LD_DOCUMENT)) {
            throw new RepositoryException("Argument is not a document node.");
        }

        ValueFactory factory = session.getValueFactory();
        Binary binary = factory.createBinary(inputStream);

        // identify file node parent
        Node fileNode = null;
        Node fileNodeParent = null;
        if ("attachment".equals(cmd)) {
            Node attachmentsNode;
            if (!documentNode.hasNode(Content.LD_ATTACHMENTS_NODE)) {
                attachmentsNode = documentNode.addNode(Content.LD_ATTACHMENTS_NODE, JcrConstants.NT_FOLDER);
            } else {
                attachmentsNode = documentNode.getNode(Content.LD_ATTACHMENTS_NODE);
            }
            fileNodeParent = attachmentsNode;
        } else if ("main".equals(cmd)) {
            fileName = Content.LD_MAIN_FILE_NODE;
            fileNodeParent = documentNode;
        }
        // Create file node
        if (fileNodeParent != null) {
            if (fileNodeParent.hasNode(fileName)) {
                // if file node exists but the content shall be updated
                fileNode = fileNodeParent.getNode(fileName);
            } else {
                fileNode = fileNodeParent.addNode(fileName, JcrConstants.NT_FILE);
            }
        }

        if (fileNode == null) {
            throw new RepositoryException("Could not create file node with cmd=["
                    + cmd + "] and filename=[" + fileName + "]");
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
        resourceNode.setProperty(Content.JCR_LASTMODIFIED_BY, session.getUserID());

        session.save();

        return fileNode;
    }

    private Node searchDocumentsTagNode(Session session, String tagName) throws RepositoryException {
        // search for the tag node
        QueryManager queryManager = session.getWorkspace().getQueryManager();
        String expression = "//" +
                Content.LD_DOCUMENTS + "/" +
                Content.LD_TAGS_NODE + "/" +
                "element(*," + JcrConstants.NT_UNSTRUCTURED + ")" +
                "[@name='" + tagName + "']";
        Query query = queryManager.createQuery(expression, "xpath");
        QueryResult result = query.execute();
        NodeIterator nodeIt = result.getNodes();
        Node documentsTagNode = null;
        while (nodeIt.hasNext()) {
            documentsTagNode = nodeIt.nextNode();
        }
        return documentsTagNode;
    }

    private Node getDocumentsNode(Session session) throws RepositoryException {
        Node rootNode = session.getRootNode();
        return rootNode.getNode(Content.LD_DOCUMENTS);
    }
}
