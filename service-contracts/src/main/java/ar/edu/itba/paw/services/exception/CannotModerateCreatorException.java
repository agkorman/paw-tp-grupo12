package ar.edu.itba.paw.services.exception;

public class CannotModerateCreatorException extends RuntimeException {

    private final String communitySlug;

    public CannotModerateCreatorException(final String communitySlug) {
        super("Cannot moderate community creator for community slug=" + communitySlug);
        this.communitySlug = communitySlug;
    }

    public String getCommunitySlug() {
        return communitySlug;
    }
}
