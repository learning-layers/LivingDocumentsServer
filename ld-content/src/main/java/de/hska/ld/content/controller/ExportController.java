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

import de.hska.ld.content.client.PDFGenClient;
import de.hska.ld.content.dto.HTMLForPDFGenDto;
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

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST, value = "/pdf")
    public Callable createPDF(@RequestBody HTMLForPDFGenDto htmlForPDFGenDto) {
        return () -> {
            // call the pdfGen server with the according HTMLForPDFGenWithApiKeyDto instance
            // receive back the generated base64 PDF
            String jsonHtml = htmlForPDFGenDto.getJsonHtml();
            String base64PDF = pdfGenClient.createPDF(jsonHtml);
            return new ResponseEntity<>(base64PDF, HttpStatus.OK);
        };
    }
}
