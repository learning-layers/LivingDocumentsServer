package de.hska.ld.oidc.dto;

public class SSSFileDto extends SSSEntityDto {
    private String fileExt;
    private String mimeType;
    private String downloadLink;
    private String fileIcon;

    public String getFileExt() {
        return fileExt;
    }

    public void setFileExt(String fileExt) {
        this.fileExt = fileExt;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getDownloadLink() {
        return downloadLink;
    }

    public void setDownloadLink(String downloadLink) {
        this.downloadLink = downloadLink;
    }

    public void setFileIcon(String fileIcon) {
        this.fileIcon = fileIcon;
    }

    public String getFileIcon() {
        return fileIcon;
    }
}
