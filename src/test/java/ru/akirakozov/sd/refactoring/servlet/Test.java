package ru.akirakozov.sd.refactoring.servlet;

import org.eclipse.jetty.util.thread.strategy.ProduceConsume;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.management.Query;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Optional.*;
import static org.junit.Assert.*;

public class Test {
    MockHttpServletResponse doAdd(AddProductServlet servlet, String name, long price) throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("name", name);
        request.addParameter("price", Long.toString(price));

        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.doGet(request, response);

        return response;
    }

    MockHttpServletResponse doGetProduct(GetProductsServlet servlet) throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.doGet(request, response);

        return response;
    }

    MockHttpServletResponse doQuery(QueryServlet servlet, String command) throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("command", command);
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.doGet(request, response);

        return response;
    }

    Optional<Long> parseValuePage(String header, String page) {
        assertTrue(page.contains(header));
        Pattern p = Pattern.compile("([0-9]+)");
        Matcher m = p.matcher(page);
        if (m.find()) {
            return Optional.of(Long.parseLong(m.group(1)));
        }
        return Optional.empty();
    }

    static class ProductInfo {
        ProductInfo(String name, long price) {
            this.name = name;
            this.price = price;
        }

        String name;
        long price;

        @Override
        public String toString() {
            return "ProductInfo{" +
                    "name='" + name + '\'' +
                    ", price=" + price +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ProductInfo that = (ProductInfo) o;
            return price == that.price && Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, price);
        }
    }

    List<ProductInfo> parseTablePage(String header, String page) {
        assertTrue(page.contains(header));
        Pattern p = Pattern.compile("([a-zA-Z0-9.]+)\t([0-9]+)</br>");
        Matcher m = p.matcher(page);
        List<ProductInfo> products = new ArrayList<ProductInfo>();
        while (m.find()) {
            ProductInfo product = new ProductInfo(m.group(1), Long.parseLong(m.group(2)));
            products.add(product);
        }
        return products;
    }

    static final Map<String, String> headers =
            Map.of(
                    "count", "Number of products:",
                    "sum", "Summary price: ",
                    "min", "<h1>Product with min price: </h1>",
                    "max", "<h1>Product with max price: </h1>");

    DataBase db;
    AddProductServlet add;
    GetProductsServlet getProducts;
    QueryServlet query;

    @Before
    public void setUp() throws SQLException {
        db = new DataBase("jdbc:sqlite:test.db");
        add = new AddProductServlet(db);
        getProducts = new GetProductsServlet(db);
        query = new QueryServlet(db);
        db.sqlUpdate("CREATE TABLE IF NOT EXISTS PRODUCT" +
                "(ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                " NAME           TEXT    NOT NULL, " +
                " PRICE          INT     NOT NULL)");
        db.sqlUpdate("DELETE FROM PRODUCT");
    }

    @org.junit.Test
    public void emptyDB() throws Exception {
        assertEquals(parseTablePage("", doGetProduct(getProducts).getContentAsString()), List.of());

        assertTrue(parseTablePage(headers.get("min"),
                doQuery(query, "min").getContentAsString()).isEmpty());

        assertTrue(parseTablePage(headers.get("max"),
                doQuery(query, "max").getContentAsString()).isEmpty());

        assertEquals(0, (long)parseValuePage(headers.get("sum"),
                doQuery(query, "sum").getContentAsString()).get());

        assertEquals(0, (long)parseValuePage(headers.get("count"),
                doQuery(query, "count").getContentAsString()).get());
    }

    @org.junit.Test
    public void addDifferentProducts() throws Exception {
        List<ProductInfo> products = List.of(
                new ProductInfo("p1", 10),
                new ProductInfo("p2", 12),
                new ProductInfo("p3", 14)
        );
        for (ProductInfo product : products) {
            doAdd(add, product.name, product.price);
        }
        assertEquals(parseTablePage("", doGetProduct(getProducts).getContentAsString()), products);
    }

    @org.junit.Test
    public void addProductsWithSameName() throws Exception {
        List<ProductInfo> products = List.of(
                new ProductInfo("p1", 10),
                new ProductInfo("p1", 12),
                new ProductInfo("p1", 14)
        );
        for (ProductInfo product : products) {
            doAdd(add, product.name, product.price);
        }
        assertEquals(parseTablePage("", doGetProduct(getProducts).getContentAsString()), products);
    }

    void checkValue(List<ProductInfo> values, Set<ProductInfo> exp) {
        assertEquals(values.size(), 1);
        ProductInfo value = values.get(0);
        assertTrue(exp.contains(value));
    }

    @org.junit.Test
    public void queries() throws Exception {
        List<ProductInfo> products = List.of(
                new ProductInfo("p1.1", 10),
                new ProductInfo("p1.2", 10),
                new ProductInfo("p2", 12),
                new ProductInfo("p3.1", 14),
                new ProductInfo("p3.2", 14)
        );
        for (ProductInfo product : products) {
            doAdd(add, product.name, product.price);
        }

        checkValue(
                parseTablePage(headers.get("max"), doQuery(query, "max").getContentAsString()),
                Set.of(
                        new ProductInfo("p3.1", 14),
                        new ProductInfo("p3.2", 14)));

        checkValue(
                parseTablePage(headers.get("min"), doQuery(query, "min").getContentAsString()),
                Set.of(
                        new ProductInfo("p1.1", 10),
                        new ProductInfo("p1.2", 10)));

        assertEquals(60, (long) parseValuePage(headers.get("sum"),
                doQuery(query, "sum").getContentAsString()).get());

        assertEquals(5, (long) parseValuePage(headers.get("count"),
                doQuery(query, "count").getContentAsString()).get());
    }
}

