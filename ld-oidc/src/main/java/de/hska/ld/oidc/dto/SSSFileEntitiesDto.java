package de.hska.ld.oidc.dto;

import java.util.List;

public class SSSFileEntitiesDto {
    String op;
    List<SSSFileEntityDto> entities;

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public List<SSSFileEntityDto> getEntities() {
        return entities;
    }

    public void setEntities(List<SSSFileEntityDto> entities) {
        this.entities = entities;
    }
}
