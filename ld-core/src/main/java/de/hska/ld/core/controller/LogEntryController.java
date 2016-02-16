/*
 *  Code contributed to the Learning Layers project
 *  http://www.learning-layers.eu
 *  Development is partly funded by the FP7 Programme of the European
 *  Commission under Grant Agreement FP7-ICT-318209.
 *  Copyright (c) 2016, Karlsruhe University of Applied Sciences.
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

package de.hska.ld.core.controller;

import de.hska.ld.core.service.LogEntryService;
import de.hska.ld.core.util.Core;
import org.pmw.tinylog.Logger;
import org.pmw.tinylog.LoggingContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.Callable;

@RestController
@RequestMapping(Core.RESOURCE_LOG)
public class LogEntryController {

    @Autowired
    private LogEntryService logEntryService;

    @Secured(Core.ROLE_ADMIN)
    @RequestMapping(method = RequestMethod.GET)
    @Transactional(readOnly = true)
    public Callable getLogEntryPage(@RequestParam(value = "page-number", defaultValue = "0") Integer pageNumber,
                                    @RequestParam(value = "page-size", defaultValue = "10") Integer pageSize,
                                    @RequestParam(value = "sort-direction", defaultValue = "DESC") Sort.Direction sortDirection,
                                    @RequestParam(value = "sort-property", defaultValue = "date") String sortProperty) {
        return () -> new ResponseEntity<>(logEntryService.findLogEntryPage(pageNumber, pageSize, sortDirection, sortProperty), HttpStatus.OK);
    }

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST)
    @Transactional
    public Callable addClientLogEntry(List<String> values) {
        return () -> {
            try {
                LoggingContext.put("user_email", Core.currentUser().getEmail());
                values.forEach(v -> {
                    String[] splittedValue = v.split("###");
                    String key = splittedValue[0];
                    String value = splittedValue[1];
                    LoggingContext.put(key, value);
                });
                Logger.trace("Client-side logging event.");
                return new ResponseEntity<>(HttpStatus.OK);
            } catch (Exception e) {
                Logger.error(e);
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            } finally {
                LoggingContext.clear();
            }
        };
    }
}
