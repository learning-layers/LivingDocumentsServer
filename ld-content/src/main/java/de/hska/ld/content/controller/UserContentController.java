package de.hska.ld.content.controller;

import de.hska.ld.content.service.UserContentInfoService;
import de.hska.ld.content.util.Content;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.UserService;
import de.hska.ld.core.util.AsyncExecutor;
import de.hska.ld.core.util.Core;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
            User currentUser = Core.currentUser();
            if (currentUser.getId().equals(userId)) {
                User user = userService.findById(userId);
                userContentInfoService.addTag(user.getId(), tagId);
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.OK);
            }
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
}
