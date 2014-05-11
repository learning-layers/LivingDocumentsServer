package de.hska.livingdocuments.core.dto;

import java.io.InputStream;

public class NodeDto {

    private String description;
    private InputStream inputStream;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }
}
