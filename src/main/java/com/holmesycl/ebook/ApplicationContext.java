package com.holmesycl.ebook;

import com.holmesycl.ebook.bean.Book;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ApplicationContext {

    private final ConcurrentLinkedQueue<Book> updateBooks = new ConcurrentLinkedQueue<>();

    public void offerUpdateBook(Book book) {
        this.updateBooks.offer(book);
    }

    public Book pollUpdateBook() {
        return this.updateBooks.poll();
    }

}
