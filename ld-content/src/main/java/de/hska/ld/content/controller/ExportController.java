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

package de.hska.ld.content.controller;

import de.hska.ld.content.client.EmailExportClient;
import de.hska.ld.content.client.PDFGenClient;
import de.hska.ld.content.dto.ExportPDFViaMailDto;
import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.content.service.DocumentService;
import de.hska.ld.core.util.Core;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Callable;

@RestController
@RequestMapping(Core.RESOURCE_API + "/export")
public class ExportController {

    @Autowired
    private PDFGenClient pdfGenClient;

    @Autowired
    private EmailExportClient emailExportClient;

    @Autowired
    private DocumentService documentService;

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST, value = "/pdf")
    public Callable createPDF(@RequestBody String htmlForPDFString) {
        return () -> {
            String base64PDF = pdfGenClient.createPDF(htmlForPDFString);
            return new ResponseEntity<>(base64PDF, HttpStatus.OK);
        };
    }

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST, value = "/pdf2mail")
    public Callable sendPDFToEmail(@RequestBody ExportPDFViaMailDto exportPDFViaMailDto) {
        return () -> {
            Document document = documentService.findById(exportPDFViaMailDto.getDocumentId());
            if (document != null) {
                exportPDFViaMailDto.setSenderEmail(Core.currentUser().getEmail());
                boolean worked = emailExportClient.sendMail(exportPDFViaMailDto);
                if (worked) {
                    return new ResponseEntity<>(HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        };
    }
}
