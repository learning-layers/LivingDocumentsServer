package de.hska.ld.content.service.impl;

import de.hska.ld.content.persistence.domain.Attachment;
import de.hska.ld.content.persistence.repository.AttachmentRepository;
import de.hska.ld.content.service.AttachmentService;
import org.springframework.beans.factory.annotation.Autowired;

public class AttachmentServiceImpl extends AbstractContentService<Attachment> implements AttachmentService {

    @Autowired
    private AttachmentRepository repository;

    @Override
    public AttachmentRepository getRepository() {
        return repository;
    }
}
