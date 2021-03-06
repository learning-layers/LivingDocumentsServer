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

import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.UserService;
import de.hska.ld.oidc.persistence.domain.UserSSSInfo;
import de.hska.ld.oidc.persistence.repository.UserSSSInfoRepository;
import de.hska.ld.oidc.service.UserSSSInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserSSSInfoServiceImpl implements UserSSSInfoService {
    @Autowired
    private UserService userService;

    @Autowired
    private UserSSSInfoRepository repository;

    @Transactional
    public void addUserSSSInfo(Long userId, String sssUserId) {
        User user = userService.findById(userId);
        UserSSSInfo userSSSInfo = new UserSSSInfo();
        userSSSInfo.setUser(user);
        userSSSInfo.setSssUserId(sssUserId);
        repository.save(userSSSInfo);
    }

    @Override
    public UserSSSInfo findByUser(User user) {
        return repository.findByUser(user);
    }

    @Override
    public UserSSSInfo findByUserEmail(String email) {
        return repository.findByUserEmail(email);
    }

    public UserSSSInfoRepository getRepository() {
        return repository;
    }
}
