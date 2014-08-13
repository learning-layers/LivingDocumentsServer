package de.hska.ld.content.persistence.repository;

import de.hska.ld.content.persistence.domain.Attachment;
import org.springframework.data.repository.CrudRepository;

public interface AttachmentRepository extends CrudRepository<Attachment, Long> {
}
