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

package de.hska.ld.recommendation.service.impl;

import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.content.service.DocumentService;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.UserService;
import de.hska.ld.recommendation.dto.LDRecommendationDocumentDto;
import de.hska.ld.recommendation.dto.LDRecommendationUserDto;
import de.hska.ld.recommendation.persistence.domain.DocumentRecommInfo;
import de.hska.ld.recommendation.persistence.repository.DocumentRecommInfoRepository;
import de.hska.ld.recommendation.service.DocumentRecommInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DocumentRecommInfoServiceImpl implements DocumentRecommInfoService {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private UserService userService;

    @Autowired
    private DocumentRecommInfoRepository repository;

    @Override
    public DocumentRecommInfo findByDocument(Document document) {
        return repository.findByDocument(document);
    }

    @Transactional
    public void addDocumentRecommInfo(Long documentId) {
        Document document = documentService.findById(documentId);
        DocumentRecommInfo documentRecommInfo = new DocumentRecommInfo();
        documentRecommInfo.setDocument(document);
        documentRecommInfo.setInitialImportToSSSDone(true);
        repository.save(documentRecommInfo);
    }

    @Override
    public List<LDRecommendationUserDto> fetchUserRecommendationDatasets(List<LDRecommendationUserDto> userIdList) {
        int count = 10;
        for (LDRecommendationUserDto userId : userIdList) {
            if (count > 0) {
                User user = userService.findById(userId.getUserId());
                userId.setUser(user);
                count--;
            }
        }
        return userIdList;
    }

    @Override
    public List<LDRecommendationDocumentDto> fetchDocumentRecommendationDatasets(List<LDRecommendationDocumentDto> documentIdList) {
        int count = 10;
        for (LDRecommendationDocumentDto documentId : documentIdList) {
            if (count > 0) {
                Document document = documentService.findById(documentId.getDocumentId());
                documentId.setDocument(document);
                count--;
            }
        }
        return documentIdList;
    }

    public DocumentRecommInfoRepository getRepository() {
        return repository;
    }
}
