package de.hska.ld.content.controller;

import de.hska.ld.content.dto.FolderDto;
import de.hska.ld.content.persistence.domain.Access;
import de.hska.ld.content.persistence.domain.Folder;
import de.hska.ld.content.service.FolderService;
import de.hska.ld.content.util.Content;
import de.hska.ld.core.exception.ValidationException;
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
    @RequestMapping(method = RequestMethod.PUT, value = "/{folderId}")
    public ResponseEntity<Folder> updateFolder(@PathVariable Long folderId, @RequestBody Folder folder) {
        Folder folderResponse = folderService.updateFolder(folderId, folder);
        // Include jsonParentId
        FolderDto folderDto = new FolderDto(folderResponse);
        return new ResponseEntity<>(folderDto, HttpStatus.OK);
    }

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST, value = "/{folderId}/documents/{documentId}")
    public ResponseEntity<Folder> addDocumentToFolder(@PathVariable Long folderId, @PathVariable Long documentId) {
        Folder folder = folderService.placeDocumentInFolder(folderId, documentId);
        FolderDto folderDto = new FolderDto(folder);
        return new ResponseEntity<>(folderDto, HttpStatus.OK);
    }

    /**
     * ================= Sharing methods ================= *
     */

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST, value = "/{folderId}/share")
    public ResponseEntity<Folder> shareFolder(@PathVariable Long folderId,
                                              @RequestParam(value = "users", defaultValue = "") String usersString,
                                              @RequestParam(value = "permissions", defaultValue = "") String permissionString) {
        // parse request data
        List<User> userList = parseUserIdString(usersString);
        Access.Permission[] permissionArray = parseAccessPermissions(permissionString);

        Folder folder = folderService.shareFolder(folderId, userList, permissionArray);
        if (folder != null) {
            return new ResponseEntity<>(folder, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST, value = "/{folderId}/share/revoke")
    public ResponseEntity<Folder> revokeShareFolder(@PathVariable Long folderId,
                                                    @RequestParam(value = "users", defaultValue = "") String usersString,
                                                    @RequestParam(value = "permissions", defaultValue = "") String permissionString) {
        List<User> userList = parseUserIdString(usersString);
        Access.Permission[] permissionArray = parseAccessPermissions(permissionString);

        Folder folder = folderService.revokeShareFolder(folderId, userList, permissionArray);
        if (folder != null) {
            return new ResponseEntity<>(folder, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    private Access.Permission[] parseAccessPermissions(String permissionString) {
        Access.Permission[] permissionArray = null;
        try {
            String[] permissionStringArray = permissionString.split(";");
            permissionArray = new Access.Permission[permissionStringArray.length];
            int i = 0;
            for (String permissionStringItem : permissionStringArray) {
                permissionArray[i] = Access.Permission.valueOf(permissionStringItem);
                i++;
            }
        } catch (Exception e) {
            throw new ValidationException("permissionString");
        }
        return permissionArray;
    }

    private List<User> parseUserIdString(String usersString) {
        List<User> userList = null;
        try {
            String[] userIdStringArray = usersString.split(";");
            List<String> userIdList = Arrays.asList(userIdStringArray);
            userList = new ArrayList<>();
            for (String userId : userIdList) {
                User user = userService.findById(Long.parseLong(userId));
                userList.add(user);
            }
        } catch (Exception e) {
            throw new ValidationException("userIdString");
        }
        return userList;
    }
}
