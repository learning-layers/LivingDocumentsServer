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

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jcr.*;

import static org.junit.Assert.assertNotNull;

public class JcrIntegrationTest extends AbstractIntegrationTest {

    Credentials credentials = new SimpleCredentials("admin", "admin".toCharArray());

    @Autowired
    Repository repository;

    @Test
    public void dummyTest() throws RepositoryException {
        Session session = repository.login(credentials);

        Node root = session.getRootNode();
        String nodeName = "testNode";
        Node node = root.getNode(nodeName);
        if (!root.hasNode(nodeName)) {
            session.save();
        }

        assertNotNull("Node is null.", node);
    }

    @Test
    public void testAddFileIfDoesNotExist() {
        /*@SuppressWarnings("unused")
        Node node = (Node) template.execute(new JcrCallback() {
            @SuppressWarnings("unchecked")
            public Object doInJcr(Session session) throws RepositoryException, IOException {
                Node resultNode = null;
                Node root = session.getRootNode();
                LOGGER.info("starting from root node.  node={}", root);

                // should have been created in previous test
                Node folderNode = root.getNode(nodeName);
                String fileName = "test.pdf";

                if (folderNode.hasNode(fileName)) {
                    LOGGER.debug("File already exists.  file={}", fileName);
                } else {
                    InputStream in = JcrIntegrationTest.class.getResourceAsStream("/" + fileName);
                    ValueFactory factory = session.getValueFactory();
                    Binary binary = factory.createBinary(in);
                    Node fileNode = folderNode.addNode(fileName, JcrConstants.NT_FILE);

                    // create the mandatory child node - jcr:content
                    resultNode = fileNode.addNode(JcrConstants.JCR_CONTENT, JcrConstants.NT_RESOURCE);
                    resultNode.setProperty(JcrConstants.JCR_MIMETYPE, "application/pdf");
                    //resultNode.setProperty(JcrConstants.JCR_ENCODING, "UTF-8");
                    resultNode.setProperty(JcrConstants.JCR_DATA, binary);
                    Calendar lastModified = Calendar.getInstance();
                    lastModified.setTimeInMillis(System.currentTimeMillis());
                    resultNode.setProperty(JcrConstants.JCR_LASTMODIFIED, lastModified);

                    session.save();
                    IOUtils.closeQuietly(in);
                    LOGGER.debug("Created '{}' file in folder.", fileName);
                }

                assertTrue("File node, '" + fileName + "', doesn't exist.", folderNode.hasNode(fileName));
                assertTrue("File content node, '" + fileName + "', doesn't exist.",
                        folderNode.getNode(fileName).hasNode(JcrConstants.JCR_CONTENT));

                Node contentNode = folderNode.getNode(fileName).getNode(JcrConstants.JCR_CONTENT);
                Property dataProperty = contentNode.getProperty(JcrConstants.JCR_DATA);
                assertNotNull(dataProperty);

                return resultNode;
            }
        });*/
    }
}
