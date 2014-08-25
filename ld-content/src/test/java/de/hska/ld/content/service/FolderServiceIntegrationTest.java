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
    public void testCreateSimpleFolderStructure() {
        Folder folder = folderService.createFolder("Folder");
        Assert.assertNotNull(folder);

        Folder subFolder = folderService.createFolder("Subfolder", folder.getId());
        Assert.assertNotNull(subFolder);
        Assert.assertNotNull(subFolder.getParent());
        Assert.assertNotNull(subFolder.getParent().getId());

        folder = folderService.findById(folder.getId());
        folder = folderService.loadSubFolderList(folder.getId());

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
    public void testFolderSharing() {
        Folder newFolder = folderService.createFolder("New Folder");
        User adminUser = userService.findByUsername("admin");

        folderService.shareFolder(newFolder.getId(), Arrays.asList(adminUser), Access.Permission.WRITE);

        setAuthentication(adminUser);

        Folder sharedItemsFolder = folderService.getSharedItemsFolder(adminUser.getId());
        Assert.assertNotNull(sharedItemsFolder);
        sharedItemsFolder = folderService.loadSubFolderList(sharedItemsFolder.getId());
        Assert.assertTrue(sharedItemsFolder.getFolderList().size() >= 1);

        // TODO check access rights
    }

    @Test
    public void testFolderAndSubFolderSharing() {
        // create folder
        Folder beforeLoadSubFolderListNewFolder = folderService.createFolder("New Folder");


        // create sub folder
        Folder newSubFolder = folderService.createFolder("New Subfolder", beforeLoadSubFolderListNewFolder.getId());
        Folder newFolder = folderService.loadSubFolderList(beforeLoadSubFolderListNewFolder.getId());
        Assert.assertEquals(beforeLoadSubFolderListNewFolder, newFolder);
        Assert.assertTrue(newFolder.getFolderList().contains(newSubFolder));

        // share the parent folder
        User adminUser = userService.findByUsername("admin");
        newFolder = folderService.shareFolder(newFolder.getId(), Arrays.asList(adminUser), Access.Permission.WRITE);

        // check if the sharing process was successful
        Folder sharedItemsFolder = folderService.getSharedItemsFolder(adminUser.getId());
        Assert.assertNotNull(sharedItemsFolder);
        sharedItemsFolder = folderService.loadSubFolderList(sharedItemsFolder.getId());
        Assert.assertTrue(sharedItemsFolder.getFolderList().contains(newFolder));
        Folder sharedNewFolder = sharedItemsFolder.getFolderList().get(0);
        Assert.assertEquals(newFolder, sharedNewFolder);
        Access access = new Access();
        access.setUser(adminUser);
        sharedNewFolder = folderService.loadContentCollection(sharedNewFolder, Access.class);
        Assert.assertTrue(sharedNewFolder.getAccessList().contains(access));

        // check if subfolder has also the sharing access right
        sharedNewFolder = folderService.loadSubFolderList(sharedNewFolder.getId());
        Assert.assertTrue(sharedNewFolder.getFolderList().size() > 0);
        Folder sharedSubFolder = sharedNewFolder.getFolderList().get(0);
        sharedSubFolder = folderService.loadContentCollection(sharedSubFolder, Access.class);
        Assert.assertTrue(sharedSubFolder.getAccessList().contains(access));
    }
}
