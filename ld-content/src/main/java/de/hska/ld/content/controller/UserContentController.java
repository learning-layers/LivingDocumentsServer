package de.hska.ld.content.controller;

import de.hska.ld.content.persistence.domain.Tag;
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
    private AsyncExecutor asyncExecutor;

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST, value = "/{userId}/tag/{tagId}")
    @Transactional(readOnly = true)
    public Callable addTag(@PathVariable Long userId, @PathVariable Long tagId) {
        return () -> {
            //User currentUser = Core.currentUser();
            //if (currentUser.getId().equals(userId)) {
                User user = userService.findById(userId);
                userContentInfoService.addTag(user.getId(), tagId);
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
                if(userAux==null) {
                    throw new NotFoundException();
                }else{
                    //User exists but doesn't have any tag
                    return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
                }
            }
        };
    }
}
