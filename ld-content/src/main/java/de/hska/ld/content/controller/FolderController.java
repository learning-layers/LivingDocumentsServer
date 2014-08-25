package de.hska.ld.content.controller;

import de.hska.ld.content.dto.FolderDto;
import de.hska.ld.content.persistence.domain.Access;
import de.hska.ld.content.persistence.domain.Folder;
import de.hska.ld.content.service.FolderService;
import de.hska.ld.content.util.Content;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.UserService;
import de.hska.ld.core.util.Core;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping(Content.RESOURCE_FOLDER)
public class FolderController {

    @Autowired
    private FolderService folderService;

    @Autowired
    private UserService userService;

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Folder> createFolder(@RequestBody Folder folder) {
        folder = folderService.createFolder(folder.getName());
        return new ResponseEntity<>(folder, HttpStatus.CREATED);
    }

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST, value = "/{parentId}/folders")
    public ResponseEntity<Folder> createSubFolder(@PathVariable Long parentId, @RequestBody Folder folder) {
        folder = folderService.createFolder(folder.getName(), parentId);
        // Include jsonParentId
        FolderDto folderDto = new FolderDto(folder);
        return new ResponseEntity<>(folderDto, HttpStatus.CREATED);
    }

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST, value = "/{folderId}/documents/{documentId}")
    public ResponseEntity<Folder> addDocumentToFolder(@PathVariable Long folderId, @PathVariable Long documentId) {
        Folder folder = folderService.placeDocumentInFolder(folderId, documentId);
        FolderDto folderDto = new FolderDto(folder);
        return new ResponseEntity<>(folderDto, HttpStatus.OK);
    }

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST, value = "/{folderId}/share")
    public ResponseEntity<Folder> shareFolder(@PathVariable Long folderId,
                                              @RequestParam(value = "users", defaultValue = "") String usersString,
                                              @RequestParam(value = "permissions", defaultValue = "") String permissionString) {
        List<Access.Permission> permissionList = null;
        List<User> userList = null;
        try {
            String[] userIdStringArray = usersString.split(";");
            List<String> userIdList = Arrays.asList(userIdStringArray);
            userList = new ArrayList<>();
            for (String userId : userIdList) {
                User user = userService.findById(Long.parseLong(userId));
                userList.add(user);
            }

            String[] permissionStringArray = permissionString.split(";");
            permissionList = new ArrayList<>();
            for (String permissionStringItem : permissionStringArray) {
                Access.Permission permission = Access.Permission.valueOf(permissionStringItem);
                permissionList.add(permission);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Folder folder = null;
        for (Access.Permission permission : permissionList) {
            folder = folderService.shareFolder(folderId, userList, permission);
        }
        if (folder != null) {
            return new ResponseEntity<>(folder, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }
}
