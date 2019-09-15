package ru.resprojects.linkchecker.model;

import org.hibernate.validator.constraints.Range;

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

    private static final String DEFAULT_PROBABILITY_VALUE = "int default " +
        NODE_PROBABILITY_DEFAULT;
    private static final String DEFAULT_COUNTER_VALUE = "int default " +
        NODE_COUNTER_DEFAULT;

    /**
     * Percentage value of probability failure node of the graph.
     */
    @Column(name = "probability", columnDefinition = DEFAULT_PROBABILITY_VALUE)
    @Range(min = MIN_PROBABILITY_VALUE,
        max = MAX_PROBABILITY_VALUE,
        message = VALIDATOR_NODE_PROBABILITY_RANGE_MESSAGE)
    private int probability;

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
        this(node.getId(), node.getName(), node.getProbability(), node.getCounter());
    }

    /**
     * Ctor.
     * @param name - unique name of database entity that implement graph node.
     */
    public Node(final String name) {
        super(null, name);
        this.probability = NODE_PROBABILITY_DEFAULT;
        this.counter = NODE_COUNTER_DEFAULT;
    }

    /**
     * Ctor.
     * @param name - unique name of database entity that implement graph node.
     * @param probability - percentage value of probability failure node of the graph.
     */
    public Node(final String name, final int probability) {
        super(null, name);
        this.probability = probability;
        this.counter = NODE_COUNTER_DEFAULT;
    }

    /**
     * Ctor.
     * @param name - unique name of database entity that implement graph node.
     * @param probability - percentage value of probability failure node of the graph.
     * @param counter - number of passes through the node of the graph.
     */
    public Node(final String name, final int probability, final int counter) {
        super(null, name);
        this.probability = probability;
        this.counter = counter;
    }

    /**
     * Ctor.
     * @param id - unique identity for database graph node entity.
     * @param name - unique name of database entity that implement graph node.
     * @param probability - percentage value of probability failure node of the graph.
     * @param counter - number of passes through the node of the graph.
     */
    public Node(final Integer id, final String name, final int probability, final int counter) {
        super(id, name);
        this.probability = probability;
        this.counter = counter;
    }

    /**
     * Get percentage value of probability failure node of the graph.
     * @return probability of failure graph node.
     */
    public int getProbability() {
        return probability;
    }

    /**
     * Set percentage value of probability failure node of the graph.
     * @param probability of failure graph node.
     */
    public void setProbability(final int probability) {
        this.probability = probability;
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
            + ", probability=" + probability
            + ", counter=" + counter
            + '}';
    }


}
