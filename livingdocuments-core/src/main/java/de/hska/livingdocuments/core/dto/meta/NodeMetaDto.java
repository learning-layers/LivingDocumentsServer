package de.hska.livingdocuments.core.dto.meta;

import de.hska.livingdocuments.core.dto.NodeDto;
import de.hska.livingdocuments.core.util.Core;
import org.apache.jackrabbit.JcrConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Calendar;

public class NodeMetaDto extends NodeDto {
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeMetaDto.class);

    private Calendar createdDate;
    private Calendar lastModifiedAt;
    private String lastModifiedBy;
    private String createdBy;

    public NodeMetaDto(Node node) {
        super(node);
        try {
            // fetch file node meta data
            Node fileNode = node.getNode(Core.LD_FILE_NODE);
            this.createdDate = fileNode.getProperty(JcrConstants.JCR_CREATED).getDate();
            this.createdBy = fileNode.getProperty("jcr:createdBy").getString();

            // fetch resource node meta data
            Node resourceNode = fileNode.getNode(JcrConstants.JCR_CONTENT);
            lastModifiedAt = resourceNode.getProperty(JcrConstants.JCR_LASTMODIFIED).getDate();
            lastModifiedBy = resourceNode.getProperty(Core.JCR_LASTMODIFIED_BY).getString();
        } catch (RepositoryException e) {
            LOGGER.error("Creating node meta dto failed", e);
        }
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
