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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
     * Building graph in {@see <a href = https://github.com/jgrapht/jgrapht/blob/master/README.md>JGraphT</a>} format
     * from collections of the nodes dto and edges dto format.
     * @param nodesGraph collection of nodes dto {@link NodeGraph}.
     * @param edgesGraph collection of edges dto {@link EdgeGraph}.
     * @return graph in JGraphT format.
     */
    public static Graph<Node, DefaultEdge> graphBuilder(final Collection<NodeGraph> nodesGraph,
        final Collection<EdgeGraph> edgesGraph) {
        Graph<Node, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        Set<Node> nodes = new HashSet<>();
        Set<Edge> edges = new HashSet<>();
        nodesGraph.forEach(nodeGraph -> nodes.add(
            new Node(nodeGraph.getId(), nodeGraph.getName(), nodeGraph.getProbability(), 0)
        ));
        edgesGraph.forEach(edgeGraph -> {
            Optional<Node> nodeOne = nodes.stream()
                .filter(n -> n.getName()
                    .toLowerCase()
                    .equals(edgeGraph.getNodeOne().toLowerCase()))
                .findFirst();
            Optional<Node> nodeTwo = nodes.stream()
                .filter(n -> n.getName()
                    .toLowerCase()
                    .equals(edgeGraph.getNodeTwo().toLowerCase()))
                .findFirst();
            if (nodeOne.isPresent() && nodeTwo.isPresent()) {
                edges.add(new Edge(nodeOne.get(), nodeTwo.get()));
            }
        });
        nodes.forEach(graph::addVertex);
        edges.forEach(edge -> graph.addEdge(
            edge.getNodeOne(), edge.getNodeTwo()
        ));
        return graph;
    }

    /**
     * Build GraphDto object from graph models - nodes and edges.
     * @param nodes model of graph nodes, see {@link Node}.
     * @param edges model of graph edges, see {@link Edge}.
     * @return object of {@link GraphDto}.
     */
    public static GraphDto graphDtoBuilder(final Collection<Node> nodes,
        final Collection<Edge> edges) {
        return new GraphDto(nodesToNodeGraphs(nodes), edgesToEdgeGraphs(edges));
    }

    /**
     * Convert graph from {@see <a href = https://github.com/jgrapht/jgrapht/blob/master/README.md>JGraphT</a>} format
     * to graph DTO {@link GraphDto} format.
     * @param graph graph in JGraphT format.
     * @return graph in DTO format.
     */
    public static GraphDto graphToGraphDto(final Graph<Node, DefaultEdge> graph) {
        GraphDto result = new GraphDto();
        result.setNodes(getNodesDtoFromGraph(graph));
        result.setEdges(getEdgesDtoFromGraph(graph));
        return result;
    }

    /**
     * Converting JGraphT nodes to the GraphDto nodes {@link NodeGraph}
     * @param graph graph in the JGraphT format.
     * @return collection of GraphDto nodes.
     */
    public static Set<NodeGraph> getNodesDtoFromGraph(final Graph<Node, DefaultEdge> graph) {
        Set<NodeGraph> nodeGraphs = new HashSet<>();
        graph.vertexSet().forEach(node -> nodeGraphs.add(
            new NodeGraph(node.getId(), node.getName(),
                node.getProbability(), node.getCounter())
        ));
        return nodeGraphs;
    }

    /**
     * Converting JGraphT edges to the GraphDto edges {@link NodeGraph}
     * @param graph graph in the JGraphT format.
     * @return collection of GraphDto edges.
     */
    public static Set<EdgeGraph> getEdgesDtoFromGraph(final Graph<Node, DefaultEdge> graph) {
        Set<EdgeGraph> edgeGraphs = new HashSet<>();
        graph.edgeSet().forEach(edge -> edgeGraphs.add(
            new EdgeGraph(
                graph.getEdgeSource(edge).getName(),
                graph.getEdgeTarget(edge).getName()
            )
        ));
        return edgeGraphs;
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
        if (!checkGraphCycles(graph)) {
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

    public static boolean checkGraphCycles(final Graph<Node, DefaultEdge> graph) {
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
    public static List<Node> nodeGraphsToNodes(final Collection<NodeGraph> nodeGraphs) {
        return nodeGraphs.stream()
            .filter(Objects::nonNull)
            .map(GraphUtil::nodeGraphToNode)
            .collect(Collectors.toList());
    }

    /**
     * Converting {@link Node} to {@link NodeGraph}
     * @param node model object.
     * @return graph node DTO or null
     */
    public static NodeGraph nodeToNodeGraph(final Node node) {
        if (node != null) {
            return new NodeGraph(node.getId(), node.getName(),
                node.getProbability(), node.getCounter());
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
                nodeGraph.getProbability(),
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

}
