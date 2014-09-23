package de.hska.ld.content.service.impl;

import de.hska.ld.content.persistence.domain.Access;
import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.content.persistence.domain.Folder;
import de.hska.ld.content.persistence.domain.UserGroup;
import de.hska.ld.content.persistence.repository.FolderRepository;
import de.hska.ld.content.service.DocumentService;
import de.hska.ld.content.service.FolderService;
import de.hska.ld.core.exception.NotFoundException;
import de.hska.ld.core.exception.ValidationException;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.UserService;
import de.hska.ld.core.util.Core;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
            parent.getFolderList().add(folder);
            folder.setParent(parent);
            save(parent);
            return parent.getFolderList().get(parent.getFolderList().size() - 1);
        }
        return null;
    }

    @Override
    @Transactional
    public Folder moveFolderToFolder(Long parentFolderId, Long newParentFolderId, Long folderId) {
        if (parentFolderId.equals(newParentFolderId)) {
            throw new ValidationException("id: new parent folder is the same as the old parent folder.");
        }
        Folder folder = findById(folderId);
        Folder newParentFolder = null;
        boolean isRootIsNewParentFolder = false;
        if (newParentFolderId != -1) {
            // not moved to the root folder
            newParentFolder = findById(newParentFolderId);
            if (newParentFolder == null) {
                throw new ValidationException("The new parent folder with id=" + newParentFolderId + " does not exist.");
            }
        } else {
            isRootIsNewParentFolder = true;
        }

        if (parentFolderId != -1) {
            // the current parent folder is not the root node
            moveFolderParentNotRoot(parentFolderId, folder, newParentFolder);
        } else {
            // the current parent folder is the root node
            moveFolderParentRoot(folder, newParentFolder);
        }
        if (isRootIsNewParentFolder) {
            return null;
        } else {
            return save(newParentFolder);
        }
    }

    private void moveFolderParentRoot(Folder folder, Folder newParentFolder) {
        if (newParentFolder != null) {
            // new parent is not the root folder
            if (!folder.getCreator().equals(Core.currentUser())) {
                // current user is not the creator of the folder
                if (newParentFolder.getCreator().equals(folder.getCreator())) {
                    folder.setParent(newParentFolder);
                } else {
                    List<Folder> parentFolderList = findFoldersByChildFolderIdAndCreatorId(folder.getId(), Core.currentUser().getId());
                    if (parentFolderList != null && parentFolderList.size() > 0) {
                        parentFolderList.forEach(f -> {
                            f.getFolderList().remove(folder);
                        });
                    }
                }
                // Move the document in the folder structure of the current user
                // Add document to the new folder
                newParentFolder.getFolderList().add(folder);
            } else {
                // Move the document in the folder structure of the creator
                // Add document to the new folder
                List<Access> accessList = newParentFolder.getAccessList();
                folder.setAccessList(new ArrayList<>(accessList));
                folder.setAccessAll(newParentFolder.isAccessAll());
                newParentFolder.getFolderList().add(folder);
                folder.setParent(newParentFolder);
                propagateAccessSettings(folder.getFolderList(), folder.getDocumentList(), accessList);
            }
        } else {
            // set new parent folder to root
            folder.setParent(null);
            save(folder);
        }
    }

    private void moveFolderParentNotRoot(Long parentFolderId, Folder folder, Folder newParentFolder) {
        Folder parentFolder = findById(parentFolderId);
        if (parentFolder == null) {
            throw new ValidationException("The old parent folder with id=" + parentFolderId + " does not exist.");
        }
        if (!parentFolder.getFolderList().contains(folder)) {
            throw new ValidationException("Folder is currently not in the given parent folder with id=" + parentFolderId);
        }

        if (newParentFolder != null) {
            // new parent is not the root folder
            if (parentFolder.getCreator() != Core.currentUser()
                    && newParentFolder.getCreator() != parentFolder.getCreator()) {
                // Move the document in the folder structure of the current user
                // Remove document from the current folder
                parentFolder.getFolderList().remove(folder);
                // Add document to the new folder
                newParentFolder.getFolderList().add(folder);
            } else {
                // Move the document in the folder structure of the creator
                // Remove document from the current folder
                parentFolder.getFolderList().remove(folder);
                // set the new parent folder
                folder.setParent(newParentFolder);
                // Add document to the new folder
                List<Access> accessList = newParentFolder.getAccessList();
                folder.setAccessList(new ArrayList<>(accessList));
                folder.setAccessAll(newParentFolder.isAccessAll());
                newParentFolder.getFolderList().add(folder);
                propagateAccessSettings(folder.getFolderList(), folder.getDocumentList(), accessList);
            }
        } else {
            // new parent is the root folder
            if (parentFolder.getCreator() != Core.currentUser()) {
                // Move the document in the folder structure of the current user
                // Remove document from the current folder
                parentFolder.getFolderList().remove(folder);
                // set new parent folder to root
                folder.setParent(null);
                save(folder);
            } else {
                // Move the document in the folder structure of the creator
                // Remove document from the current folder
                parentFolder.getFolderList().remove(folder);
                // set new parent folder to root
                folder.setParent(null);
                // TODO reset access settings
                save(folder);
            }
        }
        save(parentFolder);
    }

    private void propagateAccessSettings(List<Folder> folderList, List<Document> documentList, List<Access> accessList) {
        for (Folder f : folderList) {
            f.setAccessList(new ArrayList<>(accessList));
            save(f);
            propagateAccessSettings(f.getFolderList(), f.getDocumentList(), accessList);
        }
        for (Document d : documentList) {
            d.setAccessList(new ArrayList<>(accessList));
            documentService.saveContainsList(d);
        }
    }

    @Override
    @Transactional
    public Folder moveDocumentToFolder(Long parentFolderId, Long newParentFolderId, Long documentId) {
        if (parentFolderId != null && parentFolderId.equals(newParentFolderId)) {
            throw new ValidationException("id: new parent folder is the same as the old parent folder.");
        }
        Document document = documentService.findById(documentId);
        if (document == null) {
            throw new NotFoundException("documentId");
        }
        Folder newParentFolder = findById(newParentFolderId);
        if (newParentFolder == null) {
            throw new ValidationException("The new parent folder with id=" + newParentFolderId + " does not exist.");
        }

        if (parentFolderId != null && parentFolderId != -1) {
            Folder parentFolder = findById(parentFolderId);
            if (parentFolder == null) {
                throw new ValidationException("The old parent folder with id=" + parentFolderId + " does not exist.");
            }
            if (!parentFolder.getDocumentList().contains(document)) {
                throw new ValidationException("Document is currently not in the given parent folder with id=" + parentFolderId);
            }
            if (parentFolder.getCreator() != Core.currentUser()
                    && newParentFolder.getCreator() != parentFolder.getCreator()) {
                // Move the document in the folder structure of the current user
                // Remove document from the current folder
                parentFolder.getDocumentList().remove(document);
                // Add document to the new folder
                newParentFolder.getDocumentList().add(document);
            } else {
                // Move the document in the folder structure of the creator
                // Remove document from the current folder
                parentFolder.getDocumentList().remove(document);
                // Add document to the new folder
                List<Access> accessList = newParentFolder.getAccessList();
                document.setAccessList(new ArrayList<>(accessList));
                document.setAccessAll(newParentFolder.isAccessAll());
                newParentFolder.getDocumentList().add(document);
            }
            save(parentFolder);
        } else {
            if (document.getCreator() != Core.currentUser()
                    && newParentFolder.getCreator() != document.getCreator()) {
                // Move the document in the folder structure of the current user
                // Add document to the new folder
                newParentFolder.getDocumentList().add(document);
            } else {
                // Move the document in the folder structure of the creator
                // Add document to the new folder
                List<Access> accessList = newParentFolder.getAccessList();
                document.setAccessList(new ArrayList<>(accessList));
                document.setAccessAll(newParentFolder.isAccessAll());
                newParentFolder.getDocumentList().add(document);
            }
        }
        return save(newParentFolder);
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
    public Folder revokeShareFolder(Long folderId, UserGroup userGroup, Access.Permission... permission) {
        Folder folder = findById(folderId);
        if (folder == null) {
            throw new NotFoundException("folderId");
        }
        revokeShareFolder(folderId, userGroup.getUserList(), permission);
        for (UserGroup subUserGroup : userGroup.getUserGroupList()) {
            shareFolder(folderId, subUserGroup, permission);
        }
        return folder;
    }

    @Override
    @Transactional
    public Folder revokeShareFolder(Long folderId, List<User> userList, Access.Permission... permission) {
        Folder folder = findById(folderId);
        for (User user : userList) {
            if (Core.currentUser().getId().equals(user.getId())) {
                continue;
            }
            Folder sharedItemsFolder = getSharedItemsFolder(user.getId());
            sharedItemsFolder.getFolderList().size();
            sharedItemsFolder.getFolderList().remove(folder);
            List<Folder> parentFolderList = findFoldersByChildFolderIdAndCreatorId(folderId, user.getId());
            if (parentFolderList != null && parentFolderList.size() > 0) {
                parentFolderList.stream().filter(pf -> user.getId().equals(pf.getCreator().getId())).forEach(pf -> {
                    pf.getFolderList().remove(folder);
                    super.save(pf);
                });
            }
            removeAccess(folder.getId(), user, permission);
            super.save(folder);
        }
        for (Folder subFolder : folder.getFolderList()) {
            revokeShareSubFolder(subFolder.getId(), userList, permission);
        }
        return folder;
    }

    private Folder revokeShareSubFolder(Long folderId, List<User> userList, Access.Permission... permission) {
        Folder folder = findById(folderId);
        for (User user : userList) {
            if (Core.currentUser().getId().equals(user.getId())) {
                continue;
            }
            removeAccess(folder.getId(), user, permission);
        }
        return folder;
    }

    @Override
    @Transactional
    public Folder loadSubFolderList(Long folderId) {
        Folder folder = findById(folderId);
        // TODO add permission check
        //checkPermission(folder, Access.Permission.READ);
        if (folder == null) {
            throw new NotFoundException("folderId");
        }
        folder.getFolderList().size();
        return folder;
    }

    @Override
    @Transactional
    public Folder updateFolder(Long folderId, Folder folder) {
        Folder dbFolder = findById(folderId);
        if (dbFolder == null) {
            throw new ValidationException("folderId");
        }
        dbFolder.setName(folder.getName());
        return save(dbFolder);
    }

    @Override
    @Transactional
    public Folder getSharedItemsFolder(Long userId) {
        User user = userService.findById(userId);
        List<Folder> folders = repository.findByCreatorAndSharingFolderTrue(user);
        Folder sharedFolder;
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
        if (checkPermissionResult(folder, Access.Permission.WRITE)) {
            for (User user : userList) {
                if (Core.currentUser().getId().equals(user.getId())) {
                    continue;
                }
                Folder sharedItemsFolder = getSharedItemsFolder(user.getId());
                sharedItemsFolder.getFolderList().add(folder);
                // automatically does folder.getParentFolderList().add(sharedItemsFolder);
                super.save(sharedItemsFolder);
                addAccess(folder.getId(), user, permission);
                for (Document document : folder.getDocumentList()) {
                    documentService.addAccess(document.getId(), user, permission);
                }
            }
            for (Folder subFolder : folder.getFolderList()) {
                shareSubFolder(subFolder.getId(), userList, permission);
            }
        }
        return folder;
    }

    @Override
    @Transactional
    public void markAsDeleted(Long folderId, boolean force) {
        Folder folder = findById(folderId);
        if (folder == null) {
            throw new ValidationException("folderId");
        }
        folder.setDeleted(true);
        if (force) {
            // delete documents and folders within the folder as well
            for (Document document : folder.getDocumentList()) {
                documentService.markAsDeleted(document.getId());
            }
            for (Folder f : folder.getFolderList()) {
                markAsDeleted(f.getId(), true);
            }
        }
        save(folder);
    }

    @Override
    public List<Folder> findFoldersByChildFolderId(Long childFolderId) {
        return repository.findFoldersByChildFolderId(childFolderId);
    }

    @Override
    public List<Folder> findFoldersByChildFolderIdAndCreatorId(Long childFolderId, Long creatorId) {
        return repository.findFoldersByChildFolderIdAndCreatorId(childFolderId, creatorId);
    }

    @Override
    public List<Folder> getFoldersByUser(User user) {
        return repository.findCreatorRootFolders(user.getId());
    }

    @Override
    public List<Folder> getSubFoldersByFolderId(Long folderId) {
        //Folder folder = findById(folderId);
        //folder.getFolderList().size();
        return repository.getSubFoldersByFolderId(folderId);
    }

    @Override
    @Transactional
    public Folder loadSubDocumentList(Long folderId) {
        Folder folder = findById(folderId);
        if (folder == null) {
            throw new NotFoundException("folderId");
        }
        // TODO add permission check
        //checkPermission(folder, Access.Permission.READ);
        folder.getDocumentList().size();
        return folder;
    }

    @Override
    public Page<Document> getSubDocumentsPage(Long folderId, Integer pageNumber, Integer pageSize, String sortDirection, String sortProperty) {
        Folder folder = findById(folderId);
        if (folder == null) {
            throw new NotFoundException();
        }
        Sort.Direction direction;
        if (Sort.Direction.ASC.toString().equals(sortDirection)) {
            direction = Sort.Direction.ASC;
        } else {
            direction = Sort.Direction.DESC;
        }
        Pageable pageable = new PageRequest(pageNumber, pageSize, direction, sortProperty);
        return repository.getSubDocumentsPage(folderId, pageable);
    }

    public Folder shareSubFolder(Long folderId, List<User> userList, Access.Permission... permission) {
        Folder folder = findById(folderId);
        if (checkPermissionResult(folder, Access.Permission.WRITE)) {
            for (User user : userList) {
                if (Core.currentUser().getId().equals(user.getId())) {
                    continue;
                }
                addAccess(folder.getId(), user, permission);
                for (Document document : folder.getDocumentList()) {
                    documentService.addAccess(document.getId(), user, permission);
                }
            }
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
