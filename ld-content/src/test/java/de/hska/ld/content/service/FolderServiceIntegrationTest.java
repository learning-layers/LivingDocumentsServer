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
        // create a folder
        Folder folder = folderService.createFolder("Folder");
        Assert.assertNotNull(folder);

        // create a subfolder of the folder previously created
        Folder subFolder = folderService.createFolder("Subfolder", folder.getId());
        Assert.assertNotNull(subFolder);
        Assert.assertNotNull(subFolder.getParentFolderList());
        Assert.assertTrue(subFolder.getParentFolderList().size() > 0);
        Assert.assertNotNull(subFolder.getParentFolderList().get(0).getId());

        // load the folder
        folder = folderService.findById(folder.getId());
        // load the subfolder list
        folder = folderService.loadSubFolderList(folder.getId());

        // assert that putting the subfolder into the folder worked
        Assert.assertNotNull(folder.getFolderList());
        Assert.assertTrue(folder.getFolderList().size() == 1);
        Assert.assertTrue(folder.getFolderList().get(0).getId().equals(subFolder.getId()));
    }

    @Test
    @Transactional
    public void testCreateFolderAndPutDocumentInIt() {
        // create a new folder
        Folder folder = folderService.createFolder("Folder");
        // create a new document
        Document document = documentService.save(newDocument());
        // put the document in the folder
        folder = folderService.placeDocumentInFolder(folder.getId(), document.getId());
        // assert that the process has been successful
        Assert.assertNotNull(folder);
        Assert.assertTrue(folder.getDocumentList().size() == 1);
        Assert.assertTrue(folder.getDocumentList().get(0).getId().equals(document.getId()));
    }

    @Test
    public void testFolderSharing() {
        // create a new folder as testUser
        Folder newFolder = folderService.createFolder("New Folder");
        // share the folder as testUser to user adminUser
        User adminUser = userService.findByUsername("admin");
        folderService.shareFolder(newFolder.getId(), Arrays.asList(adminUser), Access.Permission.WRITE);

        // get the sharedItems folder of the admin user
        Folder sharedItemsFolder = folderService.getSharedItemsFolder(adminUser.getId());
        Assert.assertNotNull(sharedItemsFolder);
        // load the sharedFolder collection
        sharedItemsFolder = folderService.loadSubFolderList(sharedItemsFolder.getId());
        // assert that the shared folder is within the shared folders list.
        Assert.assertTrue(sharedItemsFolder.getFolderList().size() >= 1);
    }

    @Test
    public void testFolderAndSubFolderSharingAdd() {
        // create folder
        Folder beforeLoadSubFolderListNewFolder = folderService.createFolder("New Folder");

        // create sub folder
        Folder newSubFolder = folderService.createFolder("New Subfolder", beforeLoadSubFolderListNewFolder.getId());
        Folder newFolder = folderService.loadSubFolderList(beforeLoadSubFolderListNewFolder.getId());
        Assert.assertEquals(beforeLoadSubFolderListNewFolder, newFolder);
        Assert.assertTrue(newFolder.getFolderList().contains(newSubFolder));

        // share the parent folder
        User adminUser = userService.findByUsername("admin");
        Folder newFolderAfterSharing = folderService.shareFolder(newFolder.getId(), Arrays.asList(adminUser), Access.Permission.WRITE);
        Assert.assertEquals(newFolderAfterSharing, newFolder);

        // check if the sharing process was successful
        // 1. get the shared items folder of the adminUser
        Folder beforeLoadSubFolderListSharedItemsFolder = folderService.getSharedItemsFolder(adminUser.getId());
        Assert.assertNotNull(beforeLoadSubFolderListSharedItemsFolder);
        // 2. load the sub folders list of the shared items folder
        Folder sharedItemsFolder = folderService.loadSubFolderList(beforeLoadSubFolderListSharedItemsFolder.getId());
        Assert.assertEquals(sharedItemsFolder, beforeLoadSubFolderListSharedItemsFolder);
        // 3. Assert that the newFolder is in the shared items folder (so it is shared)
        Assert.assertTrue(sharedItemsFolder.getFolderList().contains(newFolder));
    }

    @Test
    public void testFolderAndSubFolderSharingRemove() {
        // == testFolderAndSubFolderSharingAdd ==
        // create folder
        Folder beforeLoadSubFolderListNewFolder = folderService.createFolder("New Folder");

        // create sub folder
        Folder newSubFolder = folderService.createFolder("New Subfolder", beforeLoadSubFolderListNewFolder.getId());
        Folder newFolder = folderService.loadSubFolderList(beforeLoadSubFolderListNewFolder.getId());
        Assert.assertEquals(beforeLoadSubFolderListNewFolder, newFolder);
        Assert.assertTrue(newFolder.getFolderList().contains(newSubFolder));

        // share the parent folder
        User adminUser = userService.findByUsername("admin");
        Folder newFolderAfterSharing = folderService.shareFolder(newFolder.getId(), Arrays.asList(adminUser), Access.Permission.WRITE);
        Assert.assertEquals(newFolderAfterSharing, newFolder);

        // check if the sharing process was successful
        // 1. get the shared items folder of the adminUser
        Folder beforeLoadSubFolderListSharedItemsFolder = folderService.getSharedItemsFolder(adminUser.getId());
        Assert.assertNotNull(beforeLoadSubFolderListSharedItemsFolder);
        // 2. load the sub folders list of the shared items folder
        Folder sharedItemsFolder = folderService.loadSubFolderList(beforeLoadSubFolderListSharedItemsFolder.getId());
        Assert.assertEquals(sharedItemsFolder, beforeLoadSubFolderListSharedItemsFolder);
        // 3. Assert that the newFolder is in the shared items folder (so it is shared)
        Assert.assertTrue(sharedItemsFolder.getFolderList().contains(newFolder));

        // == testFolderAndSubFolderSharingRemove ==
        //folderService.revokeShareFolder(newFolder.getId(), Arrays.asList(adminUser), Access.Permission.WRITE);
    }
}
