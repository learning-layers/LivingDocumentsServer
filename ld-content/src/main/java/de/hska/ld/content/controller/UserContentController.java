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

package de.hska.ld.content.controller;

import de.hska.ld.content.events.user.UserContentEventsPublisher;
import de.hska.ld.content.persistence.domain.Tag;
import de.hska.ld.content.service.TagService;
import de.hska.ld.content.service.UserContentInfoService;
import de.hska.ld.content.util.Content;
import de.hska.ld.core.exception.NotFoundException;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.UserService;
import de.hska.ld.core.util.AsyncExecutor;
import de.hska.ld.core.util.Core;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.concurrent.Callable;

@RestController
@RequestMapping(Content.RESOURCE_USER)
public class UserContentController {
    @Autowired
    private UserContentInfoService userContentInfoService;

    @Autowired
    private UserService userService;

    @Autowired
    private TagService tagService;

    @Autowired
    private AsyncExecutor asyncExecutor;

    @Autowired
    private UserContentEventsPublisher userContentEventsPublisher;

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST, value = "/{userId}/tag/{tagId}")
    @Transactional(readOnly = false)
    public Callable addTag(@PathVariable Long userId, @PathVariable Long tagId) {
        return () -> {
            //User currentUser = Core.currentUser();
            //if (currentUser.getId().equals(userId)) {
            User user = userService.findById(userId);
            userContentInfoService.addTag(user.getId(), tagId);
            Tag tag = tagService.findById(tagId);
            userContentEventsPublisher.sendAddTagEvent(user, tag);
            // TODO publish add tag event
            return new ResponseEntity<>(HttpStatus.OK);
            /*} else {
                return new ResponseEntity<>(HttpStatus.OK);
            }*/
        };
    }

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.DELETE, value = "/{userId}/tag/{tagId}")
    public Callable removeTag(@PathVariable Long userId, @PathVariable Long tagId) {
        return () -> {
            userContentInfoService.removeTag(userId, tagId);
            return new ResponseEntity<>(HttpStatus.OK);
        };
    }

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/{userId}/tags")
    @Transactional(readOnly = true)
    public Callable getTagsPage(@PathVariable Long userId,
                                @RequestParam(value = "page-number", defaultValue = "0") Integer pageNumber,
                                @RequestParam(value = "page-size", defaultValue = "10") Integer pageSize,
                                @RequestParam(value = "sort-direction", defaultValue = "DESC") String sortDirection,
                                @RequestParam(value = "sort-property", defaultValue = "id") String sortProperty) {
        return () -> {
            Page<Tag> tagsPage = userContentInfoService.getUserContentTagsPage(userId, pageNumber, pageSize, sortDirection, sortProperty);
            if (tagsPage != null && tagsPage.getNumberOfElements() > 0) {
                return new ResponseEntity<>(tagsPage, HttpStatus.OK);
            } else {

                User userAux = userService.findById(userId);
                if (userAux == null) {
                    throw new NotFoundException();
                } else {
                    //User exists but doesn't have any tag
                    return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
                }
            }
        };
    }
}
