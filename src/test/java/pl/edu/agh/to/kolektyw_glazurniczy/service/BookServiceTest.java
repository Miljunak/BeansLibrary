package pl.edu.agh.to.kolektyw_glazurniczy.service;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import pl.edu.agh.to.kolektyw_glazurniczy.model.Author;
import pl.edu.agh.to.kolektyw_glazurniczy.model.Book;
import pl.edu.agh.to.kolektyw_glazurniczy.repository.AuthorRepository;
import pl.edu.agh.to.kolektyw_glazurniczy.repository.BookRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@SpringBootTest
class BookServiceTest {
    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private BookService bookService;

    @Test
    public void shouldSaveBook() {
        Book book = mock(Book.class);
        when(bookRepository.save(book)).thenReturn(book);

        bookService.save(book);

        verify(bookRepository, times(1)).save(eq(book));
    }

    @Test
    public void shouldSaveAuthorIfDoesntExist() {
        Author author = mock(Author.class);
        Book book = mock(Book.class);
        when(book.getAuthors()).thenReturn(List.of(author));
        when(bookRepository.save(book)).thenReturn(book);

        bookService.save(book);

        verify(bookRepository, times(1)).save(eq(book));
        verify(authorRepository, times(1)).saveAll(any());
    }

    @Test
    public void shouldUpdateAuthorWhenAlreadyExist() {
        Author author = new Author("Author", LocalDate.parse("2020-10-10"));
        Book book = mock(Book.class);
        when(book.getAuthors()).thenReturn(List.of(author));
        when(bookRepository.save(book)).thenReturn(book);
        when(authorRepository.findByName(author.getName())).thenReturn(Optional.of(author));

        bookService.save(book);

        verify(bookRepository, times(1)).save(eq(book));
        verify(authorRepository, times(1)).saveAll(any());
    }
}