/*
 *  Code contributed to the Learning Layers project
 *  http://www.learning-layers.eu
 *  Development is partly funded by the FP7 Programme of the European
 *  Commission under Grant Agreement FP7-ICT-318209.
 *  Copyright (c) 2015, Karlsruhe University of Applied Sciences.
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

package de.hska.ld.content.service.impl;

import de.hska.ld.content.persistence.domain.Tag;
import de.hska.ld.content.persistence.domain.UserContentInfo;
import de.hska.ld.content.persistence.repository.UserContentInfoRepository;
import de.hska.ld.content.service.TagService;
import de.hska.ld.content.service.UserContentInfoService;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class UserContentInfoServiceImpl implements UserContentInfoService {

    @Autowired
    private UserService userService;

    @Autowired
    private TagService tagService;

    @Autowired
    private UserContentInfoRepository userContentInfoRepository;

    @Override
    public UserContentInfo addTag(Long userId, Long tagId) {
        User user = userService.findById(userId);
        Tag tag = tagService.findById(tagId);

        UserContentInfo userContentInfo = userContentInfoRepository.findByUser(user);
        if (userContentInfo == null) {
            userContentInfo = new UserContentInfo();
            userContentInfo.setUser(user);
        }
        //checkPermission(document, Access.Permission.WRITE, Access.Permission.ATTACH_FILES, Access.Permission.READ, Access.Permission.COMMENT_DOCUMENT);
        if (!userContentInfo.getTagList().contains(tag)) {
            userContentInfo.getTagList().add(tag);
        }
        return userContentInfoRepository.save(userContentInfo);
    }

    @Override
    public void removeTag(Long userId, Long tagId) {
        User user = userService.findById(userId);
        UserContentInfo userContentInfo = userContentInfoRepository.findByUser(user);
        if (userContentInfo != null) {
            Tag tag = tagService.findById(tagId);
            userContentInfo.getTagList().remove(tag);
            userContentInfoRepository.save(userContentInfo);
        }
    }

    @Override
    public Page<Tag> getUserContentTagsPage(Long userId, Integer pageNumber, Integer pageSize, String sortDirection, String sortProperty) {
        Sort.Direction direction;
        if (Sort.Direction.ASC.toString().equals(sortDirection)) {
            direction = Sort.Direction.ASC;
        } else {
            direction = Sort.Direction.DESC;
        }
        Pageable pageable = new PageRequest(pageNumber, pageSize, direction, sortProperty);
        User user = userService.findById(userId);
        UserContentInfo userContentInfo = userContentInfoRepository.findByUser(user);
        if (userContentInfo != null) {
            return userContentInfoRepository.findAllTagsForUserContent(userContentInfo.getId(), pageable);
        } else
            return null;
    }

    @Override
    public UserContentInfo findByUserId(Long userId) {
        User user = userService.findById(userId);
        return userContentInfoRepository.findByUser(user);
    }

    @Override
    public UserContentInfo createInitialUserContentInfo(Long userId) {
        UserContentInfo userContentInfo = new UserContentInfo();
        User user = userService.findById(userId);
        userContentInfo.setUser(user);
        return userContentInfoRepository.save(userContentInfo);
    }
}
