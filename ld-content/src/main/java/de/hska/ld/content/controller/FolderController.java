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

    /**
     * This resource allows it to create a folder that has no parent folder (Folder below root folder).
     * <p>
     * <pre>
     *     <b>Required roles:</b> ROLE_USER
     *     <b>Path:</b> POST /api/folders
     * </pre>
     *
     * @param folder A folder object containing the folder name. Example:<br>
     *               {name: "&lt;folderName&gt;}
     * @return <b>200 OK</b> with the generated folder<br>
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Folder> createFolder(@RequestBody Folder folder) {
        folder = folderService.createFolder(folder.getName());
        return new ResponseEntity<>(folder, HttpStatus.CREATED);
    }

    /**
     * This resource allows it to create a folder that is put into a parent folder.
     * <p>
     * <pre>
     *     <b>Required roles:</b> ROLE_USER
     *     <b>Path:</b> POST /api/folders/{parentId}/folders
     * </pre>
     *
     * @param parentId The id of the parent folder
     * @param folder A folder object containing the folder name. Example:<br>
     *               {name: "&lt;folderName&gt;}
     *
     * @return <b>200 OK</b> with the generated folder<br>
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST, value = "/{parentId}/folders")
    public ResponseEntity<Folder> createSubFolder(@PathVariable Long parentId, @RequestBody Folder folder) {
        folder = folderService.createFolder(folder.getName(), parentId);
        // Include jsonParentId
        FolderDto folderDto = new FolderDto(folder);
        return new ResponseEntity<>(folderDto, HttpStatus.CREATED);
    }

    /**
     * This resource allows it to update a folder.
     *
     * <p>
     * <pre>
     *     <b>Required roles:</b> ROLE_USER
     *     <b>Path:</b> PUT /api/folders/{folderId}
     * </pre>
     *
     * @param folderId The id of the folder.
     * @param folder A folder object containing the folder name. Example:<br>
     *               {name: "&lt;folderName&gt;}
     *
     * @return <b>200 OK</b> with the renamed or updated folder<br>
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.PUT, value = "/{folderId}")
    public ResponseEntity<Folder> updateFolder(@PathVariable Long folderId, @RequestBody Folder folder) {
        Folder folderResponse = folderService.updateFolder(folderId, folder);
        // Include jsonParentId
        FolderDto folderDto = new FolderDto(folderResponse);
        return new ResponseEntity<>(folderDto, HttpStatus.OK);
    }

    /**
     * This resource allows it to move a document to a specific folder.
     *
     * <p>
     * <pre>
     *     <b>Required roles:</b> ROLE_USER
     *     <b>Path:</b> POST /api/folders/{folderId}/documents/{documentId}
     * </pre>
     *
     * @param parentFolderIdString The current parent folder or -1 if no parent folder is set.
     * @param newParentFolderId The folder id of the folder the document should be added to.
     * @param documentId The id of the document that shall be placed into the folder.
     *
     * @return <b>200 OK</b> with the renamed or updated folder<br>
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST, value = "/{folderId}/documents/{documentId}")
    public ResponseEntity<Folder> moveDocumentToFolder(@PathVariable Long newParentFolderId, @PathVariable Long documentId,
                                                       @RequestParam(value = "oldParentFolderId", defaultValue = "") String parentFolderIdString) {
        // TODO check if the document has been moved
        Long parentFolderId = null;
        try {
            parentFolderId = Long.parseLong(parentFolderIdString);
        } catch (Exception e) {
            throw new ValidationException("parentFolderId");
        }
        Folder folder = folderService.moveDocumentToFolder(parentFolderId, newParentFolderId, documentId);
        FolderDto folderDto = new FolderDto(folder);
        return new ResponseEntity<>(folderDto, HttpStatus.OK);
    }

    /**
     * ================= Sharing methods ================= *
     */

    /**
     * This resource allows it to share a folder.
     *
     * <p>
     * <pre>
     *     <b>Required roles:</b> ROLE_USER
     *     <b>Path:</b> PUT /api/folders/{folderId}/share?users=1;2;3;4&permissions=READ,WRITE
     * </pre>
     *
     * @param folderId The folder id of the folder the document should be added to.
     * @param usersString The string that contains the user ids of the users this folder shall be shared with.<br>
     *                    Example:<br>
     *                    usersString: 1;2;3;4
     * @param permissionString The string containing the permissions to set on the folder.<br>
     *                         Example:<br>
     *                         permissionString: READ;WRITE
     *
     * @return <b>200 OK</b> with the renamed or updated folder<br>
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

    /**
     * This resource allows it to revoke the sharing of a folder.
     *
     * <p>
     * <pre>
     *     <b>Required roles:</b> ROLE_USER
     *     <b>Path:</b> PUT /api/folders/{folderId}/share/revoke?users=1;2;3;4&permissions=READ,WRITE
     * </pre>
     *
     * @param folderId The folder id of the folder the document should be added to.
     * @param usersString The string that contains the user ids of the users this folder shall be shared with.<br>
     *                    Example:<br>
     *                    usersString: 1;2;3;4
     * @param permissionString The string containing the permissions to set on the folder.<br>
     *                         Example:<br>
     *                         permissionString: READ;WRITE
     *
     * @return <b>200 OK</b> with the renamed or updated folder<br>
     */
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
