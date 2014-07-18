package de.hska.ld.content.persistence.repository;

import de.hska.ld.content.persistence.domain.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface TagRepository extends CrudRepository<Tag, Long> {

    Page<Tag> findAll(Pageable pageable);
}
