package de.hska.ld.content.service;

import de.hska.ld.content.persistence.domain.UserContentInfo;

public interface UserContentInfoService {

    UserContentInfo addTag(Long id, Long tagId);
}
