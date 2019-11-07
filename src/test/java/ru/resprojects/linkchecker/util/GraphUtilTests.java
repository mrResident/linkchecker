package ru.resprojects.linkchecker.util;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import ru.resprojects.linkchecker.LinkcheckerApplication;
import ru.resprojects.linkchecker.dto.GraphDto;
import ru.resprojects.linkchecker.model.Edge;
import ru.resprojects.linkchecker.model.Node;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.resprojects.linkchecker.dto.GraphDto.NodeGraph;
import static ru.resprojects.linkchecker.dto.GraphDto.EdgeGraph;
import static ru.resprojects.linkchecker.util.GraphUtil.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LinkcheckerApplication.class)
@ActiveProfiles(profiles = "moc_test")
public class GraphUtilTests {

    private static final Logger LOG = LoggerFactory.getLogger(GraphUtilTests.class);

    private GraphDto graphDto;

    @Before
    public void init() {
        Set<NodeGraph> nodeGraphSet = Stream.of(
            new NodeGraph(5000, "v1", 0),
            new NodeGraph(5001, "v2", 0),
            new NodeGraph(5002, "v3", 0),
            new NodeGraph(5003, "v4", 0),
            new NodeGraph(5004, "v5", 0)
        ).collect(Collectors.toSet());
        Set<EdgeGraph> edgeGraphSet = Stream.of(
            new EdgeGraph(5005, "v1", "v2"),
            new EdgeGraph(5006, "v1", "v3"),
            new EdgeGraph(5007, "v1", "v5"),
            new EdgeGraph(5008, "v3", "v4")
        ).collect(Collectors.toSet());
        graphDto = new GraphDto(nodeGraphSet, edgeGraphSet);
    }

    @Test
    public void generateRandomNodeFaultTest() {
        Set<NodeGraph> nodeGraphSet = new HashSet<>();
        IntStream.range(0, 23).forEach(v -> nodeGraphSet.add(new NodeGraph(5000 + v, "v" + v, 0)));
        Map<String, Boolean> result = getRandomNodeFault(nodeGraphSet);
        Assert.assertNotNull(result);
        result.forEach((key, value) -> LOG.debug("Node: " + key + " is fault = " + value));
        long countOfFault = result.entrySet().stream().filter(Map.Entry::getValue).count();
        LOG.debug("Count of fault elements = " + countOfFault);
    }

