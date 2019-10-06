package ru.resprojects.linkchecker.util;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.cycle.PatonCycleBase;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.io.ComponentNameProvider;
import org.jgrapht.io.DOTExporter;
import org.jgrapht.io.GraphExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.resprojects.linkchecker.dto.GraphDto;
import ru.resprojects.linkchecker.model.Edge;
import ru.resprojects.linkchecker.model.Node;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static ru.resprojects.linkchecker.dto.GraphDto.NodeGraph;
import static ru.resprojects.linkchecker.dto.GraphDto.EdgeGraph;


/**
 * Helper class for work with graph.
 */
public final class GraphUtil {

    private static final Logger LOG = LoggerFactory.getLogger(GraphUtil.class);

    /**
     * Ctor.
     */
    private GraphUtil() {
    }

    /**
     * Building graph in <a href = https://github.com/jgrapht/jgrapht/blob/master/README.md>JGraphT</a> format
     * from collections of the nodes and edges.
     * @param nodesGraph collection of nodes {@link NodeGraph}.
     * @param edgesGraph collection of edges {@link EdgeGraph}.
     * @return graph in JGraphT format.
     */
    public static Graph<Node, DefaultEdge> graphBuilder(final Collection<NodeGraph> nodesGraph,
        final Collection<EdgeGraph> edgesGraph) {
        Graph<Node, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        Set<Node> nodes = nodeGraphsToNodes(nodesGraph);
        Set<Edge> edges = edgesGraph.stream()
            .map(eg -> {
                Node nodeOne = nodes.stream()
                    .filter(n -> n.getName().equalsIgnoreCase(eg.getNodeOne()))
                    .findFirst()
                    .orElse(null);
                Node nodeTwo = nodes.stream()
                    .filter(n -> n.getName().equalsIgnoreCase(eg.getNodeTwo()))
                    .findFirst()
                    .orElse(null);
                if (Objects.nonNull(nodeOne) && Objects.nonNull(nodeTwo)) {
                    return new Edge(eg.getId(), nodeOne, nodeTwo);
                } else {
                    return null;
                } })
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        nodes.forEach(graph::addVertex);
        edges.forEach(edge -> graph.addEdge(
            edge.getNodeOne(), edge.getNodeTwo()
        ));
        return graph;
    }

    /**
     * Convert graph from {@see <a href = https://github.com/jgrapht/jgrapht/blob/master/README.md>JGraphT</a>} format
     * to graph DTO {@link GraphDto} format.
     * @param graph graph in JGraphT format.
     * @return {@link GraphDto}.
     */
    public static GraphDto graphToGraphDto(final Graph<Node, DefaultEdge> graph) {
        return new GraphDto(getNodesDtoFromGraph(graph), getEdgesDtoFromGraph(graph));
    }

    /**
     * Converting JGraphT nodes to the GraphDto nodes {@link NodeGraph}
     * @param graph graph in the JGraphT format.
     * @return collection of GraphDto nodes.
     */
    public static Set<NodeGraph> getNodesDtoFromGraph(final Graph<Node, DefaultEdge> graph) {
        return graph.vertexSet().stream()
            .map(n -> new NodeGraph(n.getId(), n.getName(), n.getCounter()))
            .collect(Collectors.toSet());
    }

    /**
     * Converting JGraphT edges to the GraphDto edges {@link NodeGraph}
     * @param graph graph in the JGraphT format.
     * @return collection of GraphDto edges.
     */
    public static Set<EdgeGraph> getEdgesDtoFromGraph(final Graph<Node, DefaultEdge> graph) {
        return graph.edgeSet().stream()
            .map(e -> new EdgeGraph(graph.getEdgeSource(e).getName(), graph.getEdgeTarget(e).getName()))
            .collect(Collectors.toSet());
    }

    /**
     * Find a cycle basis of an undirected graph using a variant of Paton's
     * algorithm from {@see <a href = https://github.com/jgrapht/jgrapht/blob/master/README.md>JGraphT</a>} library.
     * @param graph graph in JGraphT format.
     * @return graph in JGraphT format without cycles.
     */
    public static Graph<Node, DefaultEdge> removeCyclesFromGraph(
        final Graph<Node, DefaultEdge> graph) {
        LOG.debug("Detect cycles in graph by Paton algorithm");
        if (!isGraphContainCycles(graph)) {
            LOG.debug("Cycles not found!");
            return graph;
        }
        SimpleGraph<Node, DefaultEdge> result = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addGraph(result, graph);
        while (true) {
            LOG.debug("Try detect cycles");
            PatonCycleBase<Node, DefaultEdge> patonCycleBase = new PatonCycleBase<>(result);
            Set<GraphPath<Node, DefaultEdge>> paths = patonCycleBase.getCycleBasis().getCyclesAsGraphPaths();
            Set<DefaultEdge> edgeSet = new HashSet<>();
            if (paths.size() != 0) {
                LOG.debug("Cycles found! Count of cycles in present graph = {}", paths.size());
                for (GraphPath<Node, DefaultEdge> graphPath : paths) {
                    edgeSet.addAll(graphPath.getEdgeList());
                }
                LOG.debug("Remove edge from cycle");
                if (edgeSet.iterator().hasNext()) {
                    DefaultEdge edge = edgeSet.iterator().next();
                    result.removeEdge(edge);
                }
            } else {
                LOG.debug("Cycles not found!");
                break;
            }
        }
        return result;
    }

