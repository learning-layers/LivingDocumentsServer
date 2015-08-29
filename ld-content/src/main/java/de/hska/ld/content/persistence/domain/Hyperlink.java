package de.hska.ld.content.persistence.domain;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "ld_hyperlink")
public class Hyperlink extends Content {

    @URL
    @NotBlank
    @Column(name = "url", nullable = false, columnDefinition = "TEXT")
    private String url;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof Hyperlink)) {
            return false;
        }
        Hyperlink hyperlink = (Hyperlink) o;
        return url.equals(hyperlink.url) && !(description != null ? !description.equals(hyperlink.description) : hyperlink.description != null);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + url.hashCode();
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }
}
