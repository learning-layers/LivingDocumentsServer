/**
 * Code contributed to the Learning Layers project
 * http://www.learning-layers.eu
 * Development is partly funded by the FP7 Programme of the European
 * Commission under Grant Agreement FP7-ICT-318209.
 * Copyright (c) 2014, Karlsruhe University of Applied Sciences.
 * For a list of contributors see the AUTHORS file at the top-level directory
 * of this distribution.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.hska.livingdocuments.core.service;

import de.hska.livingdocuments.core.persistence.domain.User;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.InputStream;
import java.util.List;

public interface JcrService {

    <T extends Session> T login(User user) throws RepositoryException;

    Node createDocumentNode(Session session, String nodeId) throws RepositoryException;

    void addAllPrivileges(Session session, Node node) throws RepositoryException;

    void addAllPrivileges(Session session, String path) throws RepositoryException;

    Node addComment(Session session, Node documentNode, String comment) throws RepositoryException;

    Node updateComment(Session session, Node commentNode, String comment) throws RepositoryException;

    List<Node> getComments(Session session) throws RepositoryException;

    Node addFileNode(Session session, Node documentNode, InputStream inputStream) throws RepositoryException;
}
