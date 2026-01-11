package com.dokdok.book.service;

import com.dokdok.book.dto.response.KakaoBookResponse;
import com.dokdok.book.webClient.KakaoBookClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookService {

    private final KakaoBookClient kakaoBookClient;

    // 외부 API로 책 검색 후 가지고오기 or 저장하기
    public KakaoBookResponse searchBook(String query) {
        return kakaoBookClient.searchBooks(query);
    }
}
