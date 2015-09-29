package de.hska.ld.oidc.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SSSEntityDto {
    String id;
    String label;
    String creationTime;
    String type;
    SSSAuthorDto author;
    SSSFileDto file;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public SSSAuthorDto getAuthor() {
        return author;
    }

    public void setAuthor(SSSAuthorDto author) {
        this.author = author;
    }

    public SSSFileDto getFile() {
        return file;
    }

    public void setFile(SSSFileDto file) {
        this.file = file;
    }
}
