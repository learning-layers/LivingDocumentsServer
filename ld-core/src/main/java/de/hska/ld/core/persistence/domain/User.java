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

package de.hska.ld.core.persistence.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "ld_user", indexes = @Index(name = "ld_user_username_idx", columnList = "username"), uniqueConstraints=
@UniqueConstraint(columnNames = {"subId", "issuer"}))
public class User implements UserDetails {

    public User() {
    }

    public User(String username, String fullName, List<Role> roleList) {
        this.username = username;
        this.fullName = fullName;
        this.roleList = roleList;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @NotBlank
    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Email
    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "subId", nullable = true)
    private String subId;

    @Column(name = "issuer", nullable = true)
    private String issuer;

    @Email
    private String emailToBeConfirmed;

    @Column(name = "password", nullable = false)
    private String password;

    @NotBlank
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "description", nullable = true, columnDefinition = "TEXT")
    private String description;

    @Column(name = "registration_confirmation_key")
    private String registrationConfirmationKey;

    @Column(name = "forgot_password_confirmation_key")
    private String forgotPasswordConfirmationKey;

    @Column(name = "change_email_confirmation_key")
    private String changeEmailConfirmationKey;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    @Column(name = "enabled")
    private boolean enabled;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "avatar")
    @JsonIgnore
    private byte[] avatar;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "ld_user_role",
            joinColumns = {@JoinColumn(name = "user_id")},
            inverseJoinColumns = {@JoinColumn(name = "role_id")})
    private Collection<Role> roleList;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmailToBeConfirmed() {
        return emailToBeConfirmed;
    }

    public void setEmailToBeConfirmed(String emailToBeConfirmed) {
        this.emailToBeConfirmed = emailToBeConfirmed;
    }

    @Override
    @JsonIgnore
    @JsonProperty
    public String getPassword() {
        return password;
    }

    @JsonProperty
    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @JsonIgnore
    public String getRegistrationConfirmationKey() {
        return registrationConfirmationKey;
    }

    public void setRegistrationConfirmationKey(String registrationConfirmationKey) {
        this.registrationConfirmationKey = registrationConfirmationKey;
    }

    @JsonIgnore
    public String getForgotPasswordConfirmationKey() {
        return forgotPasswordConfirmationKey;
    }

    public void setForgotPasswordConfirmationKey(String forgotPasswordConfirmationKey) {
        this.forgotPasswordConfirmationKey = forgotPasswordConfirmationKey;
    }

    @JsonIgnore
    public String getChangeEmailConfirmationKey() {
        return changeEmailConfirmationKey;
    }

    public void setChangeEmailConfirmationKey(String changeEmailConfirmationKey) {
        this.changeEmailConfirmationKey = changeEmailConfirmationKey;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Collection<Role> getRoleList() {
        if (roleList == null) {
            roleList = new ArrayList<>();
        }
        return roleList;
    }

    public void setRoleList(Collection<Role> role) {
        roleList = role;
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roleList;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public byte[] getAvatar() {
        return avatar;
    }

    public void setAvatar(byte[] avatar) {
        this.avatar = avatar;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return username.equals(user.username);
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }

    @Override
    public String toString() {
        return "User [id=" + id + ", username=" + username + ", fullName=" + fullName + ", roleList=" + roleList + "]";
    }
}
