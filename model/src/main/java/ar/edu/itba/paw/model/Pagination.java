package ar.edu.itba.paw.model;

public final class Pagination {

    public static final int CARS_PAGE_SIZE = 16;
    public static final int REQUESTS_PAGE_SIZE = 12;
    public static final int REVIEWS_PAGE_SIZE = 5;
    public static final int DEFAULT_PAGE = 1;

    private Pagination() {}

    public static int normalizePage(final Integer page) {
        if (page == null || page < 1) {
            return DEFAULT_PAGE;
        }
        return page;
    }

    public static int offsetFor(final int page, final int pageSize) {
        return (page - 1) * pageSize;
    }
}
