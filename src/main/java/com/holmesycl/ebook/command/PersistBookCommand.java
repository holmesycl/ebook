package com.holmesycl.ebook.command;

import com.holmesycl.ebook.ApplicationContext;
import com.holmesycl.ebook.bean.Book;
import com.holmesycl.ebook.bean.Bookshelf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class PersistBookCommand implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(DiscoverBookCommand.class);

    private ApplicationContext context;

    private List<Bookshelf> bookshelves = new ArrayList<>();

    public PersistBookCommand(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void run() {
        Book book;
        while ((book = context.pollUpdateBook()) != null) {
            Bookshelf bookshelf = new Bookshelf();
            bookshelf.setBookId(book.getBookId());
            bookshelf.setBookName(book.getBookName());
            bookshelf.setAuthor(book.getAuthor());
            bookshelf.setImgUrl(book.getImage());
            bookshelves.add(bookshelf);
        }
    }
}
