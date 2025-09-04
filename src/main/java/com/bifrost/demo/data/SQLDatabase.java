package com.bifrost.demo.data;

import java.sql.Connection;
import java.sql.SQLException;

public interface SQLDatabase {
    Connection getConnection() throws SQLException;
}
