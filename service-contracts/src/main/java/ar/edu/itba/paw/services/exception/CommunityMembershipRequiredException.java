package ar.edu.itba.paw.services.exception;

public class CommunityMembershipRequiredException extends RuntimeException {

    private final String communitySlug;

    public CommunityMembershipRequiredException(final String communitySlug) {
        super("Community membership required for community slug=" + communitySlug);
        this.communitySlug = communitySlug;
    }

    public String getCommunitySlug() {
        return communitySlug;
    }
}
