package de.hska.ld.oidc.dto;

import org.codehaus.jackson.annotate.JsonProperty;

public class OIDCUserinfoDto {
    // {"sub":"","name":"","preferred_username":"","given_name":"","family_name":"","updated_time":"20150821_103013","email":"","email_verified":true}
    String sub;
    String name;
    @JsonProperty("preferred_username")
    String preferredUsername;
    @JsonProperty("given_name")
    String givenName;
    @JsonProperty("family_name")
    String familyName;
    @JsonProperty("updated_time")
    String updatedTime;
    String email;
    String picture;
    @JsonProperty("email_verified")
    boolean emailVerified;

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPreferredUsername() {
        return preferredUsername;
    }

    public void setPreferredUsername(String preferredUsername) {
        this.preferredUsername = preferredUsername;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(String updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }
}

