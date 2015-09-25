package de.hska.ld.oidc.dto;

public class SSSDiscDto {
    String id;
    String label;
    String creationTime;
    String type;
    SSSAuthorDto author;
    String description;
    //List<QAEntry> entries;

        /*public class QAEntry {
            String id;
            String label;
            String creationTime;
            String type;
            Author author;
            String description;
            String content;
        }*/

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
