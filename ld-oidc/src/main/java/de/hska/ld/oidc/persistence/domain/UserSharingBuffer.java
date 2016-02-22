/*
 *  Code contributed to the Learning Layers project
 *  http://www.learning-layers.eu
 *  Development is partly funded by the FP7 Programme of the European
 *  Commission under Grant Agreement FP7-ICT-318209.
 *  Copyright (c) 2016, Karlsruhe University of Applied Sciences.
 *  For a list of contributors see the AUTHORS file at the top-level directory
 *  of this distribution.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package de.hska.ld.oidc.persistence.domain;

import javax.persistence.*;

@Entity
@Table(name = "ld_user_sharing_buffer")
public class UserSharingBuffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    private Long documentId;
    private String email;
    private String sub;
    private String issuer;
    private String permissionString;

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public String getEmail() {
        return email;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getSub() {
        return sub;
    }

    public String getIssuer() {
        return issuer;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPermissionString() {
        return permissionString;
    }

    public void setPermissionString(String permissionString) {
        this.permissionString = permissionString;
    }
}
