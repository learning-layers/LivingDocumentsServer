package de.hska.ld.content.persistence.repository.custom;

import de.hska.ld.content.persistence.domain.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TagRepositoryCustom {

    Page<Tag> searchTagByNameOrDescription(String searchTerm, Pageable pageable);
}
