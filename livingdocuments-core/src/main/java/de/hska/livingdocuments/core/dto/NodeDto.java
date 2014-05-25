package de.hska.livingdocuments.core.dto;

import de.hska.livingdocuments.core.util.Core;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

public class NodeDto {

    @NotNull
    private String nodeId;
    private String description;
    private Map<String, String> tags;

    public NodeDto() {
    }

    public NodeDto(Node node) {
        try {
            this.nodeId = node.getName();
            this.description = node.getProperty(Core.LD_DESCRIPTION_PROPERTY).getString();
            if (node.hasNode(Core.LD_TAGS_NODE)) {
                tags = new HashMap<>();
                Node tagsNode = node.getNode(Core.LD_TAGS_NODE);
                NodeIterator tagsIt = tagsNode.getNodes();
                while (tagsIt.hasNext()) {
                    Node tag = tagsIt.nextNode();
                    tags.put(tag.getIdentifier(), tag.getProperty(Core.LD_NAME_PROPERTY).getString());
                }
            }
        } catch (RepositoryException e) {
            //
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
}
