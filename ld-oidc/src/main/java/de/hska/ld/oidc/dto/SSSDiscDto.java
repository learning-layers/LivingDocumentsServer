package de.hska.ld.oidc.dto;

import java.util.List;

public class SSSDiscDto extends SSSEntityDto {
    String description;
    List<SSSQAEntryDto> entries;
    List<String> circleTypes;
    List<SSSEntityDto> attachedEntities;
    List<SSSTagDto> tags;
    SSSLikesDto likes;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<SSSQAEntryDto> getEntries() {
        return entries;
    }

    public void setEntries(List<SSSQAEntryDto> entries) {
        this.entries = entries;
    }

    public List<String> getCircleTypes() {
        return circleTypes;
    }

    public void setCircleTypes(List<String> circleTypes) {
        this.circleTypes = circleTypes;
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

    public SSSLikesDto getLikes() {
        return likes;
    }

    public void setLikes(SSSLikesDto likes) {
        this.likes = likes;
    }
}
