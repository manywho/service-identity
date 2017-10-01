package com.manywho.services.identity.memberships;

import com.google.common.collect.Sets;
import com.manywho.services.identity.users.UserTable;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "\"Membership\"")
public class MembershipTable {

    private UUID user;
    private UUID group;

    public MembershipTable() {
    }

    public MembershipTable(UUID user, UUID group) {
        this.user = user;
        this.group = group;
    }

    @Id
    @Column(name = "user_id")
    public UUID getUser() {
        return user;
    }

    public void setUser(UUID user) {
        this.user = user;
    }

    @Id
    @Column(name = "group_id")
    public UUID getGroup() {
        return group;
    }

    public void setGroup(UUID group) {
        this.group = group;
    }
}
