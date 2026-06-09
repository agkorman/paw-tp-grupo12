package ar.edu.itba.paw.services.exception;

public class CommunitySlugConflictException extends RuntimeException {

    private final String slug;

    public CommunitySlugConflictException(final String slug, final Throwable cause) {
        super("Community slug conflict for slug=" + slug, cause);
        this.slug = slug;
    }

    public String getSlug() {
        return slug;
    }
}
