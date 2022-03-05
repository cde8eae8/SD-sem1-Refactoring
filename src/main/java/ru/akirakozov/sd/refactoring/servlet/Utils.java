package ru.akirakozov.sd.refactoring.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class Utils {
    @FunctionalInterface
    public interface throwableConsumer<T> {
        void accept(T t) throws IOException, SQLException;
    }

    static HtmlTree.Element page(Collection<HtmlTree.Element> content) {
        return new HtmlTree.Html(List.of(new HtmlTree.Body(content)));
    }

    static List<HtmlTree.Element> htmlTable(ResultSet rs) throws SQLException {
        List<HtmlTree.Element> elements = new ArrayList<HtmlTree.Element>();
        while (rs.next()) {
            String  name = rs.getString("name");
            int price  = rs.getInt("price");
            elements.add(new HtmlTree.Text(name + "\t" + price));
            elements.add(new HtmlTree.LineBreak());
        }
        return elements;
    }
}
