package ar.edu.itba.paw.model;

import java.io.Serializable;

public class CommunityMembersData implements Serializable {

    private final Community community;
    private final Page<CommunityMembershipEntry> membersPage;
    private final long totalMemberCount;
    private final String viewerRole;
    private final boolean viewerCreator;

    public CommunityMembersData(final Community community,
                                final Page<CommunityMembershipEntry> membersPage,
                                final long totalMemberCount,
                                final String viewerRole,
                                final boolean viewerCreator) {
        this.community = community;
        this.membersPage = membersPage;
        this.totalMemberCount = totalMemberCount;
        this.viewerRole = viewerRole;
        this.viewerCreator = viewerCreator;
    }

    public Community getCommunity() {
        return community;
    }

    public Page<CommunityMembershipEntry> getMembersPage() {
        return membersPage;
    }

    public long getTotalMemberCount() {
        return totalMemberCount;
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
