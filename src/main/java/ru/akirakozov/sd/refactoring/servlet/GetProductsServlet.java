package ru.akirakozov.sd.refactoring.servlet;

import org.w3c.dom.html.HTMLElement;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static ru.akirakozov.sd.refactoring.servlet.Utils.*;

/**
 * @author akirakozov
 */
public class GetProductsServlet extends HttpServlet {
    private final DataBase dataBase;

    public GetProductsServlet(DataBase db) {
        dataBase = db;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            dataBase.sqlRequest("SELECT * FROM PRODUCT",
                    (ResultSet rs) -> {
                        Function<List<HtmlTree.Element>, HtmlTree.Element> template =
                                (List<HtmlTree.Element> el) -> new HtmlTree.Html(List.of(new HtmlTree.Body(el)));
                        HtmlTree.Element page = template.apply(htmlTable(rs));
                        HtmlPrinter printer = new HtmlPrinter(response.getWriter());
                        page.accept(printer);
                    });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
