package de.hska.ld.content.service.impl;

import de.hska.ld.content.persistence.domain.Tag;
import de.hska.ld.content.persistence.repository.DocumentRepository;
import de.hska.ld.content.persistence.repository.TagRepository;
import de.hska.ld.content.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;

public class TagServiceImpl  extends AbstractContentService<Tag> implements TagService {

    @Autowired
    private TagRepository repository;

    @Override
    public Tag createTag(String name, String description) {
        Tag tag = new Tag();
        tag.setName(name);
        tag.setDescription(description);
        return super.save(tag);
    }

    @Override
    public TagRepository getRepository() {
        return repository;
    }
}
