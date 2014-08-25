package de.hska.ld.content.service;

import de.hska.ld.content.persistence.domain.Access;
import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.content.persistence.domain.Folder;
import de.hska.ld.core.AbstractIntegrationTest;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.UserService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

import static de.hska.ld.content.ContentFixture.newDocument;

public class FolderServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private FolderService folderService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private UserService userService;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        setAuthentication(testUser);
    }

    @Test
    @Transactional
    public void testCreateSimpleFolderStructure() {
        Folder folder = folderService.createFolder("Folder");
        Assert.assertNotNull(folder);

        Folder subFolder = folderService.createFolder("Subfolder", folder.getId());
        Assert.assertNotNull(subFolder);
        Assert.assertTrue(subFolder.getParentFolderList().size() > 0);
        Assert.assertTrue(subFolder.getParentFolderList().get(0).getId().equals(folder.getId()));

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

    @Test
    public void testFolderSharingFunctionality() {
        Folder newFolder = folderService.createFolder("New Folder");
        User adminUser = userService.findByUsername("admin");

        folderService.shareFolder(newFolder.getId(), Arrays.asList(adminUser), Access.Permission.WRITE);

        setAuthentication(adminUser);

        Folder sharedItemsFolder = folderService.getSharedItemsFolder(adminUser.getId());
        Assert.assertNotNull(sharedItemsFolder);
        sharedItemsFolder = folderService.loadSubFolderList(sharedItemsFolder.getId());
        Assert.assertEquals(1, sharedItemsFolder.getFolderList().size());
    }
}
