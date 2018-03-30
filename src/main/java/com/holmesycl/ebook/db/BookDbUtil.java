package com.holmesycl.ebook.db;

import com.holmesycl.ebook.bean.Book;
import com.holmesycl.ebook.bean.BookChapter;
import com.holmesycl.ebook.bean.BookIndex;
import com.holmesycl.ebook.bean.BookLink;
import org.apache.commons.dbutils.QueryRunner;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BookDbUtil {

    public static List<BookLink> qryAllBookLink() throws SQLException {
        QueryRunner runner = new QueryRunner(DateSourceHolder.get());
        return runner.query("select * from book_link", (ResultSet rs) -> {
            List<BookLink> bookLinks = new ArrayList<>();
            while (rs.next()) {
                String bookName = rs.getString("book_name");
                String linkUrl = rs.getString("link_url");
                BookLink bookLink = new BookLink(bookName, linkUrl);
                bookLinks.add(bookLink);
            }
            return bookLinks;
        });
    }

    public static Book getBookById(int bookId) throws SQLException {
        QueryRunner runner = new QueryRunner(DateSourceHolder.get());
        return runner.query("select * from book where book_id = ?", (ResultSet rs) -> {
            Book book = null;
            if (rs.next()) {
                book = new Book();
                book.setBookId(bookId);
                book.setBookName(rs.getString("book_name"));
                book.setAuthor(rs.getString("author"));
                book.setIntroduction(rs.getString("introduction"));
                book.setImage(rs.getString("image"));
                book.setLastUpdateDate(rs.getTimestamp("last_update_date"));
            }
            return book;
        }, bookId);
    }

    public static void saveBook(Book book) throws Exception {
        QueryRunner runner = new QueryRunner(DateSourceHolder.get());
        runner.insert("insert into book(book_id,book_name,author,introduction,image,last_update_date) values(?,?,?,?,?,?)", (ResultSet rs) -> {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                    return 0;
                },
                book.getBookId(), book.getBookName(), book.getAuthor(), book.getIntroduction(), book.getImage(), book.getLastUpdateDate());
    }

    public static void updateBook(Book book) throws SQLException {
        QueryRunner runner = new QueryRunner(DateSourceHolder.get());
        runner.update("update book set book_name = ?,author = ?,introduction = ?,image = ?,last_update_date = ? where book_id = ?",
                book.getBookName(), book.getAuthor(), book.getIntroduction(), book.getImage(), book.getLastUpdateDate(), book.getBookId());
    }

    public static BookChapter findBookChapterByBookIdAndPageNumber(int bookId, int pageNumber) throws SQLException {
        QueryRunner runner = new QueryRunner(DateSourceHolder.get());
        return runner.query("select * from book_chapter where book_id = ? and page_number = ?", (ResultSet rs) -> {
            BookChapter chapter = null;
            if (rs.next()) {
                chapter = new BookChapter();
                chapter.setBookId(bookId);
                chapter.setPageNumber(pageNumber);
                chapter.setPreviousChapter(rs.getInt("previous_chapter"));
                chapter.setNextChapter(rs.getInt("next_chapter"));
                chapter.setTitle(rs.getString("title"));
                chapter.setContent(rs.getString("content"));
                chapter.setCreateDate(rs.getTimestamp("create_date"));
            }
            return chapter;
        }, bookId, pageNumber);
    }

    public static void saveBookChapter(BookChapter chapter) throws SQLException {
        QueryRunner runner = new QueryRunner(DateSourceHolder.get());
        runner.insert("insert into book_chapter(book_id,page_number,previous_chapter,next_chapter,title,content,create_date) values(?,?,?,?,?,?,?)", (ResultSet rs) -> {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                    return 0;
                },
                chapter.getBookId(), chapter.getPageNumber(), chapter.getPreviousChapter(), chapter.getNextChapter(), chapter.getTitle(), chapter.getContent(), chapter.getCreateDate());
    }


    public static BookIndex findBookIndexByBookIdAndPageNumber(int bookId, int pageNumber) throws SQLException {
        QueryRunner runner = new QueryRunner(DateSourceHolder.get());
        return runner.query("select * from book_index where book_id = ? and page_number = ?", (ResultSet rs) -> {
            BookIndex index = null;
            if (rs.next()) {
                index = new BookIndex();
                index.setBookId(bookId);
                index.setPageNumber(pageNumber);
                index.setTitle(rs.getString("title"));
                index.setCreateDate(rs.getTimestamp("create_date"));
            }
            return index;
        }, bookId, pageNumber);
    }

    public static void saveBookIndex(BookIndex index) throws SQLException {
        QueryRunner runner = new QueryRunner(DateSourceHolder.get());
        runner.insert("insert into book_index(book_id,page_number,title,create_date) values(?,?,?,?)", (ResultSet rs) -> {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                    return 0;
                },
                index.getBookId(), index.getPageNumber(), index.getTitle(), index.getCreateDate());
    }
}
