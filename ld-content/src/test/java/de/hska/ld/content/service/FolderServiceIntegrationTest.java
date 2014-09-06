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
import java.util.List;

import static de.hska.ld.content.ContentFixture.newDocument;
import static de.hska.ld.core.fixture.CoreFixture.newUser;

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
        Assert.assertNotNull(subFolder.getParent());

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
        folder = folderService.moveDocumentToFolder(-1L, folder.getId(), document.getId());
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
        Folder revokedNewFolder = folderService.revokeShareFolder(newFolder.getId(), Arrays.asList(adminUser), Access.Permission.WRITE);

        // check if the revoke sharing process was successful
        // 1. get the shared items folder of the adminUser
        Folder sharedItemsFolderAfterRevoke = folderService.getSharedItemsFolder(adminUser.getId());
        // 2. load the sub folders list of the shared items folder
        sharedItemsFolderAfterRevoke = folderService.loadSubFolderList(beforeLoadSubFolderListSharedItemsFolder.getId());
        // 3. Assert that the newFolder is in the shared items folder (so it is shared)
        Assert.assertTrue(!sharedItemsFolderAfterRevoke.getFolderList().contains(newFolder));
        Assert.assertTrue(!newFolder.getParent().equals(sharedItemsFolderAfterRevoke));
    }

    @Test
    @Transactional
    public void testThatDocumentSharedWhenTheirFolderHasBeenShared() {
        Folder folder = folderService.createFolder("SharedFolder");
        Folder subFolder = folderService.createFolder("SharedFolder SubFolder", folder.getId());
        Document document = documentService.save(newDocument());
        Document documentInSubFolder = documentService.save(newDocument());

        // create a test folder structure
        folderService.moveDocumentToFolder(-1L, folder.getId(), document.getId());
        folderService.moveDocumentToFolder(-1L, subFolder.getId(), documentInSubFolder.getId());

        // The folder structure will be shared with this user
        User testUser2 = userService.save(newUser());
        folder = folderService.shareFolder(folder.getId(), Arrays.asList(testUser2), Access.Permission.READ);

        // assert that the document within the folder is shared
        Assert.assertTrue(folder.getDocumentList().size() == 1);
        Assert.assertEquals(testUser2, folder.getDocumentList().get(0).getAccessList().get(0).getUser());

        // assert that the document within the sub folder is shared
        folder = folderService.loadSubFolderList(folder.getId());
        Assert.assertTrue(folder.getFolderList().size() == 1);
        Assert.assertEquals(testUser2, folder.getFolderList().get(0).getDocumentList().get(0).getAccessList().get(0).getUser());
    }

    @Test
    public void testFindFoldersByChildFolderId() {
        // Create a simple folder structure
        Folder folder = folderService.createFolder("Folder");
        Folder subfolder = folderService.createFolder("Subfolder", folder.getId());
        folder = folderService.loadSubFolderList(folder.getId());
        Assert.assertTrue(folder.getFolderList().contains(subfolder));

        List<Folder> folderList = folderService.findFoldersByChildFolderId(subfolder.getId());

        Assert.assertNotNull(folderList);
        Assert.assertTrue(folderList.size() == 1);
        Assert.assertEquals(folder, folderList.get(0));
    }

    @Test
    public void testFindFoldersByChildFolderIdAndCreatorId() {
        // Create a simple folder structure
        Folder folder = folderService.createFolder("Folder");
        Folder subfolder = folderService.createFolder("Subfolder", folder.getId());
        folder = folderService.loadSubFolderList(folder.getId());
        Assert.assertTrue(folder.getFolderList().contains(subfolder));

        List<Folder> folderList = folderService.findFoldersByParentCreatorId(subfolder.getId(), testUser.getId());

        Assert.assertNotNull(folderList);
        Assert.assertTrue(folderList.size() == 1);
        Assert.assertEquals(folder, folderList.get(0));
    }

    @Test
    public void testRootFolderRetrieval() {
        Folder rootFolder1 = folderService.createFolder("rootFolder1");
        Folder rootFolder2 = folderService.createFolder("rootFolder2");

        List<Folder> rootFolderList = folderService.getFoldersByUser(testUser);
        Assert.assertNotNull(rootFolderList);
        Assert.assertTrue(rootFolderList.size() >= 2);
        Assert.assertTrue(rootFolderList.contains(rootFolder1));
        Assert.assertTrue(rootFolderList.contains(rootFolder2));
    }

    @Test
    @Transactional
    public void testFolderMovingAccessPropagation() {
        // 1. Create parent folder

        // 2.a) Create folder as sub folder of  the parent folder

        // 2.b) Create a document and put it in the folder

        // 3.a) Create a sub folder of folder

        // 3.b) Create a document and add it to the sub folder


        // ======== Sharing ======= //
        // 4. Share the folder with an other user

        // 5. Assert that the sharing has been successful

        // ======== Move folder ======= //
        // 6.a) Move the folder as the "other" user

        // 6.b) Assert that the access permissions stay the same

        // 7.a) Move the folder as creator

        // 7.b) Assert that the access propagation works

    }
}
