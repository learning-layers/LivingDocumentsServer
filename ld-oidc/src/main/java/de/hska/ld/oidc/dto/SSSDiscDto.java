package de.hska.ld.oidc.dto;

import java.util.List;

public class SSSDiscDto extends SSSEntityDto {
    SSSAuthorDto author;
    String description;
    List<SSSQAEntryDto> entries;

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

    public List<SSSQAEntryDto> getEntries() {
        return entries;
    }

    public void setEntries(List<SSSQAEntryDto> entries) {
        this.entries = entries;
    }
}
