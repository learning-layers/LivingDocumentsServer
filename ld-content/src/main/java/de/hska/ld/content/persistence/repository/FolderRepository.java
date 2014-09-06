package de.hska.ld.content.persistence.repository;

import de.hska.ld.content.persistence.domain.Folder;
import de.hska.ld.core.persistence.domain.User;
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
}
