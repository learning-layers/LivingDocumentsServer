package de.hska.ld.content.service;

import de.hska.ld.content.persistence.domain.Access;
import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.content.persistence.domain.Folder;
import de.hska.ld.content.persistence.domain.UserGroup;
import de.hska.ld.core.persistence.domain.User;
import org.springframework.data.domain.Page;

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

    Folder updateFolder(Long folderId, Folder folder);

    void markAsDeleted(Long folderId, boolean force);

    List<Folder> findFoldersByChildFolderId(Long childFolderId);

    List<Folder> findFoldersByChildFolderIdAndCreatorId(Long childFolderId, Long creatorId);

    List<Folder> getFoldersByUser(User user);

    List<Folder> getSubFoldersByFolderId(Long folderId);

    Folder loadSubDocumentList(Long folderId);

    Page<Document> getSubDocumentsPage(Long folderId, Integer pageNumber, Integer pageSize, String sortDirection, String sortProperty);

    boolean isPredecessorOf(Long id, Long id1);

    Page<Folder> getFoldersPage(int pageNumber, int pageSize, String sortDirection, String sortProperty);
}
