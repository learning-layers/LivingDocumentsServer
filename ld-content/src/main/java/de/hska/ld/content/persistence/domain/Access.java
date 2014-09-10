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
import java.util.List;

@Entity
@Table(name = "ld_access")
public class Access {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ElementCollection(targetClass = Permission.class)
    @CollectionTable(name = "permission", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "permission", nullable = false)
    @Enumerated(EnumType.STRING)
    private List<Permission> permissionList;

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

    public List<Permission> getPermissionList() {
        if (permissionList == null) {
            permissionList = new ArrayList<>();
        }
        return permissionList;
    }

    public void setPermissionList(List<Permission> permissionList) {
        this.permissionList = permissionList;
    }

    public enum Permission {
        READ, WRITE
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Access access = (Access) o;

        return !(user != null ? !user.equals(access.user) : access.user != null);
    }

    @Override
    public int hashCode() {
        return user != null ? user.hashCode() : 0;
    }
}
