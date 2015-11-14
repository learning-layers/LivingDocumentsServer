package de.hska.ld.core.logging;

import de.hska.ld.core.persistence.domain.ExceptionLogEntry;
import de.hska.ld.core.util.Core;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.UUID;

@Transactional
@Component
public class ExceptionLoggerImpl implements ExceptionLogger {

    @Autowired
    private EntityManager entityManager;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UUID log(Throwable ex) {
        ExceptionLogEntry entry = new ExceptionLogEntry();
        entry.setUser(Core.currentUser());
        entry.setDescription(ex.getMessage());

        entry.setType("Exception");

        entityManager.persist(entry);

        return entry.getId();
    }

}
