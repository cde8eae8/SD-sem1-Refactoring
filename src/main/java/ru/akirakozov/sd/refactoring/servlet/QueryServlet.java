package ru.akirakozov.sd.refactoring.servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.text.html.Option;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.*;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String command = request.getParameter("command");

        throwableConsumer<String> minMax = (String cmd) -> {
            Function<List<HtmlTree.Element>, HtmlTree.Element> template =
                    (List<HtmlTree.Element> elements) ->
                            Utils.page(Stream.concat(
                                    Stream.of(
                                        new HtmlTree.H1(List.of(
                                            new HtmlTree.Text(String.format("Product with %s price: ", cmd))
                                        ))),
                                    elements.stream()).collect(Collectors.toList())
                            );

            dataBase.sqlRequest(
                    String.format("SELECT * FROM PRODUCT ORDER BY PRICE %s LIMIT 1",
                            cmd.equals("max") ? "DESC" : ""),
                    (ResultSet rs) -> {
                        HtmlTree.Element page = template.apply(htmlTable(rs));
                        HtmlPrinter printer = new HtmlPrinter(response.getWriter());
                        page.accept(printer);
                    });
        };

        throwableConsumer<String> sum = (String cmd) -> {
            Function<Optional<HtmlTree.Element>, HtmlTree.Element> template =
                    (Optional<HtmlTree.Element> value) ->
                            Utils.page(Stream.concat(
                                Stream.of(new HtmlTree.Text("Summary price: ")),
                                value.stream())
                                    .collect(Collectors.toList())
                            );

            dataBase.sqlRequest(
                    "SELECT SUM(price) FROM PRODUCT",
                    (ResultSet rs) -> {
                        Optional<Integer> value = rs.next() ? Optional.of(rs.getInt(1)) : Optional.empty();
                        HtmlTree.Element page = template.apply(value.map((v) -> new HtmlTree.Text(Integer.toString(v))));
                        HtmlPrinter printer = new HtmlPrinter(response.getWriter());
                        page.accept(printer);
                    });
        };

        throwableConsumer<String> count = (String cmd) -> {
            Function<Optional<HtmlTree.Element>, HtmlTree.Element> template =
                    (Optional<HtmlTree.Element> value) ->
                            Utils.page(Stream.concat(
                                            Stream.of(new HtmlTree.Text("Number of products: ")),
                                            value.stream())
                                    .collect(Collectors.toList())
                            );

            dataBase.sqlRequest(
                    "SELECT COUNT(*) FROM PRODUCT",
                    (ResultSet rs) -> {
                        Optional<Integer> value = rs.next() ? Optional.of(rs.getInt(1)) : Optional.empty();
                        HtmlTree.Element page = template.apply(value.map((v) -> new HtmlTree.Text(Integer.toString(v))));
                        HtmlPrinter printer = new HtmlPrinter(response.getWriter());
                        page.accept(printer);
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
