package de.hska.ld.content.service.impl;

import de.hska.ld.content.persistence.domain.Access;
import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.content.persistence.domain.Folder;
import de.hska.ld.content.persistence.domain.UserGroup;
import de.hska.ld.content.persistence.repository.FolderRepository;
import de.hska.ld.content.service.DocumentService;
import de.hska.ld.content.service.FolderService;
import de.hska.ld.core.exception.NotFoundException;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FolderServiceImpl extends AbstractContentService<Folder> implements FolderService {

    @Autowired
    private FolderRepository repository;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private UserService userService;

    @Override
    public Folder createFolder(String folderName) {
        return save(new Folder(folderName));
    }

    @Override
    @Transactional
    public Folder createFolder(String folderName, Long parentId) {
        Folder parent = null;

        if (parentId != null) {
            parent = findById(parentId);
            if (parent == null) {
                throw new NotFoundException("predecessorId");
            }
        }

        Folder folder = new Folder(folderName);
        if (parent != null) {
            folder.setParent(parent);
        }
        folder = save(folder);
        final Folder finalFolder = folder;
        if (parent != null) {
            parent.getFolderList().removeIf(f -> f.getId().equals(finalFolder.getId()));
            parent.getFolderList().add(folder);
            save(parent);
        }

        return folder;
    }

    @Override
    public Folder placeDocumentInFolder(Long folderId, Long documentId) {
        Document document = documentService.findById(documentId);
        Folder folder = findById(folderId);

        folder.getDocumentList().add(document);

        return save(folder);
    }

    @Override
    @Transactional
    public Folder shareFolder(Long folderId, UserGroup userGroup, Access.Permission... permission) {
        Folder folder = findById(folderId);
        if (folder == null) {
            throw new NotFoundException("folderId");
        }
        shareFolder(folderId, userGroup.getUserList(), permission);
        for (UserGroup subUserGroup : userGroup.getUserGroupList()) {
            shareFolder(folderId, subUserGroup, permission);
        }

        return folder;
    }

    @Override
    @Transactional
    public Folder loadSubFolderList(Long folderId) {
        Folder folder = findById(folderId);
        // TODO add permission check
        //checkPermission(folder, Access.Permission.READ);
        folder.getFolderList().size();
        return folder;
    }

    @Override
    @Transactional
    public Folder getSharedItemsFolder(Long userId) {
        User user = userService.findById(userId);
        List<Folder> folders = repository.findByCreatorAndSharingFolderTrue(user);
        Folder sharedFolder = null;
        if (folders != null && folders.size() > 0) {
            sharedFolder = folders.get(0);
        } else {
            sharedFolder = createSharedItemsFolder(user);
        }
        return sharedFolder;
    }

    @Override
    @Transactional
    public Folder shareFolder(Long folderId, List<User> userList, Access.Permission... permission) {
        Folder folder = findById(folderId);
        for (User user : userList) {
            Folder sharedItemsFolder = getSharedItemsFolder(user.getId());
            sharedItemsFolder.getFolderList().add(folder);
            folder.setParent(sharedItemsFolder);
            super.save(sharedItemsFolder);
            addAccess(folder.getId(), user, permission);
        }
        for (Folder subfolder : folder.getFolderList()) {
            shareSubFolder(subfolder.getId(), userList, permission);
        }
        // TODO add document access
        return folder;
    }

    public Folder shareSubFolder(Long folderId, List<User> userList, Access.Permission... permission) {
        Folder folder = findById(folderId);
        for (User user : userList) {
            addAccess(folder.getId(), user, permission);
        }
        return folder;
    }

    private Folder createSharedItemsFolder(User user) {
        Folder newSharedItemsFolder = new Folder("SharedItems");
        newSharedItemsFolder.setCreator(user);
        newSharedItemsFolder.setSharingFolder(true);
        return super.save(newSharedItemsFolder);
    }

    @Override
    public FolderRepository getRepository() {
        return repository;
    }
}
