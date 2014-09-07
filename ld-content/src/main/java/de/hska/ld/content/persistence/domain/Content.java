/**
 * Code contributed to the Learning Layers project
 * http://www.learning-layers.eu
 * Development is partly funded by the FP7 Programme of the European
 * Commission under Grant Agreement FP7-ICT-318209.
 * Copyright (c) 2014, Karlsruhe University of Applied Sciences.
 * For a list of contributors see the AUTHORS file at the top-level directory
 * of this distribution.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.hska.ld.content.persistence.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.util.Core;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(name = "id")
    private Long id;

    @Version
    public int version;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modified_at")
    private Date modifiedAt;

    @ManyToOne
    @JoinColumn(name = "creator_id")
    private User creator;

    @Column(name = "access_all")
    private boolean accessAll;

    @Column(name = "deleted", nullable = false)
    private boolean deleted;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "ld_content_tag",
            joinColumns = {@JoinColumn(name = "content_id")},
            inverseJoinColumns = {@JoinColumn(name = "tag_id")})
    private List<Tag> tagList;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "ld_content_access",
            joinColumns = {@JoinColumn(name = "content_id")},
            inverseJoinColumns = {@JoinColumn(name = "access_id")})
    private List<Access> accessList;

    @OneToMany(cascade = CascadeType.PERSIST, mappedBy = "parent")
    private List<Comment> commentList;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Date modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    @JsonIgnore
    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @JsonProperty("tags")
    public List<Tag> getTagList() {
        if (tagList == null) {
            tagList = new ArrayList<>();
        }
        return tagList;
    }

    public void setTagList(List<Tag> tagList) {
        this.tagList = tagList;
    }

    @JsonIgnore
    public List<Access> getAccessList() {
        if (accessList == null) {
            accessList = new ArrayList<>();
        }
        return accessList;
    }

    public void setAccessList(List<Access> accessList) {
        this.accessList = accessList;
    }

    @JsonProperty("comments")
    public List<Comment> getCommentList() {
        if (commentList == null) {
            commentList = new ArrayList<>();
        }
        return commentList;
    }

    public void setCommentList(List<Comment> commentList) {
        this.commentList = commentList;
    }

    public boolean isAccessAll() {
        return accessAll;
    }

    public void setAccessAll(boolean accessAll) {
        this.accessAll = accessAll;
    }

    @PrePersist
    void prePersist() {
        this.createdAt = new Date();
        if (this.creator == null) {
            this.creator = Core.currentUser();
        }
    }

    @PreUpdate
    void modifiedAt() {
        this.modifiedAt = new Date();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Content content = (Content) o;

        if (id != null ? !id.equals(content.id) : content.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Content{" +
                "id=" + id +
                ", version=" + version +
                ", createdAt=" + createdAt +
                ", modifiedAt=" + modifiedAt +
                ", creator=" + creator +
                ", accessAll=" + accessAll +
                ", deleted=" + deleted +
                '}';
    }
}
