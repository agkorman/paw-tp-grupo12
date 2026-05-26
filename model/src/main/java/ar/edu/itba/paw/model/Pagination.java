package ar.edu.itba.paw.model;

public final class Pagination {

    public static final int CARS_PAGE_SIZE = 16;
    public static final int REQUESTS_PAGE_SIZE = 12;
    public static final int REVIEWS_PAGE_SIZE = 6;
    public static final int CONNECTIONS_PAGE_SIZE = 8;
    public static final int COMMUNITIES_PAGE_SIZE = 12;
    public static final int COMMUNITY_POSTS_PAGE_SIZE = 6;
    public static final int DEFAULT_PAGE = 1;

    private Pagination() {}

    public static int normalizePage(final Integer page) {
        if (page == null || page < 1) {
            return DEFAULT_PAGE;
        }
        return page;
    }

    public static int totalPages(final long totalItems, final int pageSize) {
        if (pageSize <= 0 || totalItems <= 0L) {
            return 0;
        }
        final long totalPages = ((totalItems - 1L) / pageSize) + 1L;
        return totalPages > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) totalPages;
    }

    public static int clampPage(final int page, final long totalItems, final int pageSize) {
        final int normalizedPage = normalizePage(page);
        final int totalPages = totalPages(totalItems, pageSize);
        if (totalPages == 0) {
            return DEFAULT_PAGE;
        }
        return Math.min(normalizedPage, totalPages);
    }

    public static long offsetFor(final int page, final int pageSize) {
        final int normalizedPage = Math.max(page, DEFAULT_PAGE);
        if (pageSize <= 0) {
            return 0L;
        }
        return ((long) normalizedPage - 1L) * (long) pageSize;
    }
}
