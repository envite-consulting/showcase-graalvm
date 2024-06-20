package de.envite.lecture.graalvm.book;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Setter
@Getter
@Document(collection = "books")
public class Book {

    @Id
    private String id;
    private String title;
    private String author;
    private int pageCount;

    public Book(String id, String title, String author, int pageCount) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.pageCount = pageCount;
    }
}
