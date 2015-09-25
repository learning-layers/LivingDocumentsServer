package de.hska.ld.oidc.dto;

public class SSSAuthorDto extends SSSEntityDto {
    String description;
    String email;

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
