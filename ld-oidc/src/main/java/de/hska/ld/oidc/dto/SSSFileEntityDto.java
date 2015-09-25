package de.hska.ld.oidc.dto;

public class SSSFileEntityDto extends SSSEntityDto {
    SSSFileDto file;
    String note;

    public SSSFileDto getFile() {
        return file;
    }

    public void setFile(SSSFileDto file) {
        this.file = file;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
