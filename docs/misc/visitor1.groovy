import groovy.transform.CompileStatic
import groovy.transform.CompileDynamic

@CompileStatic
abstract class Shape {
    void accept(Closure yield) { yield(this) }
}

@CompileStatic
interface Visitor {
    void visit_rectangle(Rectangle n1)
    void visit_line(Line n2)
    void visit_group(Group n3)
}

@CompileStatic
interface Visitable {
    void accept(Visitor visitor)
}

@CompileStatic
// class Rectangle extends Shape {
class Rectangle extends Shape implements Visitable {
    int x, y, width, height

    Rectangle(int x, int y, int width, int height) {
        this.x = x; this.y = y; this.width = width; this.height = height
    }

    Rectangle union(Rectangle rect) {
        if (!rect) return this
        int minx = [rect.x, x].min()
        int maxx = [rect.x + width, x + width].max()
        int miny = [rect.y, y].min()
        int maxy = [rect.y + height, y + height].max()
        new Rectangle(minx, miny, maxx - minx, maxy - miny)
    }

    @CompileDynamic
    void accept(Visitor visitor) {
    // def accept(Visitor visitor) {
        visitor.visit_rectangle(this)
    }
}

@CompileStatic
class Line extends Shape implements Visitable {
    int x1, y1, x2, y2

    Line(int x1, int y1, int x2, int y2) {
        this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2
    }

    @CompileDynamic
    void accept(Visitor visitor){
        visitor.visit_line(this)
    }
}

@CompileStatic
class Group extends Shape implements Visitable {
    List<Shape> shapes = []
    def add(Shape shape) { shapes += shape }
    def remove(Shape shape) { shapes -= shape }

    @CompileDynamic
    void accept(Visitor visitor) {
        visitor.visit_group(this)
    }
}

@CompileStatic
class BoundingRectangleVisitor implements Visitor {
    Rectangle bounds

    void visit_rectangle(Rectangle rectangle) {
        if (bounds)
            bounds = bounds.union(rectangle)
        else
            bounds = rectangle
    }

    void visit_line(Line line) {
        Rectangle line_bounds = new Rectangle(line.x1, line.y1, line.x2-line.y1, line.x2-line.y2)
        if (bounds)
            bounds = bounds.union(line_bounds)
        else
            bounds = line_bounds
    }

    @CompileDynamic
    void visit_group(Group group) {
        group.shapes.each { shape -> shape.accept(this) }
    }
}

def group = new Group()
group.add(new Rectangle(100, 40, 10, 5))
group.add(new Rectangle(100, 70, 10, 5))
group.add(new Line(90, 30, 60, 5))
def visitor = new BoundingRectangleVisitor()
group.accept(visitor)
bounding_box = visitor.bounds
println bounding_box.dump()
