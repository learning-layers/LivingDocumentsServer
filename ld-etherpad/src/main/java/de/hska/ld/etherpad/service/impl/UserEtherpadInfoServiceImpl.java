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

package de.hska.ld.etherpad.service.impl;

import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.UserService;
import de.hska.ld.core.util.Core;
import de.hska.ld.etherpad.persistence.domain.UserEtherpadInfo;
import de.hska.ld.etherpad.persistence.repository.UserEtherpadInfoRepository;
import de.hska.ld.etherpad.service.UserEtherpadInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserEtherpadInfoServiceImpl implements UserEtherpadInfoService {

    @Autowired
    private UserEtherpadInfoRepository userEtherpadInfoRepository;

    @Autowired
    private UserService userService;

    @Override
    @Transactional(readOnly = false)
    public UserEtherpadInfo save(UserEtherpadInfo userEtherpadInfo) {
        return userEtherpadInfoRepository.save(userEtherpadInfo);
    }

    @Override
    public UserEtherpadInfo findById(Long id) {
        return userEtherpadInfoRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = false)
    public void storeSessionForUser(String sessionId, String groupId, Long validUntil, UserEtherpadInfo userEtherpadInfo) {
        userEtherpadInfo = userEtherpadInfoRepository.findById(userEtherpadInfo.getId());
        userEtherpadInfo.setSessionId(sessionId);
        userEtherpadInfo.setGroupId(groupId);
        userEtherpadInfo.setValidUntil(validUntil);
        userEtherpadInfoRepository.save(userEtherpadInfo);
    }

    @Override
    @Transactional(readOnly = false)
    public void storeAuthorIdForCurrentUser(String authorId) {
        // 1.1.2.1 register an AuthorId for the Etherpad Server and store the Author Id for current user
        UserEtherpadInfo userEtherpadInfo = new UserEtherpadInfo();
        userEtherpadInfo.setAuthorId(authorId);
        User currentUser = Core.currentUser();
        User user = userService.findById(currentUser.getId());
        userEtherpadInfo.setUser(user);
        userEtherpadInfoRepository.save(userEtherpadInfo);
    }

    @Override
    @Transactional(readOnly = true)
    public UserEtherpadInfo getUserEtherpadInfoForCurrentUser() {
        User currentUser = Core.currentUser();
        User user = userService.findById(currentUser.getId());
        return userEtherpadInfoRepository.findByUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserEtherpadInfo findByAuthorId(String authorId) {
        return userEtherpadInfoRepository.findByAuthorId(authorId);
    }

    @Override
    public UserEtherpadInfo findBySessionId(String sessionId) {
        return userEtherpadInfoRepository.findBySessionId(sessionId);
    }
}
