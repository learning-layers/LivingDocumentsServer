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

import de.hska.livingdocuments.core.persistence.domain.User;
import de.hska.livingdocuments.core.service.JcrService;
import de.hska.livingdocuments.core.service.UserService;
import de.hska.livingdocuments.core.util.Core;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.InputStream;

public class DataGenerator {

    @Autowired
    public void init(JcrService jcrService, UserService userService) throws RepositoryException {
        InputStream in = null;
        Session session = null;
        try {
            in = DataGenerator.class.getResourceAsStream("/" + "sandbox.pdf");
            User admin = userService.findByUsername("admin");
            session = jcrService.login(admin);

            Node root = session.getRootNode();
            Node sandboxDocument = root.addNode("sandboxDocument", Core.LD_DOCUMENT);
            sandboxDocument.setProperty(Core.LD_DESCRIPTION_PROPERTY, "Sandbox Node with PDF-File");
            jcrService.addFileNode(session, sandboxDocument, in);
            jcrService.addComment(session, sandboxDocument, "Hello World!");

            session.save();
        } finally {
            if (session != null) {
                session.logout();
            }
            IOUtils.closeQuietly(in);
        }
    }
}
