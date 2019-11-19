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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static ru.resprojects.linkchecker.dto.GraphDto.NodeGraph;
import static ru.resprojects.linkchecker.dto.GraphDto.EdgeGraph;


/**
 * Helper class for work with graph.
 */
public class GraphUtil {

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
        if (Objects.isNull(nodesGraph) || Objects.isNull(edgesGraph)) {
            LOG.debug("graphBuilder: Return empty graph because one of the input collection is null");
            return graph;
        }
        Set<Node> nodes = nodeGraphsToNodes(nodesGraph);
        Set<Edge> edges = edgesGraph.stream()
            .filter(Objects::nonNull)
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
     * ATTENTION: GraphDto will return edges without IDs,
     * because graph in JGraphT format does not store IDs for edges!
     * @param graph graph in JGraphT format.
     * @return {@link GraphDto}.
     */
    public static GraphDto graphToGraphDto(final Graph<Node, DefaultEdge> graph) {
        if (Objects.isNull(graph)) {
            LOG.debug("graphToGraphDto: returned empty DTO graph because input graph is null.");
            return new GraphDto();
        }
        return new GraphDto(getNodesDtoFromGraph(graph), getEdgesDtoFromGraph(graph));
    }

    /**
     * Converting JGraphT nodes to the GraphDto nodes {@link NodeGraph}
     * @param graph graph in the JGraphT format.
     * @return collection of GraphDto nodes.
     */
    private static Set<NodeGraph> getNodesDtoFromGraph(final Graph<Node, DefaultEdge> graph) {
        if (Objects.isNull(graph)) {
            LOG.debug("getNodesDtoFromGraph: returned empty collection, because input graph is null");
            return new HashSet<>();
        }
        return graph.vertexSet().stream()
            .map(n -> new NodeGraph(n.getId(), n.getName(), n.getCounter()))
            .collect(Collectors.toSet());
    }

    /**
     * Converting JGraphT edges to the GraphDto edges {@link NodeGraph}
     * @param graph graph in the JGraphT format.
     * @return collection of GraphDto edges without IDs.
     */
    private static Set<EdgeGraph> getEdgesDtoFromGraph(final Graph<Node, DefaultEdge> graph) {
        if (Objects.isNull(graph)) {
            LOG.debug("getEdgesDtoFromGraph: returned empty collection, because input graph is null");
            return new HashSet<>();
        }
        return graph.edgeSet().stream()
            .map(e -> new EdgeGraph(graph.getEdgeSource(e).getName(), graph.getEdgeTarget(e).getName()))
            .collect(Collectors.toSet());
    }

    /**
     * Find a cycle basis of an undirected graph using a variant of Paton's
     * algorithm from {@see <a href = https://github.com/jgrapht/jgrapht/blob/master/README.md>JGraphT</a>} library.
     * NOTE: while removing cycles algorithm each time returns a random set of
     * graph edges for the same graph.
     * @param graph graph in JGraphT format.
     * @return graph in JGraphT format without cycles.
     */
    public static Graph<Node, DefaultEdge> removeCyclesFromGraph(
        final Graph<Node, DefaultEdge> graph) {
        LOG.debug("removeCyclesFromGraph: Detect cycles in graph by Paton algorithm");
        if (!isGraphContainCycles(graph)) {
            LOG.debug("removeCyclesFromGraph: Cycles not found!");
            return graph;
        }
        SimpleGraph<Node, DefaultEdge> result = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addGraph(result, graph);
        while (true) {
            LOG.debug("removeCyclesFromGraph: Try detect cycles");
            PatonCycleBase<Node, DefaultEdge> patonCycleBase = new PatonCycleBase<>(result);
            Set<GraphPath<Node, DefaultEdge>> paths = patonCycleBase.getCycleBasis().getCyclesAsGraphPaths();
            Set<DefaultEdge> edgeSet = new HashSet<>();
            if (paths.size() != 0) {
                LOG.debug("removeCyclesFromGraph: Cycles found! Count of cycles in present graph = {}", paths.size());
                for (GraphPath<Node, DefaultEdge> graphPath : paths) {
                    edgeSet.addAll(graphPath.getEdgeList());
                }
                LOG.debug("removeCyclesFromGraph: Remove edge from cycle");
                if (edgeSet.iterator().hasNext()) {
                    DefaultEdge edge = edgeSet.iterator().next();
                    result.removeEdge(edge);
                }
            } else {
                LOG.debug("removeCyclesFromGraph: Cycles not found!");
                break;
            }
        }
        return result;
    }

