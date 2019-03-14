package ru.resprojects.linkchecker.model;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "edges")
public class Edge {

    private Node node1;
    private Node node2;

    public Node getNode1() {
        return node1;
    }

    public void setNode1(Node node1) {
        this.node1 = node1;
    }

    public Node getNode2() {
        return node2;
    }

    public void setNode2(Node node2) {
        this.node2 = node2;
    }

    @Override
    public String toString() {
        return "Edge{" +
            "node1=" + node1 +
            ", node2=" + node2 +
            '}';
    }
}
