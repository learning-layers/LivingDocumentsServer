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

package de.hska.ld.oidc.service.impl;

import de.hska.ld.oidc.persistence.domain.UserSharingBuffer;
import de.hska.ld.oidc.persistence.repository.UserSharingBufferRepository;
import de.hska.ld.oidc.service.UserSharingBufferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserSharingBufferServiceImpl implements UserSharingBufferService {

    @Autowired
    private UserSharingBufferRepository repository;

    public void addUserSharingBuffer(Long documentId, String email, String permissionString) {
        UserSharingBuffer userSharingBuffer = new UserSharingBuffer();
        userSharingBuffer.setDocumentId(documentId);
        userSharingBuffer.setEmail(email);
        userSharingBuffer.setPermissionString(permissionString);
        repository.save(userSharingBuffer);
    }

    public void addUserSharingBuffer(Long documentId, String sub, String issuer, String permissionString) {
        UserSharingBuffer userSharingBuffer = new UserSharingBuffer();
        userSharingBuffer.setDocumentId(documentId);
        userSharingBuffer.setSub(sub);
        userSharingBuffer.setIssuer(issuer);
        userSharingBuffer.setPermissionString(permissionString);
        repository.save(userSharingBuffer);
    }

    @Override
    public UserSharingBuffer findBySubAndIssuer(String subId, String issuer) {
        return repository.findByIssuerAndSub(issuer, subId);
    }

    @Override
    public UserSharingBuffer findByEmail(String email) {
        return repository.findByEmail(email);
    }

    public UserSharingBufferRepository getRepository() {
        return repository;
    }
}
