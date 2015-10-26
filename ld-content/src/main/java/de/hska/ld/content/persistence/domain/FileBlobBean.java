package de.hska.ld.content.persistence.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.search.annotations.DocumentId;

import javax.persistence.*;
import java.sql.Blob;

@Entity
@Table(name = "ld_fileblob")
public class FileBlobBean {

    @Id
    @DocumentId
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(name = "id")
    private Long id;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "sourceBlob")
    @JsonIgnore
    private Blob sourceBlob;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Blob getSourceBlob() {
        return sourceBlob;
    }

    public void setSourceBlob(Blob sourceBlob) {
        this.sourceBlob = sourceBlob;
    }
}
