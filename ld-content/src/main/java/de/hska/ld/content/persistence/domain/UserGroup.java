package de.hska.ld.content.persistence.domain;

import de.hska.ld.core.persistence.domain.User;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "ld_usergroup")
public class UserGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(name = "id")
    private Long id;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "ld_usergroup_user",
            joinColumns = {@JoinColumn(name = "usergroup_id")},
            inverseJoinColumns = {@JoinColumn(name = "user_id")})
    List<User> userList;
}
