package de.hska.ld.content.service;

import de.hska.ld.content.persistence.domain.Tag;
import de.hska.ld.core.service.Service;
import org.springframework.data.domain.Page;

public interface TagService extends Service<Tag> {

    Page<Tag> getTagsPage(Integer pageNumber, Integer pageSize, String sortDirection, String sortProperty);
}
