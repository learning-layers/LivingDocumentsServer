package de.hska.ld.content.service.impl;

import de.hska.ld.content.persistence.domain.Tag;
import de.hska.ld.content.persistence.repository.TagRepository;
import de.hska.ld.content.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class TagServiceImpl extends AbstractContentService<Tag> implements TagService {

    @Autowired
    private TagRepository repository;

    @Override
    public Page<Tag> getTagsPage(Integer pageNumber, Integer pageSize, String sortDirection, String sortProperty) {
        Sort.Direction direction;
        if (Sort.Direction.ASC.toString().equals(sortDirection)) {
            direction = Sort.Direction.ASC;
        } else {
            direction = Sort.Direction.DESC;
        }
        Pageable pageable = new PageRequest(pageNumber, pageSize, direction, sortProperty);
        return repository.findAll(pageable);
    }

    @Override
    public Tag updateTag(Long tagId, Tag tag) {
        Tag dbTag = super.findById(tagId);
        dbTag.setName(tag.getName());
        dbTag.setDescription(tag.getDescription());
        return super.save(tag);
    }

    @Override
    public TagRepository getRepository() {
        return repository;
    }
}
