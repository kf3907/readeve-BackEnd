package com.book_everywhere.service;

import com.book_everywhere.domain.book.Book;
import com.book_everywhere.domain.book.BookRepository;
import com.book_everywhere.domain.user.User;
import com.book_everywhere.domain.user.UserRepository;
import com.book_everywhere.web.dto.book.BookDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    //등록
    @Transactional
    public Long createBook(Long socialId, BookDto bookDto) {
        User user = userRepository.findById(socialId).orElseThrow(() -> new IllegalArgumentException("User does not exist"));
        Book book = new Book().createBook(user, bookDto);
        bookRepository.save(book);
        return book.getId();
    }

    //수정
    @Transactional
    public void updateBook(Long id, BookDto bookDto) {
        Book book = bookRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Book does not exist"));
        book.setTitle(bookDto.getTitle());
        book.setCoverImageUrl(bookDto.getCoverImageUrl());
        book.setAuthor(bookDto.getAuthor());
        book.setComplete(bookDto.getIsComplete());
    }

    //삭제
    @Transactional
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Book does not exist"));
        bookRepository.delete(book);
    }

    //조회

    //특정 유저의 모든 책 목록 조회
    public List<BookDto> findAllBookOneUser(Long userSocialId) {
        User user = userRepository.findBySocialId(userSocialId);
        List<Book> init = bookRepository.findAllByUser(user);
        return init.stream().map(book -> new BookDto(
                book.getUser().getId(),
                book.getTitle(),
                book.getCoverImageUrl(),
                book.getAuthor(),
                book.isComplete(),
                book.getCreateAt())).toList();
    }


    //책 한권 조회
    public BookDto findOneBook(Long id) {
        Book init = bookRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Book does not exist"));
        return new BookDto(
                init.getUser().getId(),
                init.getTitle(),
                init.getCoverImageUrl(),
                init.getAuthor(),
                init.isComplete(),
                init.getCreateAt());
    }

    //등록된 모든 책 조회
    public List<BookDto> findAllBook() {
        List<Book> init = bookRepository.findAll();
        return init.stream().map(book -> new BookDto(
                book.getUser().getId(),
                book.getTitle(),
                book.getCoverImageUrl(),
                book.getAuthor(),
                book.isComplete(),
                book.getCreateAt())).toList();
    }

}
