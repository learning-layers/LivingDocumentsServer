package de.hska.ld.content.persistence.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ld_folder")
public class Folder extends Content {

    public Folder() {
    }

    public Folder(String name) {
        this.name = name;
    }

    private String name;

    private boolean sharingFolder;

    @ManyToOne
    private Folder parent;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "ld_folder_folder",
            joinColumns = {@JoinColumn(name = "folder_id")},
            inverseJoinColumns = {@JoinColumn(name = "parent_folder_id")})
    private List<Folder> folderList;

    @ManyToMany
    @JoinTable(name = "ld_folder_document",
            joinColumns = {@JoinColumn(name = "folder_id")},
            inverseJoinColumns = {@JoinColumn(name = "document_id")})
    private List<Document> documentList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @JsonProperty("folders")
    public List<Folder> getFolderList() {
        if (folderList == null) {
            folderList = new ArrayList<>();
        }
        return folderList;
    }

    public void setFolderList(List<Folder> folderList) {
        this.folderList = folderList;
    }

    @JsonProperty("documents")
    public List<Document> getDocumentList() {
        if (documentList == null) {
            documentList = new ArrayList<>();
        }
        return documentList;
    }

    public void setDocumentList(List<Document> documentList) {
        this.documentList = documentList;
    }

    public boolean isSharingFolder() {
        return sharingFolder;
    }

    public void setSharingFolder(boolean sharingFolder) {
        this.sharingFolder = sharingFolder;
    }

    public Folder getParent() {
        return parent;
    }

    public void setParent(Folder parent) {
        this.parent = parent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Folder folder = (Folder) o;

        if (sharingFolder != folder.sharingFolder) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (sharingFolder ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Folder{" + "id=" + this.getId() +
                ", sharingFolder=" + sharingFolder +
                ", name='" + name + '\'' +
                '}';
    }
}
