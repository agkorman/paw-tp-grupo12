package ar.edu.itba.paw.services.exception;

public class CommunityCreatorCannotLeaveException extends RuntimeException {

    private final String communitySlug;

    public CommunityCreatorCannotLeaveException(final String communitySlug) {
        super("Community creator cannot leave community slug=" + communitySlug);
        this.communitySlug = communitySlug;
    }

    public String getCommunitySlug() {
        return communitySlug;
    }
}
