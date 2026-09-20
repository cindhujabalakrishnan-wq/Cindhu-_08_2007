package com.insurance.platform.dto.common;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * Generic paginated response wrapper.
 *
 * @param <T> element type
 */
public class PageResponse<T> {

    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;

    /** Creates an empty page response. */
    public PageResponse() {
    }

    /**
     * Creates a fully populated page response.
     *
     * @param content page content
     * @param page zero-based page index
     * @param size page size
     * @param totalElements total element count
     * @param totalPages total page count
     * @param last whether this is the last page
     */
    public PageResponse(List<T> content, int page, int size, long totalElements, int totalPages, boolean last) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.last = last;
    }

    /**
     * Builds a page response from a Spring Data page.
     *
     * @param page Spring Data page
     * @param <T> element type
     * @return page response
     */
    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isLast());
    }

    /**
     * Builds a page response from mapped content and the source page metadata.
     *
     * @param content mapped content
     * @param page source page for metadata
     * @param <T> content type
     * @param <S> source element type
     * @return page response
     */
    public static <T, S> PageResponse<T> of(List<T> content, Page<S> page) {
        return new PageResponse<>(
            content,
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isLast());
    }

    /**
     * Returns the page content.
     *
     * @return content
     */
    public List<T> getContent() {
        return content;
    }

    /**
     * Sets the page content.
     *
     * @param content content
     */
    public void setContent(List<T> content) {
        this.content = content;
    }

    /**
     * Returns the zero-based page index.
     *
     * @return page index
     */
    public int getPage() {
        return page;
    }

    /**
     * Sets the zero-based page index.
     *
     * @param page page index
     */
    public void setPage(int page) {
        this.page = page;
    }

    /**
     * Returns the page size.
     *
     * @return page size
     */
    public int getSize() {
        return size;
    }

    /**
     * Sets the page size.
     *
     * @param size page size
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * Returns the total element count.
     *
     * @return total elements
     */
    public long getTotalElements() {
        return totalElements;
    }

    /**
     * Sets the total element count.
     *
     * @param totalElements total elements
     */
    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    /**
     * Returns the total page count.
     *
     * @return total pages
     */
    public int getTotalPages() {
        return totalPages;
    }

    /**
     * Sets the total page count.
     *
     * @param totalPages total pages
     */
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    /**
     * Returns whether this is the last page.
     *
     * @return true when last
     */
    public boolean isLast() {
        return last;
    }

    /**
     * Sets the last-page flag.
     *
     * @param last last-page flag
     */
    public void setLast(boolean last) {
        this.last = last;
    }
}
