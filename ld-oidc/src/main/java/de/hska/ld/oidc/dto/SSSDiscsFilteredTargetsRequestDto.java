package de.hska.ld.oidc.dto;

public class SSSDiscsFilteredTargetsRequestDto {
    /*{
        "setComments": true,
            "setLikes": true,
            "setCircleTypes": true,
            "setEntries": true,
            "setTags": true,
            "setAttachedEntities": true
    }*/
    boolean setComments;
    boolean setLikes;
    boolean setCircleTypes;
    boolean setEntries;
    boolean setTags;
    boolean setAttachedEntities;

    public boolean isSetComments() {
        return setComments;
    }

    public void setSetComments(boolean setComments) {
        this.setComments = setComments;
    }

    public boolean isSetLikes() {
        return setLikes;
    }

    public void setSetLikes(boolean setLikes) {
        this.setLikes = setLikes;
    }

    public boolean isSetCircleTypes() {
        return setCircleTypes;
    }

    public void setSetCircleTypes(boolean setCircleTypes) {
        this.setCircleTypes = setCircleTypes;
    }

    public boolean isSetEntries() {
        return setEntries;
    }

    public void setSetEntries(boolean setEntries) {
        this.setEntries = setEntries;
    }

    public boolean isSetTags() {
        return setTags;
    }

    public void setSetTags(boolean setTags) {
        this.setTags = setTags;
    }

    public boolean isSetAttachedEntities() {
        return setAttachedEntities;
    }

    public void setSetAttachedEntities(boolean setAttachedEntities) {
        this.setAttachedEntities = setAttachedEntities;
    }
}
