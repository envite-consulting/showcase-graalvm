package de.envite.lecture.graalvm.book;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.joining;

@RestController
@RequestMapping("/books")
public class BookController {

    private final BookRepository bookRepository;

    @Autowired
    public BookController(final BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @GetMapping
    public List<Book> getBooks() {
        return bookRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable("id") final String id) {
        final Optional<Book> bookOptional = bookRepository.findById(id);

        return bookOptional.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Void> createBook(@RequestBody final Book book) {
        final Book savedBook = bookRepository.save(book);

        final URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(savedBook.getId()).toUri();

        return ResponseEntity.created(location).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateBook(@PathVariable("id") final String id, @RequestBody final Book book) {
        final Optional<Book> bookOptional = bookRepository.findById(id);

        if (bookOptional.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        book.setId(id);
        bookRepository.save(book);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public void deleteBooks() {
        bookRepository.deleteAll();
    }

    @DeleteMapping("/{id}")
    public void deleteBookById(@PathVariable("id") final String id) {
        bookRepository.deleteById(id);
    }

    @GetMapping("/bulk/{ids}")
    public List<Book> getBooksById(@PathVariable("ids") final List<String> ids) {
        return bookRepository.findAllById(ids);
    }

    @PostMapping("/bulk")
    public ResponseEntity<Void> createBooks(@RequestBody final List<Book> books) {
        final List<Book> savedBooks = bookRepository.saveAll(books);

        final URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{ids}")
                .buildAndExpand(savedBooks.stream().map(Book::getId).collect(joining(","))).toUri();

        return ResponseEntity.created(location).build();
    }

    @DeleteMapping("/bulk/{ids}")
    public void deleteBooksById(@PathVariable("ids") final List<String> ids) {
        bookRepository.deleteAllById(ids);
    }

}
