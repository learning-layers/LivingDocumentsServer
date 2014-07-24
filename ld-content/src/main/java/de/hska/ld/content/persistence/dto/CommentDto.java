package de.hska.ld.content.persistence.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.hska.ld.content.persistence.domain.Comment;

import java.lang.reflect.Field;

public class CommentDto extends Comment {

    private Long jsonParentId;

    @JsonProperty("jsonParentId")
    public Long getJsonParentId() {
        if (this.getParent() != null) {
            jsonParentId = this.getParent().getId();
        }
        return jsonParentId;
    }

    @JsonProperty("jsonParentId")
    public void setJsonParentId(Long id) {
        if (this.getParent() != null) {
            jsonParentId = this.getParent().getId();
        } else {
            jsonParentId = id;
        }
    }

    public CommentDto() {
    }

    public CommentDto(Comment comment) {
        try {
            // extract and set values per reflection
            for (Field commentfield : comment.getClass().getDeclaredFields()) {
                commentfield.setAccessible(true);
                Object obj = commentfield.get(comment);
                commentfield.set(this, obj);
            }
        } catch (IllegalAccessException e) {
            //
        }
        this.getJsonParentId();
    }
}
