package de.hska.ld.content.service;

import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.content.persistence.domain.Folder;
import de.hska.ld.core.AbstractIntegrationTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static de.hska.ld.content.ContentFixture.newDocument;

public class FolderServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private FolderService folderService;

    @Autowired
    private DocumentService documentService;

    @Test
    @Transactional
    public void testCreateSimpleFolderStructure() {
        Folder folder = folderService.createFolder("Folder");
        Assert.assertNotNull(folder);

        Folder subFolder = folderService.createFolder("Subfolder", folder.getId());
        Assert.assertNotNull(subFolder);
        Assert.assertNotNull(subFolder.getParent());
        Assert.assertTrue(subFolder.getParent().getId().equals(folder.getId()));

        folder = folderService.findById(folder.getId());

        Assert.assertNotNull(folder.getFolderList());
        Assert.assertTrue(folder.getFolderList().size() == 1);
        Assert.assertTrue(folder.getFolderList().get(0).getId().equals(subFolder.getId()));
    }

    @Test
    @Transactional
    public void testCreateFolderAndPutDocumentInIt() {
        Document document = documentService.save(newDocument());
        Folder folder = folderService.createFolder("Folder");
        folder = folderService.placeDocumentInFolder(folder.getId(), document.getId());

        Assert.assertNotNull(folder);
        Assert.assertTrue(folder.getDocumentList().size() == 1);
        Assert.assertTrue(folder.getDocumentList().get(0).getId().equals(document.getId()));
    }
}
