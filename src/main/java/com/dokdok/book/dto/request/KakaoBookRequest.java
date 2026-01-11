package com.dokdok.book.dto.request;

import com.dokdok.book.entity.Book;

import java.util.List;

public record KakaoBookRequest(
        String title,
        List<String> authors,
        String publisher,
        String isbn,
        String thumbnail
) {
    public Book toEntity() {
        return Book.builder()
                .bookName(title)
                .author(authors != null && !authors.isEmpty()
                        ? String.join(", ", authors)
                        : null)
                .publisher(publisher)
                .thumbnail(thumbnail)
                .isbn(isbn)
                .build();
    }
}
