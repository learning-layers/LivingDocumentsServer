package de.hska.ld.oidc.dto;

public class SSSTagRequestDto {
    private String entity;
    private String label;
    private String space = "sharedSpace";

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getSpace() {
        return space;
    }
}
