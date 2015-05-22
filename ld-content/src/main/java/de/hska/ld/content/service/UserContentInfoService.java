package de.hska.ld.content.service;

import de.hska.ld.content.persistence.domain.Tag;
import de.hska.ld.content.persistence.domain.UserContentInfo;
import org.springframework.data.domain.Page;

public interface UserContentInfoService {

    UserContentInfo addTag(Long id, Long tagId);

    void removeTag(Long userId, Long tagId);

    Page<Tag> getUserContentTagsPage(Long userId, Integer pageNumber, Integer pageSize, String sortDirection, String sortProperty);
}
