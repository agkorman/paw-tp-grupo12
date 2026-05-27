package ar.edu.itba.paw.services.exception;

public class CommunityOwnerRequiredException extends RuntimeException {

    private final String communitySlug;

    public CommunityOwnerRequiredException(final String communitySlug) {
        super("Community owner role required for community slug=" + communitySlug);
        this.communitySlug = communitySlug;
    }

    public String getCommunitySlug() {
        return communitySlug;
    }
}
