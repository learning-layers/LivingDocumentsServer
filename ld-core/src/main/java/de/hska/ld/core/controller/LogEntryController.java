package de.hska.ld.core.controller;

import de.hska.ld.core.persistence.domain.LogEntry;
import de.hska.ld.core.service.LogEntryService;
import de.hska.ld.core.util.Core;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(Core.RESOURCE_LOG)
public class LogEntryController {

    @Autowired
    private LogEntryService logEntryService;

    @Secured(Core.ROLE_ADMIN)
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<LogEntry>> getInfo() {
        return new ResponseEntity<>(logEntryService.findAll(), HttpStatus.OK);
    }
}
