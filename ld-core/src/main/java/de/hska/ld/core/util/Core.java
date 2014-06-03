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

package de.hska.ld.core.util;

import javax.jcr.Credentials;
import javax.jcr.SimpleCredentials;

public class Core {

    public static final Credentials ADMIN_CREDENTIALS = new SimpleCredentials("admin", "admin".toCharArray());

    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_USER = "ROLE_USER";

    public static final String LD_DOCUMENT = "ld:document";
    public static final String LD_DOCUMENTS = "ld:documents";

    public static final String LD_FILE_NODE = "file";
    public static final String LD_COMMENTS_NODE = "comments";
    public static final String LD_TAGS_NODE = "tags";
    public static final String LD_ATTACHMENTS_NODE = "attachments";
    public static final String LD_DESCRIPTION_PROPERTY = "description";

    public static final String LD_MESSAGE_PROPERTY = "message";
    public static final String LD_NAME_PROPERTY = "name";

    public static final String JCR_LASTMODIFIED_BY = "jcr:lastModifiedBy";
}
