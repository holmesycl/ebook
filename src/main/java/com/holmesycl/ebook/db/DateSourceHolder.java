package com.holmesycl.ebook.db;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class DateSourceHolder {

    private static final String URL = "jdbc:mysql://holmesycl.com:3306/ebook?useUnicode=true&characterEncoding=UTF-8";
    private static final String DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";
    private static final String USERNAME = "ebook";
    private static final String PASSWORD = "123456";

    private static DataSource dataSource;

    static {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(DRIVER_CLASS_NAME);
        ds.setUrl(URL);
        ds.setUsername(USERNAME);
        ds.setPassword(PASSWORD);
        dataSource = ds;
    }

    public static DataSource get() {
        return dataSource;
    }

    public static Connection getConnection() throws SQLException {
        return get().getConnection();
    }

    public static void main(String[] args) {
        Connection connection = DateSourceHolder.get().getConnection();
        QueryRunner runner = new QueryRunner();
        try {
            Object[] vals = runner.query("select * from ebook.book_link", new ResultSetHandler<Object[]>() {
                @Override
                public Object[] handle(ResultSet rs) throws SQLException {
                    if (!rs.next()) {
                        return null;
                    }

                    ResultSetMetaData meta = rs.getMetaData();
                    int cols = meta.getColumnCount();
                    Object[] result = new Object[cols];

                    for (int i = 0; i < cols; i++) {
                        result[i] = rs.getObject(i + 1);
                        System.out.println(meta.getColumnName(i + 1) + result[i]);
                    }

                    return result;
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
