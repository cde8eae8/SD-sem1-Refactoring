package ru.akirakozov.sd.refactoring.servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

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
                        String template = makeTemplate((PrintWriter writer) -> {
                                    writer.println("<html><body>");
                                    writer.println("%table%");
                                    writer.println("</body></html>");
                                });
                        Utils.fromTemplate(template, rs, response.getWriter());
                    });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
