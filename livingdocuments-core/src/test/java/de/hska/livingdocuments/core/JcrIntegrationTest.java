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

package de.hska.livingdocuments.core;

import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.api.security.user.AuthorizableExistsException;
import org.apache.jackrabbit.api.security.user.User;
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

    static final String TEST_USER = "testuser";
    Credentials userCredentials = new SimpleCredentials(TEST_USER, TEST_USER.toCharArray());
    static final String TEST_FOLDER = "testfolder";
    static final String TEST_PDF = "test.pdf";
    Credentials adminCredentials = new SimpleCredentials("admin", "admin".toCharArray());
    @Autowired
    Repository repository;

    @Before
    public void setUp() throws Exception {
        JackrabbitSession session = (JackrabbitSession) repository.login(adminCredentials);
        // Create test user
        UserManager userManager = session.getUserManager();
        try {
            userManager.createUser(TEST_USER, TEST_USER);
        } catch (AuthorizableExistsException e) {
            LOGGER.info("Test user already exists.");
        }
        // Create test folder
        Node root = session.getRootNode();
        try {
            root.addNode(TEST_FOLDER, JcrConstants.NT_FOLDER);
        } catch (ItemExistsException e) {
            LOGGER.info("Test folder already exists.");
        }

        session.save();
    }

    @After
    public void tearDown() throws Exception {
        JackrabbitSession session = (JackrabbitSession) repository.login(adminCredentials);
        // Remove test folder
        Node root = session.getRootNode();
        Node folderNode = root.getNode(TEST_FOLDER);
        folderNode.remove();
        // Remove test user
        UserManager userManager = session.getUserManager();
        User user = (User) userManager.getAuthorizable(TEST_USER);
        user.remove();

        session.save();
    }

    @Test
    public void thatFileNodeIsCreated() throws RepositoryException {
        Session session = repository.login(adminCredentials);

        Node fileNode;
        Node resultNode;
        Node root = session.getRootNode();

        Node folderNode = root.getNode(TEST_FOLDER);

        if (folderNode.hasNode(TEST_PDF)) {
            LOGGER.debug("File already exists. file=" + TEST_PDF);
        } else {
            InputStream in = JcrIntegrationTest.class.getResourceAsStream("/" + TEST_PDF);
            ValueFactory factory = session.getValueFactory();
            Binary binary = factory.createBinary(in);
            fileNode = folderNode.addNode(TEST_PDF, JcrConstants.NT_FILE);

            // create the mandatory child node - jcr:content
            resultNode = fileNode.addNode(JcrConstants.JCR_CONTENT, JcrConstants.NT_RESOURCE);
            resultNode.setProperty(JcrConstants.JCR_MIMETYPE, "application/pdf");
            resultNode.setProperty(JcrConstants.JCR_DATA, binary);
            Calendar lastModified = Calendar.getInstance();
            lastModified.setTimeInMillis(System.currentTimeMillis());
            resultNode.setProperty(JcrConstants.JCR_LASTMODIFIED, lastModified);

            session.save();
            IOUtils.closeQuietly(in);
            LOGGER.debug("Created '" + TEST_PDF + "' in " + TEST_FOLDER);
        }

        assertTrue("File node, '" + TEST_PDF + "', doesn't exist.", folderNode.hasNode(TEST_PDF));
        assertTrue("File content node, '" + TEST_PDF + "', doesn't exist.",
                folderNode.getNode(TEST_PDF).hasNode(JcrConstants.JCR_CONTENT));

        Node contentNode = folderNode.getNode(TEST_PDF).getNode(JcrConstants.JCR_CONTENT);
        Property dataProperty = contentNode.getProperty(JcrConstants.JCR_DATA);
        assertNotNull(dataProperty);

        //JcrUtils.readFile(fileNode);
    }
}
