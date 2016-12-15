package com.manywho.services.identity.users;

import com.google.common.collect.Lists;
import com.manywho.sdk.api.ContentType;
import com.manywho.sdk.services.types.Type;
import com.manywho.services.identity.groups.Group;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Type.Element(name = "User")
public class User implements Type {

    @Type.Identifier
    @Type.Property(name = "ID", contentType = ContentType.String)
    private UUID id;

    @Type.Property(name = "First Name", contentType = ContentType.String)
    private String firstName;

    @Type.Property(name = "Last Name", contentType = ContentType.String)
    private String lastName;

    @Type.Property(name = "Email", contentType = ContentType.String)
    private String email;

    @Type.Property(name = "Password", contentType = ContentType.Password)
    private String password;

    @Type.Property(name = "Groups", contentType = ContentType.List)
    private List<Group> groups = Lists.newArrayList();

    @Type.Property(name = "Created At", contentType = ContentType.DateTime)
    private OffsetDateTime createdAt;

    @Type.Property(name = "Updated At", contentType = ContentType.DateTime)
    private OffsetDateTime updatedAt;

    public User() {
    }

    public User(UserTable userTable) {
        this.id = userTable.getId();
        this.firstName = userTable.getFirstName();
        this.lastName = userTable.getLastName();
        this.email = userTable.getEmail();
        this.password = userTable.getPassword();
        this.createdAt = userTable.getCreatedAt();
        this.updatedAt = userTable.getUpdatedAt();

        this.groups = userTable.getGroups().stream()
                .map(Group::new)
                .collect(Collectors.toList());
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        return String.format("%s %s", firstName, lastName);
    }

    public String getPassword() {
        return password;
    }

    public boolean hasPassword() {
        return password != null;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
