package ar.edu.itba.paw.services.exception;

public class CommunityPostSlugConflictException extends RuntimeException {

    private final String communitySlug;

    public CommunityPostSlugConflictException(final String communitySlug, final Throwable cause) {
        super("Community post slug conflict for community slug=" + communitySlug, cause);
        this.communitySlug = communitySlug;
    }

    public String getCommunitySlug() {
        return communitySlug;
    }
}
