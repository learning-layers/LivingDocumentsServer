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

package de.hska.ld.content.controller;

import de.hska.ld.content.persistence.domain.Tag;
import de.hska.ld.content.service.TagService;
import de.hska.ld.content.util.Content;
import de.hska.ld.core.exception.NotFoundException;
import de.hska.ld.core.exception.ValidationException;
import de.hska.ld.core.util.Core;
import de.hska.ld.core.util.EscapeUtil;
import org.pmw.tinylog.Logger;
import org.pmw.tinylog.LoggingContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.Callable;

/**
 * <p><b>Resource:</b> {@value de.hska.ld.content.util.Content#RESOURCE_TAG}
 */
@RestController
@RequestMapping(Content.RESOURCE_TAG)
public class TagController {

    @Autowired
    private TagService tagService;

    /**
     * <pre>
     * Gets a page of tags.
     *
     * <b>Required roles:</b> ROLE_USER
     * <b>Path:</b> GET /api/tags?page-number=0&amp;page-size=10&amp;sort-direction=DESC&amp;sort-property=createdAt
     * </pre>
     *
     * @param pageNumber    the page number as a request parameter (default: 0)
     * @param pageSize      the page size as a request parameter (default: 10)
     * @param sortDirection the sort direction as a request parameter (default: 'DESC')
     * @param sortProperty  the sort property as a request parameter (default: 'createdAt')
     * @return <b>200 OK</b> and a tag page or <br>
     * <b>404 Not Found</b> if no tags exists or <br>
     * <b>403 Forbidden</b> if authorization failed
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET)
    public Callable getTagsPage(@RequestParam(value = "page-number", defaultValue = "0") Integer pageNumber,
                                @RequestParam(value = "page-size", defaultValue = "10") Integer pageSize,
                                @RequestParam(value = "sort-direction", defaultValue = "DESC") String sortDirection,
                                @RequestParam(value = "sort-property", defaultValue = "createdAt") String sortProperty) {
        return () -> {
            Page<Tag> tagsPage = tagService.getTagsPage(pageNumber, pageSize, sortDirection, sortProperty);
            if (tagsPage != null) {
                return new ResponseEntity<>(tagsPage, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        };
    }

    /**
     * This resource allows it to create a tag that can later be user for tagging.
     * <p>
     * <pre>
     *     <b>Required roles:</b> ROLE_USER
     *     <b>Path:</b> POST /api/tags
     * </pre>
     *
     * @param tag the tag that shall be created. Example:<br>
     *            {'name':'&lt;name&gt;', 'description': '&lt;description&gt;'} <br>
     *            description is optional
     * @return <b>201 CREATED</b> with the generated tag<br>
     * <b>400 Bad Request</b> if no title exists<br>
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST)
    public Callable createTag(@RequestBody final Tag tag) {
        return () -> {
            if (tag != null) {
                if ("".equals(tag.getName())) {
                    throw new ValidationException("name");
                }
                Tag dbTag = tagService.save(tag);
                try {
                    LoggingContext.put("user_email", EscapeUtil.escapeJsonForLogging(Core.currentUser().getEmail()));
                    LoggingContext.put("tagId", EscapeUtil.escapeJsonForLogging(dbTag.getId().toString()));
                    Logger.trace("User created tag.");
                } catch (Exception e) {
                    Logger.error(e);
                } finally {
                    LoggingContext.clear();
                }
                return new ResponseEntity<>(dbTag, HttpStatus.CREATED);
            } else {
                throw new ValidationException("No tag provided.");
            }
        };
    }

    /**
     * This resource allows it to update a tag.
     * <p>
     * <pre>
     *     <b>Required roles:</b> ROLE_USER
     *     <b>Path:</b> POST /api/tags/{tagId}
     * </pre>
     *
     * @param tag the tag that shall be created. Example:<br>
     *            {'name':'&lt;name&gt;', 'description': '&lt;description&gt;'} <br>
     *            description is optional
     * @return <b>200 OK</b> with the update tag<br>
     * <b>400 Bad Request</b> if no title exists<br>
     */
    //TODO but creates a new Tag also with the same name
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.PUT, value = "/{tagId}")
    public Callable updateTag(@PathVariable Long tagId, @RequestBody final Tag tag) {
        return () -> {
            if (tag != null) {
                Tag dbTag = tagService.updateTag(tagId, tag);
                return new ResponseEntity<>(dbTag, HttpStatus.OK);
            } else {
                throw new ValidationException("No tag provided.");
            }
        };
    }

    /**
     * Retrieve tags by name.
     * <p>
     * <p>
     * <pre>
     *     <b>Required roles:</b> ROLE_USER
     *     <b>Path:</b> GET /api/tags/name/{tagName}
     * </pre>
     *
     * @param tagName the name of the tags that shall be retrieved
     * @return the tag (TODO has to be changed to a tag list, multiple tags could have the same name, different description)
     *///TODO can't get an output because of multiple response elements (tags have the same name)
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/name/{tagName}")
    public Callable getTagByName(@PathVariable String tagName) {
        return () -> {
            if (tagName != null) {
                Tag tag = tagService.findByName(tagName);
                if(tag != null) {
                    return new ResponseEntity<>(tag, HttpStatus.OK);
                } else {
                    throw new NotFoundException("Tag not found.");
                }
            } else {
                throw new ValidationException("No tag provided.");
            }
        };
    }

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/name/{tagName}/list")
    public Callable getTagsPage(@PathVariable String tagName,
                                @RequestParam(value = "page-number", defaultValue = "0") Integer pageNumber,
                                @RequestParam(value = "page-size", defaultValue = "10") Integer pageSize,
                                @RequestParam(value = "sort-direction", defaultValue = "DESC") String sortDirection,
                                @RequestParam(value = "sort-property", defaultValue = "createdAt") String sortProperty) {
        return () -> {
            Page<Tag> tagsPage = tagService.getFindByTagNameTagsPage(tagName, pageNumber, pageSize, sortDirection, sortProperty);
            if (tagsPage != null) {
                return new ResponseEntity<>(tagsPage, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        };
    }
}
