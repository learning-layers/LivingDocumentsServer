package de.hska.ld.content.dto;

public class EtherpadDocumentUpdateDto {
    private String authorId = null;
    private String padId = null;
    private String apiKey = null;

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getPadId() {
        return padId;
    }

    public void setPadId(String padId) {
        this.padId = padId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public String toString() {
        return "EtherpadDocumentUpdateDto{" +
                "authorId='" + authorId + '\'' +
                ", padId='" + padId + '\'' +
                ", apiKey='" + apiKey + '\'' +
                '}';
    }
}
