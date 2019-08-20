import groovy.transform.CompileStatic
import groovy.transform.CompileDynamic

@CompileStatic
interface Visitor {
    void visit(NodeType1 n1)
    void visit(NodeType2 n2)
}

@CompileStatic
interface Visitable {
    void accept(Visitor visitor)
}

@CompileStatic
class NodeType1 implements Visitable {
    Visitable[] children = new Visitable[0]
    void accept(Visitor visitor) {
        visitor.visit(this)
        for(int i = 0; i < children.length; ++i) {
            children[i].accept(visitor)
        }
    }
}

@CompileStatic
class NodeType2 implements Visitable {
    Visitable[] children = new Visitable[0]
    void accept(Visitor visitor) {
        visitor.visit(this)
        for(int i = 0; i < children.length; ++i) {
            children[i].accept(visitor)
        }
    }
}

@CompileStatic
class NodeType1Counter implements Visitor {
    int count = 0
    void visit(NodeType1 n1) {
        count++
    }
    void visit(NodeType2 n2){
        count += 2
    }
}

NodeType1 root = new NodeType1()
root.children = new Visitable[2]
root.children[0] = new NodeType1()
root.children[1] = new NodeType2()

def visitor = new NodeType1Counter()
root.accept(visitor)
println visitor.dump()

