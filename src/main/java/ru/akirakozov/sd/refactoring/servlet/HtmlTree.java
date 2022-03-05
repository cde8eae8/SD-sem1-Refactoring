package ru.akirakozov.sd.refactoring.servlet;

import javax.swing.text.AbstractDocument;
import java.util.Collection;
import java.util.List;

public class HtmlTree {
    static interface IVisitor {
        enum Dir {
            In, Out
        }
        void visit(Dir dir, Html html);

        void visit(Dir dir, Body html);

        void visit(Dir dir, H1 html);

        void visit(Dir dir, Text html);

        void visit(Dir dir, LineBreak html);
    }

    static interface Element {
        public void accept(IVisitor visitor);
    }

    static abstract class ContainerElement implements Element {
        ContainerElement(Collection<Element> elements) {
            children = elements;
        }

        @Override
        public void accept(IVisitor visitor) {
            doAccept(IVisitor.Dir.In, visitor);

            for (Element child : children) {
                child.accept(visitor);
            }

            doAccept(IVisitor.Dir.Out, visitor);
        }

        protected abstract void doAccept(IVisitor.Dir dir, IVisitor visitor);

        protected Collection<Element> children;
    }

    static class Html extends ContainerElement {
        Html(Collection<Element> elements) {
            super(elements);
        }

        @Override
        public void doAccept(IVisitor.Dir dir, IVisitor visitor) { visitor.visit(dir, this); }
    }

    static class Body extends ContainerElement {
        Body(Collection<Element> elements) {
            super(elements);
        }

        @Override
        public void doAccept(IVisitor.Dir dir, IVisitor visitor) { visitor.visit(dir, this); }
    }

    static class H1 extends ContainerElement {
        H1(Collection<Element> elements) {
            super(elements);
        }

        @Override
        public void doAccept(IVisitor.Dir dir, IVisitor visitor) { visitor.visit(dir, this); }
    }

    static class Text implements Element {
        private final String text;

        Text(String t) {
            text = t;
        }
        @Override
        public void accept(IVisitor visitor) {
            visitor.visit(IVisitor.Dir.In, this);
            visitor.visit(IVisitor.Dir.Out, this);
        }

        String content() {
            return text;
        }
    }

    static class LineBreak implements Element {
        @Override
        public void accept(IVisitor visitor) {
            visitor.visit(IVisitor.Dir.In, this);
            visitor.visit(IVisitor.Dir.Out, this);
        }
    }
}
