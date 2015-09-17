package de.hska.ld.content.persistence.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.hska.ld.core.persistence.domain.User;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ld_user_content_info")
public class UserContentInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "ld_user_content_tag",
            joinColumns = {@JoinColumn(name = "user_content_id")},
            inverseJoinColumns = {@JoinColumn(name = "tag_id")})
    private List<Tag> tagList;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @JsonProperty("tags")
    public List<Tag> getTagList() {
        if (tagList == null) {
            tagList = new ArrayList<>();
        }
        return tagList;
    }

    public void setTagList(List<Tag> tagList) {
        this.tagList = tagList;
    }
}
