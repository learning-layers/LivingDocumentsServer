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

package de.hska.ld.ldToSSS.persistence.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.content.persistence.domain.Tag;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ld_sss_recomm")
public class RecommInfo {

//    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotBlank
    @Column(name = "realm", nullable = false)
    private String realm;

    @NotBlank
    @Column(name = "forUser", nullable = false)
    private String forUser;

    @NotBlank
    @Column(name = "entity", nullable = false)
    private String entity;

    private ArrayList<String> tags;

    public RecommInfo(){
    }

    public RecommInfo(Long userId, String realm, String forUser){
        this.userId = userId;
        this.realm = realm;
        this.forUser = forUser;
    }

    @JsonIgnore
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "ld_document_expert",
            joinColumns = {@JoinColumn(name = "user_id")},
            inverseJoinColumns = {@JoinColumn(name = "document_id")})
    List<Document> documentList;
//
//    @ManyToMany(cascade = CascadeType.ALL)
//    @JoinTable(name = "ld_category_sss_recomm",
//            joinColumns = {@JoinColumn(name = "sss_recomm_id", referencedColumnName="id")},
//            inverseJoinColumns = {@JoinColumn(name = "category_id", referencedColumnName="id")})
//    List<Category> categoryList;

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public String getForUser() {
        return forUser;
    }

    public void setForUser(String forUser) {
        this.forUser = forUser;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<Document> getDocumentList() {
        return documentList;
    }

    public void setDocumentList(List<Document> documentList) {
        this.documentList = documentList;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }

    public void updateTagList(){
        ArrayList<String> tagList = new ArrayList<>();
        for(Document document : documentList){
            for(Tag tag : document.getTagList()){
                String tagName = tag.getName();

                if(!tagName.isEmpty() && !tagList.contains(tagName)) {
                    tagList.add(tagName);
                }
            }
        }
        setTags(tagList);
    }
}
