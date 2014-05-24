package de.hska.livingdocuments.core.dto;

import de.hska.livingdocuments.core.util.Core;
import org.springframework.web.multipart.MultipartFile;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.validation.constraints.NotNull;
import java.io.InputStream;

public class NodeDto {

    @NotNull
    private String nodeId;
    private String description;

    public NodeDto() {
    }

    public NodeDto(Node node) {
        try {
            this.nodeId = node.getName();
            this.description = node.getProperty(Core.LD_DESCRIPTION_PROPERTY).getString();
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
}
