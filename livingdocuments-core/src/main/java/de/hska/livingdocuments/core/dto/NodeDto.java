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
package de.hska.livingdocuments.core.dto;

import de.hska.livingdocuments.core.util.Core;
import org.apache.jackrabbit.JcrConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.validation.constraints.NotNull;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class NodeDto {
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeDto.class);

    @NotNull
    private String nodeId;
    private String description;
    private Map<String, String> tags;
    private Calendar createdDate;
    private Calendar lastModifiedAt;
    private String lastModifiedBy;
    private String createdBy;

    public NodeDto() {
    }

    public NodeDto(Node node) {
        try {
            this.nodeId = node.getName();
            if (node.hasProperty(Core.LD_DESCRIPTION_PROPERTY)) {
                this.description = node.getProperty(Core.LD_DESCRIPTION_PROPERTY).getString();
            }

            loadTags(node);

            // fetch file node meta data
            if (node.hasNode(Core.LD_FILE_NODE)) {
                Node fileNode = node.getNode(Core.LD_FILE_NODE);
                this.createdDate = fileNode.getProperty(JcrConstants.JCR_CREATED).getDate();
                this.createdBy = fileNode.getProperty("jcr:createdBy").getString();

                if (fileNode.hasNode(JcrConstants.JCR_CONTENT)) {
                    // fetch resource node meta data
                    Node resourceNode = fileNode.getNode(JcrConstants.JCR_CONTENT);
                    lastModifiedAt = resourceNode.getProperty(JcrConstants.JCR_LASTMODIFIED).getDate();
                    lastModifiedBy = resourceNode.getProperty(Core.JCR_LASTMODIFIED_BY).getString();
                }
            }
        } catch (RepositoryException e) {
            LOGGER.error("Creating node meta dto failed", e);
        }
    }

    private void loadTags(Node node) throws RepositoryException {
        if (node.hasNode(Core.LD_TAGS_NODE)) {
            tags = new HashMap<>();
            Node tagsNode = node.getNode(Core.LD_TAGS_NODE);
            NodeIterator tagsIt = tagsNode.getNodes();
            while (tagsIt.hasNext()) {
                Node tag = tagsIt.nextNode();
                tags.put(tag.getIdentifier(), tag.getProperty(Core.LD_NAME_PROPERTY).getString());
            }
        }
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
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

    public Calendar getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Calendar createdDate) {
        this.createdDate = createdDate;
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
