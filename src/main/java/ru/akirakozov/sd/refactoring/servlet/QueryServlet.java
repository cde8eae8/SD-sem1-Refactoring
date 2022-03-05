package ru.akirakozov.sd.refactoring.servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static ru.akirakozov.sd.refactoring.servlet.Utils.*;

/**
 * @author akirakozov
 */
public class QueryServlet extends HttpServlet {
    private final DataBase dataBase;

    public QueryServlet(DataBase db) {
        dataBase = db;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String command = request.getParameter("command");

        throwableConsumer<String> minMax = (String cmd) -> {
            String template = makeTemplate((PrintWriter writer) -> {
                        writer.println("<html><body>");
                        writer.println(String.format("<h1>Product with %s price: </h1>", cmd));
                        writer.println("%table%");
                        writer.println("</body></html>");
                    });

            dataBase.sqlRequest(
                    String.format("SELECT * FROM PRODUCT ORDER BY PRICE %s LIMIT 1",
                            cmd.equals("max") ? "DESC" : ""),
                    (ResultSet rs) -> Utils.fromTemplate(template, rs, response.getWriter()));
        };

        throwableConsumer<String> sum = (String cmd) -> {
            String template = makeTemplate((PrintWriter writer) -> {
                writer.println("<html><body>");
                writer.println("Summary price: ");
                writer.println("%value%");
                writer.println("</body></html>");
            });

            dataBase.sqlRequest(
                    "SELECT SUM(price) FROM PRODUCT",
                    (ResultSet rs) -> Utils.fromTemplate(
                            template,
                            rs.next() ? Optional.of(rs.getInt(1)) : Optional.empty(),
                            response.getWriter()));
        };

        throwableConsumer<String> count = (String cmd) -> {
            String template = makeTemplate((PrintWriter writer) -> {
                writer.println("<html><body>");
                writer.println("Number of products: ");
                writer.println("%value%");
                writer.println("</body></html>");
            });

            dataBase.sqlRequest(
                    "SELECT COUNT(*) FROM PRODUCT",
                    (ResultSet rs) -> {
                        Utils.fromTemplate(
                                template,
                                rs.next() ? Optional.of(rs.getInt(1)) : Optional.empty(),
                                response.getWriter());
                    });
        };

        Map<String, throwableConsumer<String>> functions = Map.of(
                "min", minMax,
                "max", minMax,
                "sum", sum,
                "count", count
        );

        if (functions.containsKey(command)) {
            try {
                functions.get(command).accept(command);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            response.getWriter().println("Unknown command: " + command);
        }

        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
    }

}
