package de.hska.ld.content.controller;

import de.hska.ld.content.persistence.domain.Tag;
import de.hska.ld.content.service.TagService;
import de.hska.ld.content.util.Content;
import de.hska.ld.core.util.Core;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
     * <b>Path:</b> GET {@value de.hska.ld.content.util.Content#RESOURCE_TAG}
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
    public ResponseEntity<Page<Tag>> getTagsPage(@RequestParam(value = "page-number", defaultValue = "0") Integer pageNumber,
                                                 @RequestParam(value = "page-size", defaultValue = "10") Integer pageSize,
                                                 @RequestParam(value = "sort-direction", defaultValue = "DESC") String sortDirection,
                                                 @RequestParam(value = "sort-property", defaultValue = "createdAt") String sortProperty) {
        Page<Tag> tagsPage = tagService.getTagsPage(pageNumber, pageSize, sortDirection, sortProperty);
        if (tagsPage != null) {
            return new ResponseEntity<>(tagsPage, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * @return
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(value = "tags", method = RequestMethod.GET)
    public ResponseEntity<List<Tag>> getTags() {
        List<Tag> tagList = tagService.findAll();
        if (tagList != null && !tagList.isEmpty()) {
            return new ResponseEntity<>(tagList, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
