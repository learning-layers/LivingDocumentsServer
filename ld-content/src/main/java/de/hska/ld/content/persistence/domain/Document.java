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
import org.hibernate.search.annotations.*;
import org.hibernate.search.annotations.Index;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Indexed
@Table(name = "ld_document")
public class Document extends Content {

    @NotBlank
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    @Column(name = "title", nullable = false, columnDefinition = "TEXT")
    private String title;

    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "ld_document_attachment",
            joinColumns = {@JoinColumn(name = "document_id")},
            inverseJoinColumns = {@JoinColumn(name = "attachment_id")})
    private List<Attachment> attachmentList;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "ld_discussion",
            joinColumns = {@JoinColumn(name = "discussion_id")},
            inverseJoinColumns = {@JoinColumn(name = "discussion_id_inverse")})
    private List<Document> discussionList;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "ld_document_subscription",
            joinColumns = {@JoinColumn(name = "document_id")},
            inverseJoinColumns = {@JoinColumn(name = "subscription_id")})
    private List<Subscription> subscriptionList;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "ld_document_hyperlink",
            joinColumns = {@JoinColumn(name = "document_id")},
            inverseJoinColumns = {@JoinColumn(name = "hyperlink_id")})
    private List<Hyperlink> hyperlinkList;

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "ld_document_expert",
            joinColumns = {@JoinColumn(name = "document_id")},
            inverseJoinColumns = {@JoinColumn(name = "user_id")})
    private List<User> expertList;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    @JsonIgnore
    private Document parent;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("attachments")
    public List<Attachment> getAttachmentList() {
        if (attachmentList == null) {
            attachmentList = new ArrayList<>();
        }
        return attachmentList;
    }

    public void setAttachmentList(List<Attachment> attachmentList) {
        this.attachmentList = attachmentList;
    }

    @JsonProperty("discussions")
    public List<Document> getDiscussionList() {
        return discussionList;
    }

    public void setDiscussionList(List<Document> discussionList) {
        if (discussionList == null) {
            discussionList = new ArrayList<>();
        }
        this.discussionList = discussionList;
    }

    @JsonProperty("subscriptions")
    public List<Subscription> getSubscriptionList() {
        if (subscriptionList == null) {
            subscriptionList = new ArrayList<>();
        }
        return subscriptionList;
    }

    @JsonProperty("hyperlinks")
    public List<Hyperlink> getHyperlinkList() {
        if (hyperlinkList == null) {
            hyperlinkList = new ArrayList<>();
        }
        return hyperlinkList;
    }

    public List<User> getExpertList() {
        if (expertList == null) {
            expertList = new ArrayList<>();
        }
        return expertList;
    }

    public void setExpertList(List<User> expertList) {
        this.expertList = expertList;
    }

    public void setHyperlinkList(List<Hyperlink> hyperlinkList) {
        this.hyperlinkList = hyperlinkList;
    }

    public void setSubscriptionList(List<Subscription> subscriptionList) {
        this.subscriptionList = subscriptionList;
    }

    public Document getParent() {
        return parent;
    }

    public void setParent(Document parent) {
        this.parent = parent;
    }

}
