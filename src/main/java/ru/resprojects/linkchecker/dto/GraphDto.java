package ru.resprojects.linkchecker.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static ru.resprojects.linkchecker.util.ValidationUtil.*;
import static ru.resprojects.linkchecker.util.Messages.*;

/**
 * Class for transfer object that implements graph.
 */
public class GraphDto {

    /**
     * Class for transfer object that implements graph node.
     */
    public static class NodeGraph extends BaseDto {

        /**
         * Unique graph node name.
         */
        @NotBlank(message = VALIDATOR_NODE_NOT_BLANK_NAME_MESSAGE)
        @Size(min = MIN_NAME_SIZE, max = MAX_NAME_SIZE,
            message = VALIDATOR_NODE_NAME_RANGE_MESSAGE)
        private String name;

        /**
         * The number of passes through the graph node.
         */
        private int counter;

        /**
         * Default ctor.
         */
        public NodeGraph() {
        }

        /**
         * Ctor.
         * @param name - unique graph node name.
         */
        public NodeGraph(final String name) {
            this(null, name, NODE_COUNTER_DEFAULT);
        }

        /**
         * Ctor.
         * @param id - identity number of graph node.
         * @param name - unique graph node name.
         * @param counter - the number of passes through the current node.
         */
        public NodeGraph(final Integer id, final String name, final int counter) {
            super(id);
            this.name = name;
            this.counter = counter;
        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public int getCounter() {
            return counter;
        }

        public void setCounter(final int counter) {
            this.counter = counter;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            NodeGraph nodeGraph = (NodeGraph) o;
            return Objects.equals(id, nodeGraph.id)
                && counter == nodeGraph.counter
                && name.equals(nodeGraph.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name, counter);
        }

        @Override
        public String toString() {
            return "{"
                + "\"id\": \"" + id + '"'
                + ", \"name\": \"" + name + '"'
                + ", \"counter\":" + counter
                + '}';
        }
    }

    /**
     * Class for transport object that implements undirected graph edge.
     */
    public static class EdgeGraph extends BaseDto {

        /**
         * Unique name of first graph node.
         */
        @NotBlank
        @Size(min = MIN_NAME_SIZE, max = MAX_NAME_SIZE)
        private String nodeOne;

        /**
         * Unique name of second graph node.
         */
        @NotBlank
        @Size(min = MIN_NAME_SIZE, max = MAX_NAME_SIZE)
        private String nodeTwo;

        /**
         * Default ctor.
         */
        public EdgeGraph() {
        }

        /**
         * Ctor.
         * @param edge - object of undirected graph edge.
         */
        public EdgeGraph(final EdgeGraph edge) {
            this(edge.getId(), edge.getNodeOne(), edge.getNodeTwo());
        }


        /**
         * Ctor.
         * @param nodeOne - unique name of first graph node.
         * @param nodeTwo - unique name of second graph node.
         */
        public EdgeGraph(final String nodeOne, final String nodeTwo) {
            this(null, nodeOne, nodeTwo);
        }

        /**
         * Ctor.
         * @param nodeOne - object of first graph node.
         * @param nodeTwo - object of second graph node.
         */
        public EdgeGraph(final NodeGraph nodeOne, final NodeGraph nodeTwo) {
            this(null, nodeOne.getName(), nodeTwo.getName());
        }

        /**
         * Ctor.
         * @param id - identity number of graph edge.
         * @param nodeOne - unique name of first graph node.
         * @param nodeTwo - unique name of second graph node.
         */
        public EdgeGraph(final Integer id, final String nodeOne, final String nodeTwo) {
            super(id);
            this.nodeOne = nodeOne;
            this.nodeTwo = nodeTwo;
        }

        /**
         * Ctor.
         * @param id - identity number of graph edge.
         * @param nodeOne - object of first graph node.
         * @param nodeTwo - object of second graph node.
         */
        public EdgeGraph(final Integer id, final NodeGraph nodeOne, final NodeGraph nodeTwo) {
            super(id);
            this.nodeOne = nodeOne.getName();
            this.nodeTwo = nodeTwo.getName();
        }


        public String getNodeOne() {
            return nodeOne;
        }


        public void setNodeOne(final String nodeOne) {
            this.nodeOne = nodeOne;
        }

        public String getNodeTwo() {
            return nodeTwo;
        }

        public void setNodeTwo(final String nodeTwo) {
            this.nodeTwo = nodeTwo;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EdgeGraph edgeGraph = (EdgeGraph) o;
            return nodeOne.equals(edgeGraph.nodeOne)
                && nodeTwo.equals(edgeGraph.nodeTwo)
                && Objects.equals(id, edgeGraph.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, nodeOne, nodeTwo);
        }

        @Override
        public String toString() {
            return "{"
                + "\"id\": \"" + id + '"'
                + ", \"nodeOne\": \""
                + nodeOne + '"'
                + ", \"nodeTwo\": \""
                + nodeTwo + '"'
                + '}';
        }
    }

    /**
     * Collection of graph nodes.
     */
    private Set<NodeGraph> nodes = new HashSet<>();

    /**
     * Collection of graph edges.
     */
    private Set<EdgeGraph> edges = new HashSet<>();

    /**
     * Default ctor.
     */
    public GraphDto() {
    }

    /**
     * Ctor.
     * @param nodes - collection of graph nodes {@link NodeGraph}.
     * @param edges - collection of graph edges {@link EdgeGraph}.
     */
    public GraphDto(final Set<NodeGraph> nodes, final Set<EdgeGraph> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public Set<NodeGraph> getNodes() {
        return nodes;
    }

    public void setNodes(final Set<NodeGraph> nodes) {
        this.nodes = nodes;
    }


    public Set<EdgeGraph> getEdges() {
        return edges;
    }

    public void setEdges(final Set<EdgeGraph> edges) {
        this.edges = edges;
    }

    @Override
    public final String toString() {
        return "{"
            + "\"nodes\": "
            + nodes
            + ", \"edges\": "
            + edges
            + '}';
    }


}
