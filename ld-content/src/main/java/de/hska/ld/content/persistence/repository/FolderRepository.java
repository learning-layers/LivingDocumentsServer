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
}
