package de.hska.ld.content.service;

import de.hska.ld.content.persistence.domain.Folder;
import de.hska.ld.core.service.Service;

public interface FolderService extends Service<Folder> {

    Folder createFolder(String folderName);

    Folder createFolder(String folderName, Long parentId);

    Folder placeDocumentInFolder(Long folderId, Long documentId);
}
