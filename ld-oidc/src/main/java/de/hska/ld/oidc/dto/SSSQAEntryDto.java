package de.hska.ld.oidc.dto;


import java.util.List;

public class SSSQAEntryDto extends SSSEntityDto {
    String description;
    String content;
    List<SSSEntityDto> attachedEntities;
    List<SSSTagDto> tags;

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

    public List<SSSEntityDto> getAttachedEntities() {
        return attachedEntities;
    }

    public void setAttachedEntities(List<SSSEntityDto> attachedEntities) {
        this.attachedEntities = attachedEntities;
    }

    public List<SSSTagDto> getTags() {
        return tags;
    }

    public void setTags(List<SSSTagDto> tags) {
        this.tags = tags;
    }
}
