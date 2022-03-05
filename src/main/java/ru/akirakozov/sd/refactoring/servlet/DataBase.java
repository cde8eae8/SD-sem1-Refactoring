package ru.akirakozov.sd.refactoring.servlet;

import java.io.IOException;
import java.sql.*;

public class DataBase {
    private final String path;

    public DataBase(String path) {
        this.path = path;
    }

    public void sqlRequest(String request, Utils.throwableConsumer<ResultSet> consumer) throws SQLException, IOException {
        try (Connection c = DriverManager.getConnection(path)) {
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery(request);

            consumer.accept(rs);

            rs.close();
            stmt.close();
        }
    }

    public void sqlUpdate(String sql) throws SQLException {
        try (Connection c = DriverManager.getConnection(path)) {
            Statement stmt = c.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
        }
    }
}
