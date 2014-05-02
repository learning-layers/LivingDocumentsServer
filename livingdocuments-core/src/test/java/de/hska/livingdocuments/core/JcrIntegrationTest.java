package de.hska.livingdocuments.core;

import org.apache.jackrabbit.JcrConstants;
import org.apache.tika.io.IOUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springmodules.jcr.JcrCallback;
import org.springmodules.jcr.JcrTemplate;

import javax.jcr.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class JcrIntegrationTest extends AbstractIntegrationTest {

    final Logger LOGGER = LoggerFactory.getLogger(JcrIntegrationTest.class);

    @Autowired
    private JcrTemplate template = null;

    private String nodeName = "fileFolder";

    /**
     * Adds node if it doesn't exist.
     */
    @Test
    public void testAddNodeIfDoesNotExist() {
        assertNotNull("JCR Template is null.", template);
        template.execute(session -> {
            Node root = session.getRootNode();
            LOGGER.debug("Starting from root node.  node={}", root);
            Node node;
            if (root.hasNode(nodeName)) {
                node = root.getNode(nodeName);
                LOGGER.debug("Node exists.  node={}", node);
            } else {
                node = root.addNode(nodeName);
                session.save();
                LOGGER.info("Saved node.  node={}", node);
            }
            assertNotNull("Node is null.", node);
            return node;
        });
    }

    /**
     * Adds file to repository.
     */
    @Test
    public void testAddFileIfDoesNotExist() {
        @SuppressWarnings("unused")
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
        });
    }
}
