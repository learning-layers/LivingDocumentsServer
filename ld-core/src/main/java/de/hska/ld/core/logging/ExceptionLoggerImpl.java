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
import de.hska.ld.core.persistence.domain.User;
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
    public UUID log(String action, Throwable ex) {
        return log(action, ex, "");
    }

    @Override
    public UUID log(String action, Throwable ex, String reason) {
        String stackTraceAsString = org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(ex);
        ExceptionLogEntry entry = createLogEntry(action, ex.getMessage(), stackTraceAsString, ExceptionLogEntry.LogLevel.DEBUG, reason);
        return save(entry);
    }

    @Override
    public UUID log(String action, Throwable ex, ExceptionLogEntry.LogLevel logLevel) {
        return log(action, ex, logLevel, "");
    }

    @Override
    public UUID log(String action, Throwable ex, ExceptionLogEntry.LogLevel logLevel, String reason) {
        String stackTraceAsString = org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(ex);
        ExceptionLogEntry entry = createLogEntry(action, ex.getMessage(), stackTraceAsString, logLevel, reason);
        return save(entry);
    }

    @Override
    public UUID log(String action, String message) {
        return log(action, message, "");
    }

    @Override
    public UUID log(String action, String message, String reason) {
        ExceptionLogEntry entry = createLogEntry(action, message, ExceptionLogEntry.LogLevel.DEBUG, reason);
        return save(entry);
    }

    @Override
    public UUID log(String action, String message, ExceptionLogEntry.LogLevel logLevel) {
        return log(action, message, logLevel, "");
    }

    @Override
    public UUID log(String action, String message, ExceptionLogEntry.LogLevel logLevel, String reason) {
        ExceptionLogEntry entry = createLogEntry(action, message, logLevel, reason);
        return save(entry);
    }

    private ExceptionLogEntry createLogEntry(String action, String message, ExceptionLogEntry.LogLevel logLevel) {
        return createLogEntry(action, message, logLevel, null);
    }

    private ExceptionLogEntry createLogEntry(String action, String message, ExceptionLogEntry.LogLevel logLevel, String reason) {
        return createLogEntry(action, message, null, logLevel, reason);
    }

    private ExceptionLogEntry createLogEntry(String action, String message, String stackTraceAsString, ExceptionLogEntry.LogLevel logLevel) {
        return createLogEntry(action, message, null, logLevel, null);
    }

    private ExceptionLogEntry createLogEntry(String action, String message, String stackTraceAsString, ExceptionLogEntry.LogLevel logLevel, String reason) {
        ExceptionLogEntry entry = new ExceptionLogEntry();
        if (stackTraceAsString != null) {
            entry.setStackTraceAsString(stackTraceAsString);
        }
        if (!"".equals(reason)) {
            entry.setReason(reason);
        }
        User user = null;
        try {
            user = Core.currentUser();
        } catch (Exception e) {
            //
        }
        entry.setLogLevel(logLevel);
        entry.setUser(user);
        entry.setDescription(message);
        entry.setAction(action);
        entry.setType("Exception");
        return entry;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private UUID save(ExceptionLogEntry entry) {
        entityManager.persist(entry);
        return entry.getId();
    }

}
