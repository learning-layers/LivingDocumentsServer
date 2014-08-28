package de.hska.ld.content.service;

import de.hska.ld.content.persistence.domain.Access;
import de.hska.ld.content.persistence.domain.Folder;
import de.hska.ld.content.persistence.domain.UserGroup;
import de.hska.ld.core.persistence.domain.User;

import java.util.List;

public interface FolderService extends ContentService<Folder> {

    Folder createFolder(String folderName);

    Folder createFolder(String folderName, Long parentId);

    Folder moveFolderToFolder(Long parentFolderId, Long newParentFolderId, Long folderId);

    Folder moveDocumentToFolder(Long parentFolderId, Long newParentFolderId, Long documentId);

    Folder shareFolder(Long folderId, UserGroup userGroup, Access.Permission... permission);

    Folder revokeShareFolder(Long folderId, UserGroup userGroup, Access.Permission... permission);

    Folder shareFolder(Long folderId, List<User> userList, Access.Permission... permission);

    Folder revokeShareFolder(Long folderId, List<User> userList, Access.Permission... permission);

    Folder getSharedItemsFolder(Long userId);

    Folder loadSubFolderList(Long folderId);

    Folder loadParentFolderList(Long id);

    Folder updateFolder(Long folderId, Folder folder);

    void markAsDeleted(Long folderId, boolean force);
}
