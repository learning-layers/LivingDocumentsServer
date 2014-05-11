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

package de.hska.livingdocuments.sandbox;

import de.hska.livingdocuments.core.service.JcrService;
import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.JcrConstants;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jcr.*;
import java.io.InputStream;
import java.util.Calendar;

public class DataGenerator {

    @Autowired
    public void init(JcrService jcrService) throws RepositoryException {
        InputStream in = null;
        Session session = null;
        try {
            in = DataGenerator.class.getResourceAsStream("/" + "sandbox.pdf");
            session = jcrService.adminLogin();

            Node root = session.getRootNode();

            ValueFactory factory = session.getValueFactory();
            Binary binary = factory.createBinary(in);

            Node sandboxDocument = root.addNode("sandboxDocument", JcrConstants.NT_UNSTRUCTURED);
            sandboxDocument.setProperty("description", "Sandbox Node with PDF-File");

            Node fileNode = sandboxDocument.addNode("fileNode", JcrConstants.NT_FILE);

            // create the mandatory child node - jcr:content
            Node resourceNode = fileNode.addNode(JcrConstants.JCR_CONTENT, JcrConstants.NT_RESOURCE);
            resourceNode.setProperty(JcrConstants.JCR_MIMETYPE, "application/pdf");
            resourceNode.setProperty(JcrConstants.JCR_DATA, binary);
            Calendar lastModified = Calendar.getInstance();
            lastModified.setTimeInMillis(System.currentTimeMillis());
            resourceNode.setProperty(JcrConstants.JCR_LASTMODIFIED, lastModified);

            Node commentContainer = sandboxDocument.addNode("commentContainer", JcrConstants.NT_UNSTRUCTURED);
            jcrService.addAllPrivileges(commentContainer, session);

            Node helloWorldComment = commentContainer.addNode("helloWorldComment", JcrConstants.NT_UNSTRUCTURED);
            helloWorldComment.setProperty("message", "Hello World!");

            session.save();
        } finally {
            if (session != null) {
                session.logout();
            }
            IOUtils.closeQuietly(in);
        }
    }
}
