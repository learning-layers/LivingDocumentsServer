package de.hska.livingdocuments.core.dto;

import javax.jcr.Node;
import java.util.Calendar;

public class NodeMetaDto {

    private Calendar createdDate;
    private Calendar lastModifiedAt;
    private String lastModifiedBy;

    public NodeMetaDto() {
    }

    public void setCreatedAt(Calendar createdDate) {
        this.createdDate = createdDate;
    }

    public void setLastModifiedAt(Calendar lastModifiedAt) {
        this.lastModifiedAt = lastModifiedAt;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }
}
