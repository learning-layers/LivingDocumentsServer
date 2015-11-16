/*
 *  Code contributed to the Learning Layers project
 *  http://www.learning-layers.eu
 *  Development is partly funded by the FP7 Programme of the European
 *  Commission under Grant Agreement FP7-ICT-318209.
 *  Copyright (c) 2015, Karlsruhe University of Applied Sciences.
 *  For a list of contributors see the AUTHORS file at the top-level directory
 *  of this distribution.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UUID log(Throwable ex) {
        ExceptionLogEntry entry = new ExceptionLogEntry();
        entry.setUser(Core.currentUser());
        entry.setDescription(ex.getMessage());

        entry.setType("Exception");

        entityManager.persist(entry);

        return entry.getId();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UUID log(String action, Throwable ex) {
        ExceptionLogEntry entry = new ExceptionLogEntry();
        entry.setUser(Core.currentUser());
        entry.setDescription(ex.getMessage());
        entry.setAction(action);

        entry.setType("Exception");

        entityManager.persist(entry);

        return entry.getId();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UUID log(String action, String message) {
        ExceptionLogEntry entry = new ExceptionLogEntry();
        entry.setUser(Core.currentUser());
        entry.setDescription(message);
        entry.setAction(action);

        entry.setType("Exception");

        entityManager.persist(entry);

        return entry.getId();
    }

}
