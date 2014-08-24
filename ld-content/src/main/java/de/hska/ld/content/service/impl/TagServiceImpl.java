/**
 * Code contributed to the Learning Layers project
 * http://www.learning-layers.eu
 * Development is partly funded by the FP7 Programme of the European
 * Commission under Grant Agreement FP7-ICT-318209.
 * Copyright (c) 2014, Karlsruhe University of Applied Sciences.
 * For a list of contributors see the AUTHORS file at the top-level directory
 * of this distribution.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.hska.ld.content.service.impl;

import de.hska.ld.content.persistence.domain.Tag;
import de.hska.ld.content.persistence.repository.TagRepository;
import de.hska.ld.content.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
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
    public Tag findByName(String tagName) {
        return repository.findByName(tagName);
    }

    @Override
    public TagRepository getRepository() {
        return repository;
    }
}
