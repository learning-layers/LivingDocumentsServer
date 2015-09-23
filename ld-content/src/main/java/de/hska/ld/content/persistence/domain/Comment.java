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
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "ld_comment")
public class Comment extends Content {

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Content parent;

    @NotBlank
    @Column(name = "text", nullable = false, columnDefinition = "TEXT")
    private String text;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    @JoinTable(name = "ld_comment_like_user",
            joinColumns = {@JoinColumn(name = "comment_id")},
            inverseJoinColumns = {@JoinColumn(name = "user_id")})
    private List<User> likeList;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @JsonIgnore
    public Content getParent() {
        return parent;
    }

    @JsonIgnore
    public void setParent(Content parent) {
        this.parent = parent;
    }

    public List<User> getLikeList() {
        if (likeList == null) {
            likeList = new ArrayList<>();
        }
        return likeList;
    }

    public void setLikeList(List<User> likeList) {
        this.likeList = likeList;
    }

    @JsonProperty("subcommentlength")
    public int getSubcommentlength() {
        int size = this.getCommentList().stream().filter(c -> !c.isDeleted()).collect(Collectors.toList()).size();
        return size;
    }

    @JsonProperty("likeslength")
    public int getLikeslength() {
        return this.getLikeList().size();
    }

    public void setSubcommentlength(int size) {
    }

    public void setLikeslength(int size) {
    }
}
