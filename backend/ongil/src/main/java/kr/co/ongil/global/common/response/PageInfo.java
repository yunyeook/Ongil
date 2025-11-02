package kr.co.ongil.global.common.response;

import org.springframework.data.domain.Page;

public record PageInfo(
    long totalElements,
    int totalPages,
    boolean isLast,
    int currPage
) {

    public PageInfo(Page<?> page) {
        this(
            page.getTotalElements(),
            page.getTotalPages(),
            page.isLast(),
            page.getNumber() + 1  // 첫 페이지는 1부터
        );
    }

    // 외부 API 응답용 생성자 (Tmap 등)
    public PageInfo(int totalElements, int currentPage, int pageSize) {
        this(
            totalElements,
            (int) Math.ceil((double) totalElements / pageSize),
            currentPage >= (int) Math.ceil((double) totalElements / pageSize),
            currentPage
        );
    }

    public static PageInfo from(Page<?> page) {
        return new PageInfo(page);
    }

    public static PageInfo of(int totalElements, int currentPage, int pageSize) {
        return new PageInfo(totalElements, currentPage, pageSize);
    }
}