package kr.co.ongil.global.common.response;


import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
public class PageResponse {

    private final long totalElements;
    private final int totalPages;
    private final boolean isLast;
    private final int currPage;

    public PageResponse(Page<?> page) {
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.isLast = page.isLast();
        this.currPage = page.getNumber() + 1; //첫 페이지는 1부터
    }

    public static PageResponse from(Page<?> page) {
        return new PageResponse(page);
    }
}
