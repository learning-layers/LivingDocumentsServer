package de.hska.ld.oidc.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SSSEntryForDiscussionRequestDto {
    private boolean addNewDisc = false;
    private String disc;
    private String entry;
    private List<String> entities;
    private String type = "qaEntry";
    private List<String> tags;

    public boolean isAddNewDisc() {
        return addNewDisc;
    }

    public void setAddNewDisc(boolean addNewDisc) {
        this.addNewDisc = addNewDisc;
    }

    public String getDisc() {
        return disc;
    }

    public void setDisc(String disc) {
        this.disc = disc;
    }

    public String getEntry() {
        return entry;
    }

    public void setEntry(String entry) {
        this.entry = entry;
    }

    public List<String> getEntities() {
        return entities;
    }

    public void setEntities(List<String> entities) {
        this.entities = entities;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
