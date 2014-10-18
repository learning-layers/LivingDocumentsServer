package de.hska.ld.core.controller;

import de.hska.ld.core.service.LogEntryService;
import de.hska.ld.core.util.Core;
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
}
