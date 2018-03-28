package com.holmesycl.ebook.db;

import com.holmesycl.ebook.bean.BookLink;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class BookDbUtil {

    public static BookLink[] qryAllBookLink() throws SQLException {
        Connection connection = DateSourceHolder.getConnection();
        QueryRunner runner = new QueryRunner();
        return runner.query(connection, "select * from book_link", new ResultSetHandler<BookLink[]>() {
            /**
             * Turn the <code>ResultSet</code> into an Object.
             *
             * @param rs The <code>ResultSet</code> to handle.  It has not been touched
             *           before being passed to this method.
             * @return An Object initialized with <code>ResultSet</code> data. It is
             * legal for implementations to return <code>null</code> if the
             * <code>ResultSet</code> contained 0 rows.
             * @throws SQLException if a database access error occurs
             */
            @Override
            public BookLink[] handle(ResultSet rs) throws SQLException {
                while(rs.next()){
                    rs.getInt(1);
                }

                ResultSetMetaData meta = rs.getMetaData();
                int cols = meta.getColumnCount();
                BookLink[] result = new BookLink[cols];

                for (int i = 0; i < cols; i++) {
                    result[i] = rs.getObject(i + 1);
                }
            }
        });
    }


}
