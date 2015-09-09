package de.hska.ld.oidc.dto;

public class SSSLivingdocsRequestDto {
    /*{
        "uri": "SSUri",
            "label": "SSLabel",
            "description": "SSTextComment",
            "discussion": "SSUri"
    }*/
    String uri;
    String label;
    String description;
    String discussion;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDiscussion() {
        return discussion;
    }

    public void setDiscussion(String discussion) {
        this.discussion = discussion;
    }

    @Override
    public String toString() {
        return "SSSLivingdocsDto{" +
                "uri='" + uri + '\'' +
                ", label='" + label + '\'' +
                ", description='" + description + '\'' +
                ", discussion='" + discussion + '\'' +
                '}';
    }
}
