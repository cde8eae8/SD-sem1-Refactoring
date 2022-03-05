package ru.akirakozov.sd.refactoring.servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.akirakozov.sd.refactoring.servlet.Utils.*;

/**
 * @author akirakozov
 */
public class QueryServlet extends HttpServlet {
    private final DataBase dataBase;

    public QueryServlet(DataBase db) {
        dataBase = db;
    }

    private static HtmlTree.Element minMaxTemplate(String cmd, List<HtmlTree.Element> elements) {
        return Utils.page(Stream.concat(
                Stream.of(
                        new HtmlTree.H1(List.of(
                                new HtmlTree.Text(String.format("Product with %s price: ", cmd))
                        ))),
                elements.stream()).collect(Collectors.toList())
        );
    }

    private static HtmlTree.Element valuePageTemplate(String header, Optional<HtmlTree.Element> value) {
        return Utils.page(Stream.concat(
                        Stream.of(new HtmlTree.Text(header)),
                        value.stream())
                .collect(Collectors.toList())
        );
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String command = request.getParameter("command");

        throwableConsumer<String> minMax = (String cmd) ->
            dataBase.sqlRequest(
                    String.format("SELECT * FROM PRODUCT ORDER BY PRICE %s LIMIT 1", cmd.equals("max") ? "DESC" : ""),
                    (ResultSet rs) -> Utils.makePage(minMaxTemplate(cmd, htmlTable(rs)), response.getWriter()));

        throwableConsumer<String> sum = (String cmd) ->
            dataBase.sqlRequest(
                    "SELECT SUM(price) FROM PRODUCT",
                    (ResultSet rs) -> {
                        Optional<Integer> value = rs.next() ? Optional.of(rs.getInt(1)) : Optional.empty();
                        Optional<HtmlTree.Element> valueElement = value.map(v -> new HtmlTree.Text(Integer.toString(v)));
                        Utils.makePage(valuePageTemplate("Summary price: ", valueElement), response.getWriter());
                    });

        throwableConsumer<String> count = (String cmd) ->
            dataBase.sqlRequest(
                    "SELECT COUNT(*) FROM PRODUCT",
                    (ResultSet rs) -> {
                        Optional<Integer> value = rs.next() ? Optional.of(rs.getInt(1)) : Optional.empty();
                        Optional<HtmlTree.Element> valueElement = value.map(v -> new HtmlTree.Text(Integer.toString(v)));
                        Utils.makePage(valuePageTemplate("Number of products: ", valueElement), response.getWriter());
                    });

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
