package de.hska.ld.oidc.dto;


public class SSSQAEntryDto extends SSSEntityDto {
    String description;
    String content;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
