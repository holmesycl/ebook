package com.holmesycl.ebook.db;

import org.apache.commons.dbcp2.BasicDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
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
}
