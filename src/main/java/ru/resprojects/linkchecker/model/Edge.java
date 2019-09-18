package ru.resprojects.linkchecker.model;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ru.resprojects.linkchecker.util.Messages;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "edges", uniqueConstraints = {
    @UniqueConstraint(
        columnNames = {"nodeone", "nodetwo"},
        name = "unique_edge"
    )
})
public class Edge extends AbstractBaseEntity {

    /**
     * First object of graph node.
     */
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nodeone", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @NotNull(message = Messages.VALIDATOR_NOT_NULL_MESSAGE)
    private Node nodeOne;

    /**
     * Second object of graph node.
     */
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nodetwo", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @NotNull(message = Messages.VALIDATOR_NOT_NULL_MESSAGE)
    private Node nodeTwo;

    /**
     * Default ctor.
     */
    public Edge() {
    }

    /**
     * Ctor.
     * @param edge - object of undirected graph edge.
     */
    public Edge(final Edge edge) {
        this(edge.getId(), edge.getNodeOne(), edge.getNodeTwo());
    }

    /**
     * Ctor.
     * @param nodeOne - first object of graph node.
     * @param nodeTwo - second object of graph node.
     */
    public Edge(final Node nodeOne, final Node nodeTwo) {
        this(null, nodeOne, nodeTwo);
    }

    /**
     * Ctor.
     * @param id - unique identity for database graph edge entity.
     * @param nodeOne - first object of graph node.
     * @param nodeTwo - second object of graph node.
     */
    public Edge(final Integer id, final Node nodeOne, final Node nodeTwo) {
        this.id = id;
        this.nodeOne = nodeOne;
        this.nodeTwo = nodeTwo;
    }

    /**
     * Get first object of graph node.
     * @return first object of graph node.
     */
    public Node getNodeOne() {
        return nodeOne;
    }

    /**
     * Set first object of graph node.
     * @param nodeOne - first object of graph node.
     */
    public void setNodeOne(final Node nodeOne) {
        this.nodeOne = nodeOne;
    }

    /**
     * Get second object of graph node.
     * @return second object of graph node.
     */
    public Node getNodeTwo() {
        return nodeTwo;
    }

    /**
     * Set second object of graph node.
     * @param nodeTwo second object of graph node.
     */
    public void setNodeTwo(final Node nodeTwo) {
        this.nodeTwo = nodeTwo;
    }

    @Override
    public String toString() {
        return "Edge{"
            + "id=" + getId()
            + ", nodeOne="
            + nodeOne
            + ", nodeTwo="
            + nodeTwo
            + '}';
    }
}
