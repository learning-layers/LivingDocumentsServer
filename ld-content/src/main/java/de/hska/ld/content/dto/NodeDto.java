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

package de.hska.ld.content.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.hska.ld.content.util.Content;
import org.apache.jackrabbit.JcrConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class NodeDto {
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeDto.class);

    private String nodeId;
    private String title;
    private String description;
    private Map<String, String> tags;
    private Calendar createdAt;
    private Calendar lastModifiedAt;
    private String lastModifiedBy;
    private String createdBy;

    public NodeDto() {
    }

    public NodeDto(Node node) {
        try {
            this.nodeId = node.getName();
            loadProperties(node);
            loadTags(node);
            loadFileMetaData(node);
        } catch (RepositoryException e) {
            LOGGER.error("Creating node meta dto failed", e);
        }
    }

    private void loadTags(Node node) throws RepositoryException {
        if (node.hasNode(Content.LD_TAGS_NODE)) {
            tags = new HashMap<>();
            Node tagsNode = node.getNode(Content.LD_TAGS_NODE);
            NodeIterator tagsIt = tagsNode.getNodes();
            while (tagsIt.hasNext()) {
                Node tag = tagsIt.nextNode();
                tags.put(tag.getIdentifier(), tag.getProperty(Content.LD_NAME_PROPERTY).getString());
            }
        }
    }

    private void loadFileMetaData(Node node) throws RepositoryException {
        if (node.hasNode(Content.LD_MAIN_FILE_NODE)) {
            Node fileNode = node.getNode(Content.LD_MAIN_FILE_NODE);
            this.createdAt = fileNode.getProperty(JcrConstants.JCR_CREATED).getDate();
            this.createdBy = fileNode.getProperty("jcr:createdBy").getString();

            if (fileNode.hasNode(JcrConstants.JCR_CONTENT)) {
                // fetch resource node meta data
                Node resourceNode = fileNode.getNode(JcrConstants.JCR_CONTENT);
                lastModifiedAt = resourceNode.getProperty(JcrConstants.JCR_LASTMODIFIED).getDate();
                lastModifiedBy = resourceNode.getProperty(Content.JCR_LASTMODIFIED_BY).getString();
            }
        }
    }

    private void loadProperties(Node node) throws RepositoryException {
        if (node.hasProperty(Content.LD_TITLE_PROPERTY)) {
            this.title = node.getProperty(Content.LD_TITLE_PROPERTY).getString();
        }
        if (node.hasProperty(Content.LD_DESCRIPTION_PROPERTY)) {
            this.description = node.getProperty(Content.LD_DESCRIPTION_PROPERTY).getString();
        }
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public Calendar getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Calendar createdAt) {
        this.createdAt = createdAt;
    }

    public Calendar getLastModifiedAt() {
        return lastModifiedAt;
    }

    public void setLastModifiedAt(Calendar lastModifiedAt) {
        this.lastModifiedAt = lastModifiedAt;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
