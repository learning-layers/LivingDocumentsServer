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

package de.hska.ld.content;

import de.hska.ld.content.service.JcrService;
import de.hska.ld.core.AbstractIntegrationTest;
import de.hska.ld.core.fixture.CoreFixture;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.UserService;
import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jcr.*;
import java.io.InputStream;
import java.util.Calendar;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class JcrIntegrationTest extends AbstractIntegrationTest {
    static final Logger LOGGER = Logger.getLogger(JcrIntegrationTest.class);

    static final String TEST_USER = "user";
    static final String TEST_ROOT = "testRoot";
    static final String TEST_PDF = "test.pdf";

    @Autowired
    UserService userService;

    @Autowired
    JcrService jcrService;

    User user;
    User adminUser;

    @Before
    public void setUp() throws Exception {
        adminUser = userService.findByUsername("admin");
        init();
        // Create test folder
        Session adminSession = jcrService.login(adminUser);
        try {
            Node testRoot = null;
            Node root = adminSession.getRootNode();
            try {
                testRoot = root.addNode(TEST_ROOT, JcrConstants.NT_UNSTRUCTURED);
            } catch (ItemExistsException e) {
                LOGGER.info("Test folder already exists.");
            }
            jcrService.addAllPrivileges(adminSession, testRoot);
            adminSession.save();
        } finally {
            adminSession.logout();
        }

        // Create test user
        user = userService.findByUsername("user");
        jcrService.login(user);
    }

    @After
    public void tearDown() throws Exception {
        init();
    }

    private void init() throws RepositoryException {
        JackrabbitSession adminSession = jcrService.login(adminUser);
        try {
            // Remove test folder
            Node root = adminSession.getRootNode();
            Node testRoot = root.getNode(TEST_ROOT);
            if (testRoot != null) {
                testRoot.remove();
            }
            // Remove test user
            UserManager userManager = adminSession.getUserManager();
            org.apache.jackrabbit.api.security.user.User user =
                    (org.apache.jackrabbit.api.security.user.User) userManager.getAuthorizable(TEST_USER);
            if (user != null) {
                user.remove();
            }
            if (testRoot != null || user != null) {
                adminSession.save();
            }
        } catch (PathNotFoundException e) {
            LOGGER.info("Test folder doesn't exists.");
        } finally {
            adminSession.logout();
        }
    }

    @Test
    public void thatFileNodeIsCreated() throws RepositoryException {
        Node fileNode, resultNode, testRoot;
        Session session = jcrService.login(user);
        InputStream in = JcrIntegrationTest.class.getResourceAsStream("/" + TEST_PDF);
        try {
            testRoot = session.getNode("/" + TEST_ROOT);

            ValueFactory factory = session.getValueFactory();
            Binary binary = factory.createBinary(in);
            fileNode = testRoot.addNode(TEST_PDF, JcrConstants.NT_FILE);

            // create the mandatory child node - jcr:content
            resultNode = fileNode.addNode(JcrConstants.JCR_CONTENT, JcrConstants.NT_RESOURCE);
            resultNode.setProperty(JcrConstants.JCR_MIMETYPE, "application/pdf");
            resultNode.setProperty(JcrConstants.JCR_DATA, binary);
            Calendar lastModified = Calendar.getInstance();
            lastModified.setTimeInMillis(System.currentTimeMillis());
            resultNode.setProperty(JcrConstants.JCR_LASTMODIFIED, lastModified);

            session.save();

            LOGGER.debug("Created '" + TEST_PDF + "' in " + TEST_ROOT);
            assertTrue("File node, '" + TEST_PDF + "', doesn't exist.", testRoot.hasNode(TEST_PDF));
            assertTrue("File content node '" + TEST_PDF + "', doesn't exist.",
                    testRoot.getNode(TEST_PDF).hasNode(JcrConstants.JCR_CONTENT));

            Node contentNode = testRoot.getNode(TEST_PDF).getNode(JcrConstants.JCR_CONTENT);
            Property dataProperty = contentNode.getProperty(JcrConstants.JCR_DATA);
            assertNotNull(dataProperty);
        } finally {
            session.logout();
            IOUtils.closeQuietly(in);
        }
    }

    @Test
    public void thatUserHasNoAccessToRootNode() throws RepositoryException {
        Session session = jcrService.login(user);
        try {
            Node root = session.getRootNode();
            root.addNode("thatUserHasNoAccessToRootNode");
            session.save();
        } catch (AccessDeniedException e) {
            expectedException = e;
        } finally {
            session.logout();
        }
        assertNotNull(expectedException);
        assertTrue(expectedException instanceof AccessDeniedException);
    }

    @Test
    public void createNodeWithAttachment() throws RepositoryException {
        Session session = jcrService.login(user);
        try {
            Node testRoot = session.getNode("/" + TEST_ROOT);
            Node nodeWithAttachment = testRoot.addNode("nodeWithAttachment", JcrConstants.NT_UNSTRUCTURED);
            nodeWithAttachment.setProperty("message", "Node with attachment");
            Node attachedNode = nodeWithAttachment.addNode("attachedNode");
            attachedNode.setProperty("message", "Attached node");
            session.save();

            // Get message property from the attached node
            Property property = attachedNode.getProperties("message").nextProperty();
            LOGGER.info(property.getValue().getString());
        } finally {
            session.logout();
        }
    }

    @Test
    public void thatUserHasNoAccessToNodesFromOtherUsers() throws RepositoryException {
        User otherUser = userService.save(CoreFixture.newUser(), "pass");

        Session session = jcrService.login(otherUser);
        Node testRoot = session.getNode("/" + TEST_ROOT);
        testRoot.addNode("thatUserHasNoAccessToNodesFromOtherUsers_1", JcrConstants.NT_UNSTRUCTURED);
        session.save();
        session.logout();

        session = jcrService.login(user);
        Node testNode = session.getNode("/" + TEST_ROOT + "/" + "thatUserHasNoAccessToNodesFromOtherUsers_1");
        testNode.addNode("thatUserHasNoAccessToNodesFromOtherUsers_2", JcrConstants.NT_UNSTRUCTURED);
        session.save();
        session.logout();
    }
}
