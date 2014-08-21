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

import de.hska.ld.core.persistence.domain.User;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "subscription")
public class Subscription {

    public Subscription() {
    }

    public Subscription(User user, Type... types) {
        this.typeList = new ArrayList<>(Arrays.asList(types));
        this.user = user;
        this.createdAt = new Date();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ElementCollection(targetClass = Type.class)
    @CollectionTable(name = "type", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private List<Type> typeList;

    @Column(name = "created_at")
    private Date createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Type> getTypeList() {
        return typeList;
    }

    public void setTypeList(List<Type> typeList) {
        this.typeList = typeList;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public enum Type {
        MAIN_CONTENT, ATTACHMENT, COMMENT, DISCUSSION
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Subscription that = (Subscription) o;

        if (user != null ? !user.equals(that.user) : that.user != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return user != null ? user.hashCode() : 0;
    }
}