package ru.akirakozov.sd.refactoring.servlet;

import java.io.PrintWriter;

public class HtmlPrinter implements HtmlTree.IVisitor {
    private final PrintWriter writer;

    HtmlPrinter(PrintWriter w) {
        writer = w;
    }

    @Override
    public void visit(Dir dir, HtmlTree.Html html) {
        tag(dir, "head");
    }

    @Override
    public void visit(Dir dir, HtmlTree.Body html) {
        tag(dir, "body");
    }

    @Override
    public void visit(Dir dir, HtmlTree.H1 html) {
        tag(dir, "h1");
    }

    @Override
    public void visit(Dir dir, HtmlTree.Text text) {
        if (dir.equals(Dir.In)) {
            writer.print(text.content());
        }
    }

    @Override
    public void visit(Dir dir, HtmlTree.LineBreak html) {
        if (dir.equals(Dir.In)) {
            writer.println("</br>");
        }
    }

    private void tag(Dir dir, String tag) {
        if (dir.equals(Dir.In)) {
            writer.println("<" + tag + ">");
        } else {
            writer.println("</" + tag + ">");
        }
    }
}
