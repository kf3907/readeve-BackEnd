package com.book_everywhere.service;

import com.book_everywhere.domain.book.Book;
import com.book_everywhere.domain.book.BookRepository;
import com.book_everywhere.domain.pin.Pin;
import com.book_everywhere.domain.pin.PinRepository;
import com.book_everywhere.domain.review.Review;
import com.book_everywhere.domain.review.ReviewRepository;
import com.book_everywhere.web.dto.review.ReviewDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final PinRepository pinRepository;

    //사용자 검증에 메소드

    //등록
    @Transactional
    public Long createReview(Long bookId, Long pinId, ReviewDto reviewDto) {
        Book book = bookRepository.findById(bookId).orElseThrow(
                ()-> new IllegalArgumentException("Book does not exist"));
        Pin pin = pinRepository.findById(pinId).orElseThrow(
                () -> new IllegalArgumentException("Pin does not exist"));

        Review review = new Review().createFromDto(book, pin, reviewDto);
        reviewRepository.save(review);
        return review.getId();
    }


    //수정
    @Transactional
    public void updateReview(Long id, ReviewDto reviewDto) {
        Review review = findOneReview(id);
        review.setTitle(reviewDto.getTitle());
        review.setContent(reviewDto.getContent());
        review.setIsPrivate(reviewDto.getIsPrivate());
    }


    //삭제
    @Transactional
    public void deleteReview(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new IllegalArgumentException("Review does not exist");
        }
        reviewRepository.deleteById(id);
    }


    //조회
    //특정 유저의 특정 책에 등록된 독후감 조회 기능
    public List<Review> findReviewsByUserAndBook(Long bookId) {
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new IllegalArgumentException("Book does not exist"));
        return reviewRepository.findByUserAndBook(book.getUser(), book);
    }

    //공유 목록에서의 독후감 조회
    public List<Review> findPublicReviews(boolean isPrivate, Pageable pageable) {
        return reviewRepository.findByIsPrivate(isPrivate, pageable);
    }


    //리뷰 하나만 조회
    public Review findOneReview(Long id) {
        return reviewRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Review does not exist"));
    }

    //등록된 모든 리뷰 조회
    public List<Review> findReviews() {
        return reviewRepository.findAll();
    }
}
