package com.dokdok.book.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record KakaoBookResponse(
        List<Document> documents,
        Meta meta
) {
    public record Document(
            String title,
            String contents,
            List<String> authors,
            String publisher,
            String isbn,
            String thumbnail
    ) {}

    public record Meta(
            @JsonProperty("is_end") boolean isEnd,
            @JsonProperty("pageable_count") int pageableCount,
            @JsonProperty("total_count") int totalCount
    ) {}
}

