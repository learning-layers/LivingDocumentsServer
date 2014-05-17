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

package de.hska.livingdocuments.core.config;

import de.hska.livingdocuments.core.util.Core;
import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.commons.cnd.ParseException;
import org.apache.jackrabbit.core.TransientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;

import javax.jcr.*;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.NodeTypeTemplate;
import java.io.File;
import java.io.IOException;

@Configuration
public class JcrConfig {

    @Autowired
    private Environment env;

    @Autowired
    private ResourceLoader resourceLoader;

    @Bean
    public Repository repository() throws IOException {
        String homePathCfg = env.getProperty("module.core.repository.home");
        String homePath = resourceLoader.getResource("file:" + homePathCfg).getURL().getPath();
        //String config = resourceLoader.getResource("classpath:/repository.xml").getURI().getPath();
        System.setProperty("derby.stream.error.file", homePath + File.separator + "derby.log");
        return new TransientRepository(resourceLoader.getResource("file:" + homePath).getFile());
    }

    @Autowired
    private void registerNodeTypes() throws IOException, RepositoryException, ParseException {
        Repository repository = repository();
        Session session = repository.login(Core.ADMIN_CREDENTIALS);

        NamespaceRegistry namespaceRegistry = session.getWorkspace().getNamespaceRegistry();
        try {
            namespaceRegistry.registerNamespace("ld", "http://learning-layers.eu/nt/ld");

            // Retrieve node type manager from the session
            NodeTypeManager nodeTypeManager = session.getWorkspace().getNodeTypeManager();

            // Create node type
            NodeTypeTemplate ldDocumentNodeType = nodeTypeManager.createNodeTypeTemplate();
            ldDocumentNodeType.setName(Core.LD_DOCUMENT);

            String[] superTypeNames = {JcrConstants.NT_UNSTRUCTURED};
            ldDocumentNodeType.setDeclaredSuperTypeNames(superTypeNames);

            nodeTypeManager.registerNodeType(ldDocumentNodeType, false);
        } catch (NamespaceException e) {
            // namespace ld -> http://learning-layers.eu/nt/ld: mapping already exists
        }
    }
}
