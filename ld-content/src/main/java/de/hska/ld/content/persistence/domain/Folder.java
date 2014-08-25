package de.hska.ld.content.persistence.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @ManyToMany(cascade = {CascadeType.ALL}, mappedBy = "folderList")
    @JsonIgnore
    private List<Folder> parentFolderList;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "ld_folder_folder",
            joinColumns = {@JoinColumn(name = "folder_id")},
            inverseJoinColumns = {@JoinColumn(name = "folder_id_inverse")})
    private List<Folder> folderList;

    @OneToMany
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

    public List<Folder> getParentFolderList() {
        if (this.parentFolderList == null) {
            this.parentFolderList = new ArrayList<>();
        }
        return parentFolderList;
    }

    public void setParentFolderList(List<Folder> parentFolderList) {
        this.parentFolderList = parentFolderList;
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
}
