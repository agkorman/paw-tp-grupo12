package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class CommunityMembershipEntry implements Serializable {

    private final long userId;
    private final String username;
    private final String role;
    private final LocalDateTime joinedAt;
    private final boolean creator;

    public CommunityMembershipEntry(final long userId, final String username,
                                    final String role, final LocalDateTime joinedAt) {
        this(userId, username, role, joinedAt, false);
    }

    public CommunityMembershipEntry(final long userId, final String username,
                                    final String role, final LocalDateTime joinedAt,
                                    final boolean creator) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.joinedAt = joinedAt;
        this.creator = creator;
    }

    public boolean isCreator() {
        return creator;
    }

    public boolean isModerator() {
        return "moderator".equals(role);
    }

    public long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }
}
