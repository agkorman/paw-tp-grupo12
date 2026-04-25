package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class Page<T> implements Serializable {

    private final List<T> items;
    private final int pageNumber;
    private final int pageSize;
    private final long totalItems;

    public Page(final List<T> items, final int pageNumber, final int pageSize, final long totalItems) {
        this.items = items == null ? Collections.emptyList() : Collections.unmodifiableList(items);
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalItems = totalItems;
    }

    public static <T> Page<T> empty(final int pageNumber, final int pageSize) {
        return new Page<>(Collections.emptyList(), pageNumber, pageSize, 0L);
    }

    public List<T> getItems() {
        return items;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public int getTotalPages() {
        if (pageSize <= 0 || totalItems <= 0) {
            return 0;
        }
        return (int) ((totalItems + pageSize - 1) / pageSize);
    }

    public boolean hasPrevious() {
        return pageNumber > 1;
    }

    public boolean hasNext() {
        return pageNumber < getTotalPages();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}