    @Test
    public void returnEmptyMapFromGenerateRandomNodeFault() {
        Map<String, Boolean> result = getRandomNodeFault(null);
        Assert.assertTrue(result.isEmpty());
        result = getRandomNodeFault(new HashSet<>());
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void getEdgesFromGraphDtoTest() {
        Set<Edge> actual = getEdgesFromGraphDto(graphDto);
        Assert.assertNotNull(actual);
        assertThat(actual).isNotEmpty();
        Assert.assertEquals(graphDto.getEdges().size(), actual.size());
        Assert.assertTrue(actual.stream()
            .anyMatch(edge -> edge.getId().equals(
                graphDto.getEdges().iterator().next().getId())
            )
        );
        actual.forEach(edge -> LOG.debug(edge.toString()));
    }

    @Test
    public void getEdgesFromGraphDtoSkipEdgeGraph() {
        EdgeGraph eg = new EdgeGraph(5009, "v3", "v7");
        graphDto.getEdges().add(eg);
        Set<Edge> actual = getEdgesFromGraphDto(graphDto);
        Assert.assertNotNull(actual);
        Assert.assertTrue(actual.stream().noneMatch(
            edge -> edge.getId().equals(eg.getId()))
        );
        LOG.debug("EdgeGraph collection:");
        graphDto.getEdges().forEach(edgeGraph -> LOG.debug(edgeGraph.toString()));
        LOG.debug("Edge collection:");
        actual.forEach(edge -> LOG.debug(edge.toString()));
    }

    @Test
    public void edgeToEdgeGraphTest() {
        Edge edge = new Edge(3, new Node(1, "v1", 0),
            new Node(2, "v2", 0));
        EdgeGraph actual = edgeToEdgeGraph(edge);
        Assert.assertNotNull(actual);
        Assert.assertEquals(edge.getId(), actual.getId());
    }

    @Test
    public void edgeToEdgeGraphReturnNull() {
        Assert.assertNull(edgeToEdgeGraph(null));
    }

    @Test
    public void edgesToEdgeGraphsTest() {
        Set<Edge> edges = Stream.of(
            new Edge(3, new Node(1, "v1", 0),
                new Node(2, "v2", 0)),
            new Edge(6, new Node(4, "v1", 0),
                new Node(5, "v3", 0))
        ).collect(Collectors.toSet());
        Set<EdgeGraph> actual = edgesToEdgeGraphs(edges);
        Assert.assertNotNull(actual);
        assertThat(actual).isNotEmpty();
        Assert.assertEquals(edges.size(), actual.size());
        for (EdgeGraph edgeGraph : actual) {
            Assert.assertTrue(edges.stream()
                .anyMatch(e -> e.getId().equals(edgeGraph.getId())));
        }
    }

    @Test
    public void edgesToEdgeGraphsReturnEmptyCollection() {
        assertThat(edgesToEdgeGraphs(null)).isEmpty();
    }

    @Test
    public void nodeGraphToNodeTest() {
        NodeGraph nodeGraph = graphDto.getNodes().iterator().next();
        Node node = nodeGraphToNode(nodeGraph);
        Assert.assertNotNull(node);
        Assert.assertEquals(nodeGraph.getId(), node.getId());
    }

    @Test
    public void nodeGraphToNodeReturnNull() {
        Assert.assertNull(nodeGraphToNode(null));
    }

    @Test
    public void nodeToNodeGraphTest() {
        Node node = new Node(1, "v1", 0);
        NodeGraph actual = nodeToNodeGraph(node);
        Assert.assertNotNull(actual);
        Assert.assertEquals(node.getId(), node.getId());
    }

    @Test
    public void nodeToNodeGraphReturnNull() {
        Assert.assertNull(nodeToNodeGraph(null));
    }

    @Test
    public void nodeGraphsToNodesTest() {
        Set<Node> actual = nodeGraphsToNodes(graphDto.getNodes());
        Assert.assertNotNull(actual);
        assertThat(actual).isNotEmpty();
        Assert.assertEquals(graphDto.getNodes().size(), actual.size());
        for (Node node : actual) {
            Assert.assertTrue(graphDto.getNodes().stream()
                .anyMatch(n -> n.getId().equals(node.getId())));
        }
    }

    @Test
    public void nodeGraphsToNodesReturnEmptyCollection() {
        assertThat(nodeGraphsToNodes(null)).isEmpty();
    }

    @Test
    public void nodesToNodeGraphsTest() {
        Set<Node> nodes = Stream.of(
            new Node(1, "v1", 0),
            new Node(2, "v2", 0)
        ).collect(Collectors.toSet());
        Set<NodeGraph> actual = nodesToNodeGraphs(nodes);
        Assert.assertNotNull(actual);
        assertThat(actual).isNotEmpty();
        for (NodeGraph nodeGraph : actual) {
            Assert.assertTrue(nodes.stream()
                .anyMatch(n -> n.getId().equals(nodeGraph.getId())));
        }
    }

    @Test
    public void nodesToNodeGraphsReturnEmptyCollection() {
        assertThat(nodesToNodeGraphs(null)).isEmpty();
    }

    @Test
    public void exportToGraphVizTest() {
        String actual = exportToGraphViz(graphDto);
        assertThat(actual).isNotEmpty();
        assertThat(actual).contains("strict graph");
    }

    @Test
    public void exportToGraphVizReturnNull() {
        assertThat(exportToGraphViz(null)).isEmpty();
    }

    @Test
    public void graphBuilderTest() {
        Graph<Node, DefaultEdge> actual = graphBuilder(graphDto.getNodes(),
            graphDto.getEdges());
        Assert.assertNotNull(actual);
        Assert.assertEquals(actual.vertexSet().size(), graphDto.getNodes().size());
        Assert.assertEquals(actual.edgeSet().size(), graphDto.getEdges().size());
        LOG.debug(actual.toString());
    }

    @Test
    public void graphBuilderSkipNullElementFromNodesAndEdges() {
        graphDto.getNodes().add(null);
        graphDto.getEdges().add(null);
        Graph<Node, DefaultEdge> actual = graphBuilder(graphDto.getNodes(),
            graphDto.getEdges());
        Assert.assertNotNull(actual);
        Assert.assertNotEquals(actual.vertexSet().size(), graphDto.getNodes().size());
        Assert.assertNotEquals(actual.edgeSet().size(), graphDto.getEdges().size());
        LOG.debug(actual.toString());
    }

    @Test
    public void graphBuilderSkipEdgeWithNonExistsNodes() {
        graphDto.getEdges().add(new EdgeGraph(5009, "v10", "v12"));
        Graph<Node, DefaultEdge> actual = graphBuilder(graphDto.getNodes(),
            graphDto.getEdges());
        Assert.assertNotNull(actual);
        Assert.assertNotEquals(actual.edgeSet().size(), graphDto.getEdges().size());
        LOG.debug(actual.toString());
    }

    @Test
    public void graphBuilderReturnEmptyGraph() {
        Graph<Node, DefaultEdge> actual = graphBuilder(null,
            graphDto.getEdges());
        assertThat(actual.vertexSet()).isEmpty();
        assertThat(actual.edgeSet()).isEmpty();
        LOG.debug(actual.toString());
        actual = graphBuilder(graphDto.getNodes(), null);
        assertThat(actual.vertexSet()).isEmpty();
        assertThat(actual.edgeSet()).isEmpty();
        LOG.debug(actual.toString());
        actual = graphBuilder(null, null);
        assertThat(actual.vertexSet()).isEmpty();
        assertThat(actual.edgeSet()).isEmpty();
        LOG.debug(actual.toString());
    }

    @Test
    public void graphToGraphDtoTest() {
        GraphDto actual = graphToGraphDto(graphBuilder(graphDto.getNodes(),
            graphDto.getEdges()));
        assertThat(actual.getNodes()).isNotEmpty();
        assertThat(actual.getEdges()).isNotEmpty();
        Assert.assertEquals(graphDto.getNodes().size(), actual.getNodes().size());
        Assert.assertEquals(graphDto.getEdges().size(), actual.getEdges().size());
        for (NodeGraph nodeGraph : actual.getNodes()) {
            Assert.assertTrue(graphDto.getNodes().stream()
                .anyMatch(ng -> ng.equals(nodeGraph)));
        }
        // Because method graphToGraphDto is return edges without IDs ,
        // IDs is not checked.
        for (EdgeGraph edgeGraph : actual.getEdges()) {
            Assert.assertTrue(graphDto.getEdges().stream()
                .anyMatch(eg -> eg.getNodeOne().equals(edgeGraph.getNodeOne())
                    && eg.getNodeTwo().equals(edgeGraph.getNodeTwo()))
            );
        }
        LOG.debug(actual.toString());
    }

    @Test
    public void graphToGraphDtoReturnEmptyGraphDto() {
        GraphDto actual = graphToGraphDto(null);
        assertThat(actual.getNodes()).isEmpty();
        assertThat(actual.getEdges()).isEmpty();
    }

    @Test
    public void removeCyclesFromGraphTest() {
        GraphDto cyclesGraph = new GraphDto();
        cyclesGraph.getNodes().addAll(graphDto.getNodes());
        cyclesGraph.getEdges().addAll(graphDto.getEdges());
        cyclesGraph.getEdges().add(new EdgeGraph(5009, "v2", "v4"));
        cyclesGraph.getEdges().add(new EdgeGraph(5010, "v2", "v3"));
        cyclesGraph.getEdges().add(new EdgeGraph(5011, "v3", "v5"));
        cyclesGraph.getEdges().add(new EdgeGraph(5012, "v3", "v4"));
        cyclesGraph.getEdges().add(new EdgeGraph(5013, "v5", "v4"));
        GraphDto actual = graphToGraphDto(removeCyclesFromGraph(graphBuilder(
            cyclesGraph.getNodes(), cyclesGraph.getEdges())));
        Assert.assertEquals(graphDto.getNodes().size(), actual.getNodes().size());
        Assert.assertEquals(graphDto.getEdges().size(), actual.getEdges().size());
    }

    @Test
    public void removeCyclesFromGraphCyclesNotFound() {
        GraphDto actual = graphToGraphDto(removeCyclesFromGraph(graphBuilder(
            graphDto.getNodes(), graphDto.getEdges())));
        for (NodeGraph nodeGraph : actual.getNodes()) {
            Assert.assertTrue(graphDto.getNodes().stream()
                .anyMatch(ng -> ng.equals(nodeGraph)));
        }
        // Because method graphToGraphDto is return edges without IDs ,
        // IDs is not checked.
        for (EdgeGraph edgeGraph : actual.getEdges()) {
            Assert.assertTrue(graphDto.getEdges().stream()
                .anyMatch(eg -> eg.getNodeOne().equals(edgeGraph.getNodeOne())
                    && eg.getNodeTwo().equals(edgeGraph.getNodeTwo()))
            );
        }
    }


}
