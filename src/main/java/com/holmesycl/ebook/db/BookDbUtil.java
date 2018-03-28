package com.holmesycl.ebook.db;

import com.holmesycl.ebook.bean.BookLink;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
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
                List<BookLink> bookLinks = new ArrayList<BookLink>();
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

    public static void main(String[] args) throws SQLException {
        System.out.println(BookDbUtil.qryAllBookLink());
    }


}
