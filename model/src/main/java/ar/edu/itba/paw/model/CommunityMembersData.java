package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CommunityMembersData implements Serializable {

    private final Community community;
    private final List<CommunityMembershipEntry> members;
    private final String viewerRole;
    private final boolean viewerCreator;

    public CommunityMembersData(final Community community,
                                final List<CommunityMembershipEntry> members,
                                final String viewerRole,
                                final boolean viewerCreator) {
        this.community = community;
        this.members = members == null ? new ArrayList<>() : new ArrayList<>(members);
        this.viewerRole = viewerRole;
        this.viewerCreator = viewerCreator;
    }

    public Community getCommunity() {
        return community;
    }

    public List<CommunityMembershipEntry> getMembers() {
        return new ArrayList<>(members);
    }

    public String getViewerRole() {
        return viewerRole;
    }

    public boolean isViewerModerator() {
        return CommunityRole.MODERATOR.equals(viewerRole);
    }

    public boolean isViewerMember() {
        return CommunityRole.MEMBER.equals(viewerRole) || CommunityRole.MODERATOR.equals(viewerRole);
    }

    public boolean isViewerCreator() {
        return viewerCreator;
    }
}
