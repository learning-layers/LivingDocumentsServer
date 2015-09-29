package de.hska.ld.oidc.dto;

public class DiscussionListDto {
    SSSFileEntitiesDto fileEnities;
    SSSDiscsDto discussions;

    public SSSFileEntitiesDto getFileEnities() {
        return fileEnities;
    }

    public void setFileEnities(SSSFileEntitiesDto fileEnities) {
        this.fileEnities = fileEnities;
    }

    public SSSDiscsDto getDiscussions() {
        return discussions;
    }

    public void setDiscussions(SSSDiscsDto discussions) {
        this.discussions = discussions;
    }
}
