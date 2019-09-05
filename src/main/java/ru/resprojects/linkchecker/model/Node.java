package ru.resprojects.linkchecker.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "nodes", uniqueConstraints = {
    @UniqueConstraint(columnNames = "name", name = "nodes_unique_name_idx")
})
public class Node extends AbstractNamedEntity {

    /**
     * Probability of failure graph node.
     */
    @Column(name = "probability", columnDefinition = "float default 0.5")
    private float probability;

    /**
     * The number of passes through the graph node.
     */
    @Column(name = "counter", columnDefinition = "int default 0")
    private int counter;

    /**
     * Default ctor.
     */
    public Node() {
    }

    /**
     * Ctor.
     * @param node - graph node database entity.
     */
    public Node(final Node node) {
        this(node.getId(), node.getName(), node.getProbability(), node.getCounter());
    }

    /**
     * Ctor.
     * @param name - unique name of database entity that implement graph node.
     * @param probability - probability of failure graph node.
     * @param counter - number of passes through the graph node.
     */
    public Node(final String name, final float probability, final int counter) {
        super(null, name);
        this.probability = probability;
        this.counter = counter;
    }

    /**
     * Ctor.
     * @param id - unique identity for database graph node entity.
     * @param name - unique name of database entity that implement graph node.
     * @param probability - probability of failure graph node.
     * @param counter - number of passes through the graph node.
     */
    public Node(final Integer id, final String name, final float probability, final int counter) {
        super(id, name);
        this.probability = probability;
        this.counter = counter;
    }

    /**
     * Get probability of failure graph node.
     * @return probability of failure graph node.
     */
    public float getProbability() {
        return probability;
    }

    /**
     * Set probability of failure graph node.
     * @param probability of failure graph node.
     */
    public void setProbability(final float probability) {
        this.probability = probability;
    }

    /**
     * Get number of passes through the graph node.
     * @return number of passes through the graph node.
     */
    public int getCounter() {
        return counter;
    }

    /**
     * Set number of passes through the graph node.
     * @param counter number of passes through the graph node.
     */
    public void setCounter(final int counter) {
        this.counter = counter;
    }

    @Override
    public String toString() {
        return "Node{"
            + "id=" + getId()
            + ", name='" + getName() + '\''
            + ", probability=" + probability
            + ", counter=" + counter
            + '}';
    }


}
