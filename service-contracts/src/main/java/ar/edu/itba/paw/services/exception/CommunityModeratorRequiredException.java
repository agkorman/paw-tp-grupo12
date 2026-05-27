package ar.edu.itba.paw.services.exception;

public class CommunityModeratorRequiredException extends RuntimeException {

    private final String communitySlug;

    public CommunityModeratorRequiredException(final String communitySlug) {
        super("Community moderator role required for community slug=" + communitySlug);
        this.communitySlug = communitySlug;
    }

    public String getCommunitySlug() {
        return communitySlug;
    }
}
