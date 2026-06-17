package com.dokdok.book.dto.request;

public enum TimelineSortType {
    LATEST,   // 최신순(내림차순) — 프런트 기본값
    OLDEST,   // 오래된순(오름차순)
    DESC,     // 하위 호환: LATEST 와 동일(최신순)
    ASC;      // 하위 호환: OLDEST 와 동일(오래된순)

    /** 오름차순(오래된 순) 정렬이면 true */
    public boolean isAscending() {
        return this == OLDEST || this == ASC;
    }
}
