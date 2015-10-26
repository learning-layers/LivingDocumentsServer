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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.sql.Blob;

@Entity
@Table(name = "ld_attachment")
public class Attachment extends Content {
    private static final Logger LOGGER = LoggerFactory.getLogger(Attachment.class);

    public Attachment() {
    }

    public Attachment(String name) {
        setNewValues(name);
    }

    public Attachment(InputStream inputStream, String name) {
        setNewValues(inputStream, name);
    }

    public void setNewValues(InputStream inputStream, String name) {
        try {
            this.mimeType = URLConnection.guessContentTypeFromName(name);
            this.source = IOUtils.toByteArray(inputStream);
            this.name = name;
        } catch (IOException e) {
            LOGGER.warn("Unable to set source or mime type from input stream.", e);
        }
    }

    public void setNewValues(String name) {
        this.mimeType = URLConnection.guessContentTypeFromName(name);
        this.name = name;
    }

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "mimeType")
    private String mimeType;

    // TODO add Source One-to-one mapping, fetch type lazy
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "source")
    @JsonIgnore
    private byte[] source;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "sourceBlob")
    @JsonIgnore
    private Blob sourceBlob;

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

    public Blob getSourceBlob() {
        return sourceBlob;
    }

    public void setSourceBlob(Blob sourceBlob) {
        this.sourceBlob = sourceBlob;
    }
}
