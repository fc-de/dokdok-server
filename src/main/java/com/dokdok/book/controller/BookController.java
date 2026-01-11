package com.dokdok.book.controller;

import com.dokdok.book.dto.response.KakaoBookResponse;
import com.dokdok.book.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/book")
public class BookController {
    private final BookService bookService;

    @GetMapping("/search")
    public KakaoBookResponse searchBook(@RequestParam String title) {
        return bookService.searchBook(title);
    }
}
