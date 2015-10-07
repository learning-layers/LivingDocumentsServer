package de.hska.ld.oidc.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SSSCreateDiscRequestDto {
    private boolean addNewDisc = true;
    private String description;
    private List<String> entities;
    private String label;
    private List<String> targets;
    private String type = "qa";
    private List<String> tags;

    public boolean isAddNewDisc() {
        return addNewDisc;
    }

    public void setAddNewDisc(boolean addNewDisc) {
        this.addNewDisc = addNewDisc;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<String> getTargets() {
        if (targets == null) {
            targets = new ArrayList<String>();
        }
        return targets;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getEntities() {
        if (entities == null) {
            entities = new ArrayList<String>();
        }
        return entities;
    }

    public void setEntities(List<String> entities) {
        this.entities = entities;
    }

    public void setTargets(List<String> targets) {
        this.targets = targets;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
