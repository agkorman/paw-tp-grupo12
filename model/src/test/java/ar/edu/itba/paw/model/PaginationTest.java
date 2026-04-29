package ar.edu.itba.paw.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PaginationTest {

    @Test
    public void normalizePageUsesDefaultForMissingOrInvalidPage() {
        assertEquals(Pagination.DEFAULT_PAGE, Pagination.normalizePage(null));
        assertEquals(Pagination.DEFAULT_PAGE, Pagination.normalizePage(0));
        assertEquals(Pagination.DEFAULT_PAGE, Pagination.normalizePage(-10));
    }

    @Test
    public void clampPageLimitsRequestedPageToAvailablePages() {
        assertEquals(1, Pagination.clampPage(0, 23L, 5));
        assertEquals(5, Pagination.clampPage(99, 23L, 5));
    }

    @Test
    public void clampPageUsesDefaultWhenThereAreNoItems() {
        assertEquals(Pagination.DEFAULT_PAGE, Pagination.clampPage(99, 0L, 5));
    }

    @Test
    public void offsetForUsesLongArithmetic() {
        assertEquals(42_949_672_920L, Pagination.offsetFor(Integer.MAX_VALUE, 20));
    }

    @Test
    public void totalPagesIsCappedToIntegerRange() {
        assertEquals(Integer.MAX_VALUE, Pagination.totalPages(Long.MAX_VALUE, 5));
    }
}
