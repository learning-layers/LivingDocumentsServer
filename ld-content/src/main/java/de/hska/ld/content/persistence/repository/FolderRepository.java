package de.hska.ld.content.persistence.repository;

import de.hska.ld.content.persistence.domain.Folder;
import de.hska.ld.core.persistence.domain.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface FolderRepository extends CrudRepository<Folder, Long> {

    List<Folder> findByCreatorAndSharingFolderTrue(User user);
}
