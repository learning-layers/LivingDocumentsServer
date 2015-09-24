package de.hska.ld.content.persistence.repository;

import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.content.persistence.domain.Folder;
import de.hska.ld.core.persistence.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FolderRepository extends CrudRepository<Folder, Long> {

    List<Folder> findByCreatorAndSharingFolderTrue(User creator);

    @Query("SELECT f FROM Folder f INNER JOIN f.folderList c WHERE c.id = :childFolderId")
    public List<Folder> findFoldersByChildFolderId(@Param("childFolderId") Long childFolderId);

    @Query("SELECT f FROM Folder f INNER JOIN f.folderList c WHERE c.id = :childFolderId AND f.creator.id = :creatorId")
    public List<Folder> findFoldersByChildFolderIdAndCreatorId(@Param("childFolderId") Long childFolderId, @Param("creatorId") Long creatorId);

    @Query("SELECT f FROM Folder f WHERE f.creator.id = :userId AND f.parent IS null")
    List<Folder> findCreatorRootFolders(@Param("userId") Long userId);

    @Query("SELECT f FROM Folder f WHERE f.parent.id = :folderId")
    List<Folder> getSubFoldersByFolderId(@Param("folderId") Long folderId);

    @Query("SELECT dl FROM Folder f INNER JOIN f.documentList dl WHERE f.id = :folderId")
    Page<Document> getSubDocumentsPage(@Param("folderId") Long folderId, Pageable pageable);

    @Query("SELECT f FROM Folder f WHERE f.parent is null AND f.creator.id = :userId")
    Page<Folder> getFoldersPage(@Param("userId") Long userId, Pageable pageable);
}
