package de.hska.ld.content.dto;

import de.hska.ld.content.persistence.domain.Document;

public class DiscussionSectionDto {

    private Document document;

    private String sectionText;

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public String getSectionText() {
        return sectionText;
    }

    public void setSectionText(String sectionText) {
        this.sectionText = sectionText;
    }
}
