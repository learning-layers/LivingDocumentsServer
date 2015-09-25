package de.hska.ld.oidc.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SSSAuthorDto extends SSSEntityDto {
    String description;
    String email;

    @JsonIgnore
    public SSSAuthorDto getAuthor() {
        return author;
    }

    @JsonIgnore
    public void setAuthor(SSSAuthorDto author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