    private static boolean isGraphContainCycles(final Graph<Node, DefaultEdge> graph) {
        SimpleGraph<Node, DefaultEdge> result = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addGraph(result, graph);
        PatonCycleBase<Node, DefaultEdge> patonCycleBase = new PatonCycleBase<>(result);
        Set<GraphPath<Node, DefaultEdge>> paths = patonCycleBase.getCycleBasis()
            .getCyclesAsGraphPaths();
        return !paths.isEmpty();
    }

    /**
     * Export graph to {@see <a href = https://www.graphviz.org/ >GraphViz.dot</a>}
     * format.
     * @param graphTo graph in DTO format, see {@link GraphDto}.
     * @return graph in GraphViz.dot format.
     */
    public static String exportToGraphViz(final GraphDto graphTo) {
        if (Objects.isNull(graphTo)) return "";
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
            LOG.warn("Fail export graph to GraphViz format.", e);
        }
        return "";
    }

    /**
     * Converting collection of {@link Node} into set of {@link NodeGraph}
     * @param nodes node model objects collection.
     * @return collection of graph node DTO or empty collection if nodes is null.
     */
    public static Set<NodeGraph> nodesToNodeGraphs(final Collection<Node> nodes) {
        if (Objects.isNull(nodes)) {
            LOG.debug("nodesToNodeGraphs: return empty collection because input collection of nodes is null");
            return new HashSet<>();
        }
        return nodes.stream()
            .filter(Objects::nonNull)
            .map(GraphUtil::nodeToNodeGraph)
            .collect(Collectors.toSet());
    }

    /**
     * Converting collection of {@link NodeGraph} to collection of {@link Node}
     * @param nodeGraphs collection of graph node DTO
     * @return collection of node model objects or empty collection if
     * nodeGraphs is null.
     */
    public static Set<Node> nodeGraphsToNodes(final Collection<NodeGraph> nodeGraphs) {
        if (Objects.isNull(nodeGraphs)) {
            LOG.debug("nodeGraphsToNodes: return empty collection because input collection of DTO nodes is null");
            return new HashSet<>();
        }
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
            return new NodeGraph(
                node.getId(),
                node.getName(),
                node.getCounter()
            );
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
     * Converting collection of {@link Edge} to collection of {@link EdgeGraph}
     * @param edges model objects collection.
     * @return collection of graph edge DTO or empty collection if param is null.
     */
    public static Set<EdgeGraph> edgesToEdgeGraphs(final Collection<Edge> edges) {
        if (Objects.isNull(edges)) {
            LOG.debug("edgesToEdgeGraphs: return empty collection because input collection of edges is null");
            return new HashSet<>();
        }
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

    /**
     * Getting graph edges {@link Edge} from graph data transfer object {@link GraphDto}
     * @param graph graph data transfer object
     * @return collection of graph edges.
     */
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
     * Node random failure generator.
     * @param nodes node graph collection
     * @return map with key as node graph name and value as fault of node graph
     * state or null if node graph collection is empty or null.
     */
    public static Map<String, Boolean> getRandomNodeFault(Collection<NodeGraph> nodes) {
        LOG.debug("getRandomNodeFault: Generating random node fault");
        Map<String, Boolean> result = new HashMap<>();
        if (Objects.isNull(nodes) || nodes.isEmpty()) {
            return result;
        }
        nodes.forEach(nodeGraph -> {
            boolean isFault = ThreadLocalRandom.current().nextInt(100) >= 90;
            result.put(nodeGraph.getName(), isFault);
        });
        LOG.debug("getRandomNodeFault: result = " + result.toString());
        return result;
    }

}
