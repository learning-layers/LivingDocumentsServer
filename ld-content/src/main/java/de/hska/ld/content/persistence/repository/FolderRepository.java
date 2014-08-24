package de.hska.ld.content.persistence.repository;

import de.hska.ld.content.persistence.domain.Folder;
import org.springframework.data.repository.CrudRepository;

public interface FolderRepository extends CrudRepository<Folder, Long> {
}
