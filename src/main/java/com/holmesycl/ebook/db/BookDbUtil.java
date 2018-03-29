package com.holmesycl.ebook.db;

import com.holmesycl.ebook.bean.Book;
import com.holmesycl.ebook.bean.BookLink;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BookDbUtil {

    public static List<BookLink> qryAllBookLink() throws SQLException {
        Connection connection = DateSourceHolder.getConnection();
        QueryRunner runner = new QueryRunner();
        return runner.query(connection, "select * from book_link", new ResultSetHandler<List<BookLink>>() {

            @Override
            public List<BookLink> handle(ResultSet rs) throws SQLException {
                List<BookLink> bookLinks = new ArrayList<>();
                while (rs.next()) {
                    String bookName = rs.getString("book_name");
                    String linkUrl = rs.getString("link_url");
                    BookLink bookLink = new BookLink(bookName, linkUrl);
                    bookLinks.add(bookLink);
                }
                return bookLinks;
            }
        });
    }

    public static Book getBookById(int bookId) throws SQLException {
        Connection connection = DateSourceHolder.getConnection();
        QueryRunner runner = new QueryRunner();
        return runner.query(connection, "select * from book where book_id = ?", (ResultSet rs) -> {
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


    public static void main(String[] args) throws SQLException {
        System.out.println(BookDbUtil.qryAllBookLink());
    }


    public static void saveBook(Book book) throws Exception {
        Connection connection = DateSourceHolder.getConnection();
        QueryRunner runner = new QueryRunner();
        runner.insert(connection, "insert into book(book_id,book_name,author,introduction,image,last_update_date) values(?,?,?,?,?,?)", (ResultSet rs) -> {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                    return 0;
                },
                book.getBookId(), book.getBookName(), book.getAuthor(), book.getIntroduction(), book.getImage(), book.getLastUpdateDate());
    }

    public static void updateBook(Book book) throws SQLException {
        Connection connection = DateSourceHolder.getConnection();
        QueryRunner runner = new QueryRunner();
        runner.update(connection, "update book set book_name = ?,author = ?,introduction = ?,image = ?,last_update_date = ? where book_id = ?",
                book.getBookName(), book.getAuthor(), book.getIntroduction(), book.getImage(), book.getLastUpdateDate(), book.getBookId());
    }
}
