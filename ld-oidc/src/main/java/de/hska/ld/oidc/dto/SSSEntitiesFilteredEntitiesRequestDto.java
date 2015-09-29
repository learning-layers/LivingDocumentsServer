package de.hska.ld.oidc.dto;

public class SSSEntitiesFilteredEntitiesRequestDto {
    boolean setDiscs;
    boolean setThumb;

    public boolean isSetDiscs() {
        return setDiscs;
    }

    public void setSetDiscs(boolean setDiscs) {
        this.setDiscs = setDiscs;
    }

    public boolean isSetThumb() {
        return setThumb;
    }

    public void setSetThumb(boolean setThumb) {
        this.setThumb = setThumb;
    }
}
