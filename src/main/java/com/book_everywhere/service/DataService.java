package com.book_everywhere.service;

import com.book_everywhere.domain.book.Book;
import com.book_everywhere.domain.book.BookRepository;
import com.book_everywhere.domain.pin.Pin;
import com.book_everywhere.domain.pin.PinRepository;
import com.book_everywhere.domain.review.Review;
import com.book_everywhere.domain.review.ReviewRepository;
import com.book_everywhere.domain.tag.Tag;
import com.book_everywhere.domain.tag.TagRepository;
import com.book_everywhere.domain.tagged.Tagged;
import com.book_everywhere.domain.tagged.TaggedRepository;
import com.book_everywhere.domain.visit.Visit;
import com.book_everywhere.domain.visit.VisitRepository;
import com.book_everywhere.web.dto.AllDataDto;
import com.book_everywhere.web.dto.book.BookRespDto;
import com.book_everywhere.web.dto.exception.customs.CustomErrorCode;
import com.book_everywhere.web.dto.exception.customs.EntityNotFoundException;
import com.book_everywhere.web.dto.pin.PinRespDto;
import com.book_everywhere.web.dto.tag.TagRespDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

//프론트 단 요청에 의해 만들어진 서비스 입니다. 이후에 삭제될 예정입니다.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DataService {

    private final ReviewRepository reviewRepository;
    private final PinRepository pinRepository;
    private final BookRepository bookRepository;
    private final TaggedRepository taggedRepository;
    private final TagRepository tagRepository;
    private final VisitRepository visitRepository;



    public List<AllDataDto> 모든공유또는개인데이터가져오기(boolean isPrivate) {
        List<Review> reviews = reviewRepository.findByIsPrivateOrderByCreateAtDesc(isPrivate);

        return reviews.stream().map(review ->
        {
            Pin pin = pinRepository.mFindByPinId(review.getPin().getId());
            Visit visit = visitRepository.mFindByUserAndPin(review.getBook().getUser().getId(), review.getPin().getId());
            PinRespDto pinRespDto = new PinRespDto(
                    pin.getTitle(),
                    pin.getPlaceId(),
                    pin.getLatitude(),
                    pin.getLongitude(),
                    pin.getAddress(),
                    visit.isPinPrivate(),
                    pin.getUrl());
            Book book = bookRepository.findById(review.getBook().getId())
                    .orElseThrow(() -> new EntityNotFoundException(CustomErrorCode.BOOK_NOT_FOUND));
            BookRespDto bookRespDto = new BookRespDto(
                    book.getIsbn(),
                    book.getTitle(),
                    book.getCoverImageUrl(),
                    book.isComplete(),
                    book.getAuthor()
            );
            List<Tagged> taggedList = taggedRepository.findAllByPinId(pin.getId());
            List<String> tags = tagRepository.findAll().stream().map(Tag::getContent).toList();
            List<TagRespDto> tagRespDtoList = taggedList.stream().map(tagged ->
            {
                boolean isSelected = false;

                for (String tag : tags) {
                    if (tag.equals(tagged.getTag().getContent())) {
                        isSelected = true;
                        break;
                    }
                }

                return new TagRespDto(
                        tagged.getTag().getContent(),
                        isSelected
                );
            }).toList();

            return new AllDataDto(
                    review.getId(),
                    book.getUser().getSocialId(),
                    review.getWriter(),
                    review.getTitle(),
                    review.isPrivate(),
                    pinRespDto,
                    bookRespDto,
                    tagRespDtoList,
                    review.getContent(),
                    review.getCreateAt()
            );
        }).toList();
    }

    //태그 조회 테스트용
    public List<Tagged> findAllTaggedList(Long pinId) {
        return taggedRepository.findAllByPinId(pinId);
    }


    public List<AllDataDto> 유저독후감조회(Long userId) {
        List<Review> reviews = reviewRepository.mFindReviewsByUser(userId);

        return reviews.stream().map(review ->
        {
            Pin pin = pinRepository.mFindByPinId(review.getPin().getId());
            Visit visit = visitRepository.mFindByUserAndPin(userId, review.getPin().getId());
            PinRespDto pinRespDto = new PinRespDto(
                    pin.getTitle(),
                    pin.getPlaceId(),
                    pin.getLatitude(),
                    pin.getLongitude(),
                    pin.getAddress(),
                    visit.isPinPrivate(),
                    pin.getUrl());
            Book book = bookRepository.findById(review.getBook().getId())
                    .orElseThrow(() -> new EntityNotFoundException(CustomErrorCode.BOOK_NOT_FOUND));
            BookRespDto bookRespDto = new BookRespDto(
                    book.getIsbn(),
                    book.getTitle(),
                    book.getCoverImageUrl(),
                    book.isComplete(),
                    book.getAuthor()
            );
            List<Tagged> taggedList = taggedRepository.findAllByPinId(pin.getId());
            List<String> tags = tagRepository.findAll().stream().map(Tag::getContent).toList();
            List<TagRespDto> tagRespDtoList = tags.stream().map(tag -> {
                for (Tagged tagged : taggedList) {
                    if (tag.equals(tagged.getTag().getContent())) {
                        return new TagRespDto(tag, true);
                    }
                }
                return new TagRespDto(tag, false);
            }).toList();


            return new AllDataDto(
                    review.getId(),
                    book.getUser().getSocialId(),
                    review.getWriter(),
                    review.getTitle(),
                    review.isPrivate(),
                    pinRespDto,
                    bookRespDto,
                    tagRespDtoList,
                    review.getContent(),
                    review.getCreateAt()
            );
        }).toList();
    }


}
