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
import java.util.Optional;
import java.util.Set;

import static ru.resprojects.linkchecker.dto.GraphDto.NodeGraph;
import static ru.resprojects.linkchecker.dto.GraphDto.EdgeGraph;


/**
 * Helper class for work with graph.
 */
public final class GraphUtil {

    /**
     * Logging.
     */
    private static final Logger LOG = LoggerFactory.getLogger(GraphUtil.class);

    /**
     * Ctor.
     */
    private GraphUtil() {
    }

    /**
     * Convert graph from DTO format {@link GraphDto} to graph in {@see <a href = https://github.com/jgrapht/jgrapht/blob/master/README.md>JGraphT</a>}
     * format.
     * @param graphTo graph in DTO format.
     * @return graph in JGraphT format.
     */
    public static Graph<Node, DefaultEdge> graphDtoToGraph(final GraphDto graphTo) {
        Graph<Node, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        Set<Node> nodes = new HashSet<>();
        Set<Edge> edges = new HashSet<>();
        graphTo.getNodes().forEach(nodeGraph -> nodes.add(
            new Node(nodeGraph.getId(), nodeGraph.getName(), nodeGraph.getProbability(), 0)
        ));
        graphTo.getEdges().forEach(edgeGraph -> {
            Optional<Node> node1 = nodes.stream()
                .filter(n -> n.getName()
                    .toLowerCase()
                    .equals(edgeGraph.getNodeOne().toLowerCase()))
                .findFirst();
            Optional<Node> node2 = nodes.stream()
                .filter(n -> n.getName()
                    .toLowerCase()
                    .equals(edgeGraph.getNodeTwo().toLowerCase()))
                .findFirst();
            if (node1.isPresent() && node2.isPresent()) {
                edges.add(new Edge(node1.get(), node2.get()));
            }
        });
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
     * @return graph in DTO format.
     */
    public static GraphDto graphToGraphDto(final Graph<Node, DefaultEdge> graph) {
        GraphDto result = new GraphDto();
        Set<NodeGraph> nodeGraphs = new HashSet<>();
        Set<EdgeGraph> edgeGraphs = new HashSet<>();
        graph.vertexSet().forEach(node -> nodeGraphs.add(
            new NodeGraph(node.getId(), node.getName(),
                node.getProbability(), node.getCounter())
        ));
        graph.edgeSet().forEach(edge -> edgeGraphs.add(
            new EdgeGraph(
                graph.getEdgeSource(edge).getName(),
                graph.getEdgeTarget(edge).getName()
            )
        ));
        result.setNodes(nodeGraphs);
        result.setEdges(edgeGraphs);
        return result;
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

    /**
     * Export graph to {@see <a href = https://www.graphviz.org/ >GraphViz.dot</a>}
     * format.
     * @param graphTo graph in DTO format.
     * @return graph in GraphViz.dot format.
     */
    public static String exportToGraphViz(final GraphDto graphTo) {
        Graph<Node, DefaultEdge> graph = graphDtoToGraph(graphTo);
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
     * Build GraphTo object from graph models - nodes and edges.
     * @param nodes model of graph nodes.
     * @param edges model of graph edges.
     * @return object of GraphTo.
     */
    public static GraphDto buildGraphTo(final Collection<Node> nodes,
        final Collection<Edge> edges) {
        return new GraphDto(nodesToNodeGraphs(nodes), edgesToEdgeGraphs(edges));
    }

    /**
     * Convert nodes - model object collection into NodeGraph - transfer object set.
     * @param nodes model object set.
     * @return NodeGraph - transport object set.
     */
    public static Set<NodeGraph> nodesToNodeGraphs(final Collection<Node> nodes) {
        Set<NodeGraph> result = new HashSet<>();
        nodes.forEach(node -> result.add(
            new NodeGraph(node.getId(), node.getName(), node.getProbability(),
                node.getCounter())
            )
        );
        return result;
    }

    /**
     * Convert edges - model object collection into EdgeGraph - transfer object set.
     * @param edges model object set.
     * @return EdgeGraph - transport object set.
     */
    public static Set<EdgeGraph> edgesToEdgeGraphs(final Collection<Edge> edges) {
        Set<EdgeGraph> result = new HashSet<>();
        edges.forEach(edge -> result.add(
            new EdgeGraph(edge.getId(), edge.getNodeOne().getName(),
                edge.getNodeTwo().getName())
            )
        );
        return result;
    }

}
