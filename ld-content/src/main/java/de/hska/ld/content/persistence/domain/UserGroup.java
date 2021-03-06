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
import java.util.List;

@Entity
@Table(name = "ld_usergroup")
public class UserGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(name = "id")
    private Long id;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "ld_usergroup_user",
            joinColumns = {@JoinColumn(name = "usergroup_id")},
            inverseJoinColumns = {@JoinColumn(name = "user_id")})
    List<User> userList;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "ld_usergroup_subusergroup",
            joinColumns = {@JoinColumn(name = "usergroup_id")},
            inverseJoinColumns = {@JoinColumn(name = "subusergroup_id")})
    List<UserGroup> userGroupList;

    public Long getId() {
        return id;
    }

    public List<User> getUserList() {
        return userList;
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
    }

    public List<UserGroup> getUserGroupList() {
        return userGroupList;
    }

    public void setUserGroupList(List<UserGroup> userGroupList) {
        this.userGroupList = userGroupList;
    }
}
