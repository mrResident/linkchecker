package ru.resprojects.linkchecker.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import static ru.resprojects.linkchecker.util.ValidationUtil.*;

@Entity
@Table(name = "nodes", uniqueConstraints = {
    @UniqueConstraint(columnNames = "name", name = "nodes_unique_name_idx")
})
public class Node extends AbstractNamedEntity {

    private static final String DEFAULT_COUNTER_VALUE = "int default " +
        NODE_COUNTER_DEFAULT;

    /**
     * The number of passes through the node of the graph.
     */
    @Column(name = "counter", columnDefinition = DEFAULT_COUNTER_VALUE)
    private int counter;

    /**
     * Default ctor.
     */
    public Node() {
    }

    /**
     * Ctor.
     * @param node - node of the graph database entity.
     */
    public Node(final Node node) {
        this(node.getId(), node.getName(), node.getCounter());
    }

    /**
     * Ctor.
     * @param name - unique name of database entity that implement graph node.
     */
    public Node(final String name) {
        super(null, name);
        this.counter = NODE_COUNTER_DEFAULT;
    }

    /**
     * Ctor.
     * @param id - unique identity for database graph node entity.
     * @param name - unique name of database entity that implement graph node.
     * @param counter - number of passes through the node of the graph.
     */
    public Node(final Integer id, final String name, final int counter) {
        super(id, name);
        this.counter = counter;
    }

    /**
     * Get number of passes through the node of the graph.
     * @return number of passes through the node of the graph.
     */
    public int getCounter() {
        return counter;
    }

    /**
     * Set number of passes through the node of the graph.
     * @param counter number of passes through the node of the graph.
     */
    public void setCounter(final int counter) {
        this.counter = counter;
    }

    @Override
    public String toString() {
        return "Node{"
            + "id=" + getId()
            + ", name='" + getName() + '\''
            + ", counter=" + counter
            + '}';
    }


}
