package com.dokdok.topic.service;

import com.dokdok.book.dto.request.BookReviewRequest;
import com.dokdok.book.dto.response.BookReviewResponse;
import com.dokdok.book.entity.Book;
import com.dokdok.book.exception.BookErrorCode;
import com.dokdok.book.exception.BookException;
import com.dokdok.book.repository.BookReviewRepository;
import com.dokdok.book.service.BookReviewService;
import com.dokdok.book.service.BookValidator;
import com.dokdok.global.util.SecurityUtil;
import com.dokdok.keyword.entity.Keyword;
import com.dokdok.keyword.service.KeywordValidator;
import com.dokdok.meeting.entity.Meeting;
import com.dokdok.meeting.service.MeetingValidator;
import com.dokdok.topic.entity.PreOpinionBookReview;
import com.dokdok.topic.entity.PreOpinionBookReviewKeyword;
import com.dokdok.topic.repository.PreOpinionBookReviewRepository;
import com.dokdok.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PreOpinionBookReviewService {

    private final PreOpinionBookReviewRepository preOpinionBookReviewRepository;
    private final BookReviewRepository bookReviewRepository;
    private final BookReviewService bookReviewService;
    private final MeetingValidator meetingValidator;
    private final BookValidator bookValidator;
    private final KeywordValidator keywordValidator;

    @Transactional
    public BookReviewResponse upsertReview(Long meetingId, BookReviewRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        User user = SecurityUtil.getCurrentUserEntity();

        Meeting meeting = meetingValidator.findMeetingOrThrow(meetingId);
        Book book = meeting.getBook();
        if (book == null) {
            throw new BookException(BookErrorCode.BOOK_NOT_FOUND);
        }

        bookValidator.validateRating(request.rating());
        List<Keyword> keywords = request.keywordIds().stream()
                .map(keywordValidator::validateAndGetSelectableKeyword)
                .collect(Collectors.toList());

        PreOpinionBookReview review = preOpinionBookReviewRepository.findByMeetingIdAndUserId(meetingId, userId)
                .map(existing -> {
                    existing.updateReview(request.rating(), keywords);
                    return existing;
                })
                .orElseGet(() -> preOpinionBookReviewRepository.save(
                        PreOpinionBookReview.create(meeting, book, user, request.rating(), keywords)
                ));

        return toResponse(review);
    }

    @Transactional(readOnly = true)
    public BookReviewResponse findMyReview(Long meetingId) {
        Long userId = SecurityUtil.getCurrentUserId();
        return preOpinionBookReviewRepository.findByMeetingIdAndUserId(meetingId, userId)
                .map(this::toResponse)
                .orElse(null);
    }

    @Transactional
    public void deleteMyReview(Long meetingId) {
        Long userId = SecurityUtil.getCurrentUserId();
        preOpinionBookReviewRepository.findByMeetingIdAndUserId(meetingId, userId)
                .ifPresent(PreOpinionBookReview::deleteReview);
    }

    @Transactional
    public BookReviewResponse applyToPersonalBookReview(Long meetingId, BookReviewRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        Meeting meeting = meetingValidator.findMeetingOrThrow(meetingId);
        Book book = meeting.getBook();
        if (book == null) {
            throw new BookException(BookErrorCode.BOOK_NOT_FOUND);
        }

        boolean exists = bookReviewRepository.findByBookIdAndUserId(book.getId(), userId).isPresent();
        return exists
                ? bookReviewService.updateMyReview(book.getId(), request)
                : bookReviewService.createReview(book.getId(), request);
    }

    private BookReviewResponse toResponse(PreOpinionBookReview review) {
        List<BookReviewResponse.KeywordInfo> keywordInfos = review.getKeywords().stream()
                .map(PreOpinionBookReviewKeyword::getKeyword)
                .map(keyword -> new BookReviewResponse.KeywordInfo(
                        keyword.getId(),
                        keyword.getKeywordName(),
                        keyword.getKeywordType()
                ))
                .toList();

        return BookReviewResponse.of(
                review.getId(),
                review.getBook().getId(),
                review.getUser().getId(),
                review.getRating(),
                keywordInfos
        );
    }
}
