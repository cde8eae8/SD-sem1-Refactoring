package ru.akirakozov.sd.refactoring.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.*;
import java.util.Optional;

public class Utils {
    @FunctionalInterface
    public interface throwableConsumer<T> {
        void accept(T t) throws IOException, SQLException;
    }

    @FunctionalInterface
    public interface throwableFunction<T, R> {
        R apply(T t) throws IOException, SQLException;
    }

    static void htmlTable(ResultSet rs, PrintWriter writer) throws SQLException {
        while (rs.next()) {
            String  name = rs.getString("name");
            int price  = rs.getInt("price");
            writer.println(name + "\t" + price + "</br>");
        }
    }

    static void fromTemplate(String template, Optional<Integer> value, PrintWriter writer) throws SQLException {
        String result = template.replace("%value%", value.map(Long::toString).orElse(""));
        writer.print(result);
    }

    static void fromTemplate(String template, ResultSet rs, PrintWriter writer) throws SQLException {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        htmlTable(rs, pw);
        String table = sw.toString();
        String result = template.replace("%table%", table);
        writer.print(result);
    }

    static String makeTemplate(throwableConsumer<PrintWriter> consumer) throws SQLException, IOException {
        StringWriter sw = new StringWriter();
        PrintWriter writer = new PrintWriter(sw, true);
        consumer.accept(writer);
        return sw.toString();
    }
}
