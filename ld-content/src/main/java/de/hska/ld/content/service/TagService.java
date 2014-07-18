package de.hska.ld.content.service;

import de.hska.ld.content.persistence.domain.Tag;

public interface TagService {

    Tag createTag(String name, String description);
}
