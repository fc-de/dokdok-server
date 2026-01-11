package com.dokdok.book.service;

import com.dokdok.book.dto.response.BookDetailResponse;
import com.dokdok.book.dto.response.KakaoBookResponse;
import com.dokdok.book.entity.Book;
import com.dokdok.book.repository.BookRepository;
import com.dokdok.book.webClient.KakaoBookClient;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final KakaoBookClient kakaoBookClient;

    // 외부 API로 책 검색 후 가지고오기 or 저장하기
    public KakaoBookResponse searchBook(String query) {
        return kakaoBookClient.searchBooks(query);
    }

    public BookDetailResponse findBookByIsbn(String isbn) {
        Book entity = bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new EntityNotFoundException("데이터가 존재하지 않습니다."));
        return BookDetailResponse.from(entity);
    }
}
