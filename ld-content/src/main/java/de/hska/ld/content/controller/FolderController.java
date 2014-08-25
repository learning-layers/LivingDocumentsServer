package de.hska.ld.content.controller;

import de.hska.ld.content.dto.FolderDto;
import de.hska.ld.content.persistence.domain.Folder;
import de.hska.ld.content.service.FolderService;
import de.hska.ld.content.util.Content;
import de.hska.ld.core.util.Core;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(Content.RESOURCE_FOLDER)
public class FolderController {

    @Autowired
    private FolderService folderService;

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

}
