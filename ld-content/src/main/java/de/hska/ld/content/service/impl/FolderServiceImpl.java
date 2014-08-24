package de.hska.ld.content.service.impl;

import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.content.persistence.domain.Folder;
import de.hska.ld.content.persistence.repository.FolderRepository;
import de.hska.ld.content.service.DocumentService;
import de.hska.ld.content.service.FolderService;
import de.hska.ld.core.exception.NotFoundException;
import de.hska.ld.core.service.impl.AbstractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FolderServiceImpl extends AbstractService<Folder> implements FolderService {

    @Autowired
    private FolderRepository repository;

    @Autowired
    private DocumentService documentService;

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

        if (parent != null) {
            parent.getFolderList().removeIf(s -> s.getName().equals(folderName));
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
    public FolderRepository getRepository() {
        return repository;
    }
}