    public static boolean isGraphContainCycles(final Graph<Node, DefaultEdge> graph) {
        SimpleGraph<Node, DefaultEdge> result = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addGraph(result, graph);
        PatonCycleBase<Node, DefaultEdge> patonCycleBase = new PatonCycleBase<>(result);
        Set<GraphPath<Node, DefaultEdge>> paths = patonCycleBase.getCycleBasis().getCyclesAsGraphPaths();
        return !paths.isEmpty();
    }

    /**
     * Export graph to {@see <a href = https://www.graphviz.org/ >GraphViz.dot</a>}
     * format.
     * @param graphTo graph in DTO format, see {@link GraphDto}.
     * @return graph in GraphViz.dot format.
     */
    public static String exportToGraphViz(final GraphDto graphTo) {
        Graph<Node, DefaultEdge> graph = graphBuilder(graphTo.getNodes(), graphTo.getEdges());
        ComponentNameProvider<Node> vertexIdProvider = component ->
            component.getName() + "_" + component.getId();
        ComponentNameProvider<Node> vertexNameProvider = Node::getName;
        GraphExporter<Node, DefaultEdge> exporter = new DOTExporter<>(
            vertexIdProvider, vertexNameProvider, null
        );
        try (Writer writer = new StringWriter()) {
            exporter.exportGraph(graph, writer);
            LOG.debug(writer.toString());
            return writer.toString();
        } catch (Exception e) {
            LOG.debug("Fail export graph to GraphViz format.");
        }
        return null;
    }

    /**
     * Converting collection of {@link Node} into set of {@link NodeGraph}
     * @param nodes node model objects collection.
     * @return set of graph node DTO
     */
    public static Set<NodeGraph> nodesToNodeGraphs(final Collection<Node> nodes) {
        return nodes.stream()
            .filter(Objects::nonNull)
            .map(GraphUtil::nodeToNodeGraph)
            .collect(Collectors.toSet());
    }

    /**
     * Converting collection of {@link NodeGraph} to the list of {@link Node}
     * @param nodeGraphs collection of graph node DTO
     * @return list of node model objects
     */
    public static Set<Node> nodeGraphsToNodes(final Collection<NodeGraph> nodeGraphs) {
        return nodeGraphs.stream()
            .filter(Objects::nonNull)
            .map(GraphUtil::nodeGraphToNode)
            .collect(Collectors.toSet());
    }

    /**
     * Converting {@link Node} to {@link NodeGraph}
     * @param node model object.
     * @return graph node DTO or null
     */
    public static NodeGraph nodeToNodeGraph(final Node node) {
        if (node != null) {
            return new NodeGraph(node.getId(), node.getName(), node.getCounter());
        } else {
            return null;
        }
    }

    /**
     * Converting {@link NodeGraph} to {@link Node}
     * @param nodeGraph graph node DTO.
     * @return model object or null
     */
    public static Node nodeGraphToNode(final NodeGraph nodeGraph) {
        if (nodeGraph != null) {
            return new Node(
                nodeGraph.getId(),
                nodeGraph.getName(),
                nodeGraph.getCounter()
            );
        } else {
            return null;
        }
    }

    /**
     * Converting collection of {@link Edge} to set of {@link EdgeGraph}
     * @param edges model objects collection.
     * @return set of graph edge DTO
     */
    public static Set<EdgeGraph> edgesToEdgeGraphs(final Collection<Edge> edges) {
        return edges.stream()
            .filter(Objects::nonNull)
            .map(GraphUtil::edgeToEdgeGraph)
            .collect(Collectors.toSet());
    }

    /**
     * Converting {@link Edge} to {@link EdgeGraph}
     * @param edge model object.
     * @return graph edge DTO or null
     */
    public static EdgeGraph edgeToEdgeGraph(final Edge edge) {
        if (edge != null) {
            return new EdgeGraph(edge.getId(), edge.getNodeOne().getName(),
                edge.getNodeTwo().getName());
        } else {
            return null;
        }
    }

    public static Set<Edge> getEdgesFromGraphDto(final GraphDto graph) {
        return graph.getEdges().stream()
            .map(eg -> {
                Node nodeOne = nodeGraphToNode(graph.getNodes().stream()
                    .filter(ng -> ng.getName().equalsIgnoreCase(eg.getNodeOne()))
                    .findFirst()
                    .orElse(null));
                Node nodeTwo = nodeGraphToNode(graph.getNodes().stream()
                    .filter(ng -> ng.getName().equalsIgnoreCase(eg.getNodeTwo()))
                    .findFirst()
                    .orElse(null));
                if (Objects.nonNull(nodeOne) && Objects.nonNull(nodeTwo)) {
                    return new Edge(eg.getId(), nodeOne, nodeTwo);
                } else {
                    return null;
                } })
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    /**
     * Get a pseudo random event with a given probability.
     * @param probability of event
     * @return true if pseudo random number less than probability.
     */
    public static boolean getRandomEvent(int probability) {
        if (probability == 100) {
            return true;
        }
        if (probability == 0) {
            return false;
        }
        return ThreadLocalRandom.current().nextInt(100) < probability;
    }

}
