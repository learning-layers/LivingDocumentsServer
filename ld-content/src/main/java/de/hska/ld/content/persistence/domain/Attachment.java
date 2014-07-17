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

package de.hska.ld.content.persistence.domain;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

@Entity
@Table(name = "ld_attachment")
public class Attachment extends Content {
    private static final Logger LOGGER = LoggerFactory.getLogger(Attachment.class);

    public Attachment() {
    }

    public Attachment(InputStream inputStream, String name) {
        try {
            this.mimeType = URLConnection.guessContentTypeFromStream(inputStream);
            this.source = IOUtils.toByteArray(inputStream);
            this.name = name;
        } catch (IOException e) {
            LOGGER.warn("Unable to set source or mime type from input stream.", e);
        }
    }

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "mimeType")
    private String mimeType;

    @Lob
    @Column(name = "source")
    private byte[] source;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public byte[] getSource() {
        return source;
    }

    public void setSource(byte[] source) {
        this.source = source;
    }
}