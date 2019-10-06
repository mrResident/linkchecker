package ru.resprojects.linkchecker.services;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import ru.resprojects.linkchecker.model.Edge;
import ru.resprojects.linkchecker.model.Node;
import ru.resprojects.linkchecker.repositories.EdgeRepository;
import ru.resprojects.linkchecker.repositories.NodeRepository;
import ru.resprojects.linkchecker.util.GraphUtil;
import ru.resprojects.linkchecker.util.Messages;
import ru.resprojects.linkchecker.util.exeptions.ApplicationException;
import ru.resprojects.linkchecker.util.exeptions.NotFoundException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static ru.resprojects.linkchecker.dto.GraphDto.EdgeGraph;

@RunWith(SpringRunner.class)
public class GraphEdgeServiceMockTests {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @MockBean
    private EdgeRepository edgeRepository;

    @MockBean
    private NodeRepository nodeRepository;

    private GraphEdgeService edgeService;

    private List<Node> nodes;

    @Before
    public void init() {
        edgeService = new GraphEdgeServiceImpl(edgeRepository, nodeRepository);
        nodes = Stream.of(
            new Node(5000, "v1", 0),
            new Node(5001, "v2", 0),
            new Node(5002, "v3", 0),
            new Node(5003, "v4", 0),
            new Node(5004, "v5", 0)
        ).collect(Collectors.toList());
    }

    @Test
    public void createEdge() {
        Node nodeOne = nodes.get(0);
        Node nodeTwo = nodes.get(1);
        Edge edge = new Edge(5005, nodeOne, nodeTwo);
        EdgeGraph edgeGraph = new EdgeGraph("v1", "v2");
        given(nodeRepository.getByName("v1")).willReturn(nodeOne);
        given(nodeRepository.getByName("v2")).willReturn(nodeTwo);
        when(edgeRepository.save(any(Edge.class))).thenReturn(edge);
        EdgeGraph actual = edgeService.create(edgeGraph);
        Assert.assertNotNull(actual);
        Assert.assertEquals(new Integer(5005), actual.getId());
    }

    @Test
    public void exceptionOneWhileCreateEdge() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("Node with NAME = v1 is not found");
        EdgeGraph edgeGraph = new EdgeGraph("v1", "v2");
        given(nodeRepository.getByName("v1")).willReturn(null);
        edgeService.create(edgeGraph);
    }

    @Test
    public void exceptionTwoWhileCreateEdge() {
        thrown.expect(ApplicationException.class);
        thrown.expectMessage(Messages.MSG_ARGUMENT_NULL);
        edgeService.create((EdgeGraph) null);
    }

    @Test
    public void createEdges() {
        List<Edge> edges = new ArrayList<>();
        edges.add(new Edge(5005,nodes.get(0), nodes.get(1)));
        edges.add(new Edge(5006,nodes.get(0), nodes.get(2)));
        edges.add(new Edge(5007,nodes.get(0), nodes.get(4)));
        edges.add(new Edge(5008,nodes.get(2), nodes.get(3)));
        Set<EdgeGraph> edgeGraphs = edges.stream()
            .map(e -> new EdgeGraph(e.getNodeOne().getName(), e.getNodeTwo().getName()))
            .collect(Collectors.toSet());
        given(nodeRepository.getByName("v1")).willReturn(nodes.get(0));
        given(nodeRepository.getByName("v2")).willReturn(nodes.get(1));
        given(nodeRepository.getByName("v3")).willReturn(nodes.get(2));
        given(nodeRepository.getByName("v4")).willReturn(nodes.get(3));
        given(nodeRepository.getByName("v5")).willReturn(nodes.get(4));
        when(edgeRepository.saveAll(anyIterable())).thenReturn(edges);
        Set<EdgeGraph> actual = edgeService.create(edgeGraphs);
        Assert.assertNotNull(actual);
        Assert.assertEquals(4, actual.size());
    }

    @Test
    public void exceptionOneWhileCreateEdges() {
        List<Edge> edges = Stream.of(
            new Edge(5005,nodes.get(0), nodes.get(1)),
            new Edge(5006,nodes.get(0), nodes.get(2)),
            new Edge(5007,nodes.get(0), nodes.get(4)),
            new Edge(5008,nodes.get(2), nodes.get(3))
        ).collect(Collectors.toList());
        Set<EdgeGraph> edgeGraphs = edges.stream()
            .map(e -> new EdgeGraph(e.getNodeOne().getName(), e.getNodeTwo().getName()))
            .collect(Collectors.toSet());
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("Node with NAME = v1 is not found");
        given(nodeRepository.getByName("v1")).willReturn(null);
        edgeService.create(edgeGraphs);
    }

    @Test
    public void exceptionTwoWhileCreateEdges() {
        thrown.expect(ApplicationException.class);
        thrown.expectMessage(Messages.MSG_ARGUMENT_NULL);
        edgeService.create((Set<EdgeGraph>) null);
    }

    @Test
    public void exceptionThreeWhileCreateEdges() {
        thrown.expect(ApplicationException.class);
        thrown.expectMessage("Collection must not be empty");
        edgeService.create(new HashSet<>());
    }

    @Test
    public void exceptionFourWhileCreateEdges() {
        thrown.expect(ApplicationException.class);
        thrown.expectMessage("Collection must not contain a null item");
        Set<EdgeGraph> edgeGraphs = new HashSet<>();
        edgeGraphs.add(new EdgeGraph("v1", "v2"));
        edgeGraphs.add(null);
        edgeService.create(edgeGraphs);
    }

    @Test
    public void deleteEdgeById() {
        List<Edge> edges = Stream.of(
            new Edge(5008, nodes.get(2), nodes.get(3))
        ).collect(Collectors.toList());
        given(edgeRepository.existsById(new Integer(5005))).willReturn(true);
        given(edgeRepository.findAll()).willReturn(edges);
        edgeService.delete(5005);
        Set<EdgeGraph> actual = edgeService.getAll();
        Assert.assertEquals(1, actual.size());
    }

    @Test
    public void exceptionWhileDeleteEdgeById() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("EDGE with ID = 5050 is not found");
        when(edgeRepository.existsById(5050)).thenReturn(false);
        edgeService.delete(5050);
    }

    @Test
    public void deleteEdgesByNodeName() {
        List<Edge> edges = Stream.of(
            new Edge(5005,nodes.get(0), nodes.get(1)),
            new Edge(5006,nodes.get(0), nodes.get(2)),
            new Edge(5007,nodes.get(0), nodes.get(4))
        ).collect(Collectors.toList());
        List<Edge> edgesAfterDelete = Stream.of(
            new Edge(5008, nodes.get(2), nodes.get(3))
        ).collect(Collectors.toList());
        given(nodeRepository.getByName("v1")).willReturn(nodes.get(0));
        given(edgeRepository.findEdgesByNodeOneOrNodeTwo(any(Node.class), any(Node.class))).willReturn(edges);
        given(edgeRepository.findAll()).willReturn(edgesAfterDelete);
        edgeService.delete("v1");
        Set<EdgeGraph> actual = edgeService.getAll();
        Assert.assertEquals(1, actual.size());
    }

    @Test
    public void exceptionWhileDeleteEdgesByNodeName() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("Edges for node v1 is not found");
        List<Edge> emptyList = new ArrayList<>();
        given(edgeRepository.findEdgesByNodeOneOrNodeTwo(any(Node.class), any(Node.class))).
            willReturn(emptyList);
        edgeService.delete("v1");
    }

    @Test
    public void deleteEdgeByNodeNameOneAndNodeNameTwo() {
        Edge edge = new Edge(5005,nodes.get(0), nodes.get(1));
        given(nodeRepository.getByName("v1")).willReturn(nodes.get(0));
        given(nodeRepository.getByName("v2")).willReturn(nodes.get(1));
        given(edgeRepository.findEdgeByNodeOneAndNodeTwo(any(Node.class), any(Node.class))).
            willReturn(Optional.of(edge));
        edgeService.delete("v1", "v2");
    }

    @Test
    public void exceptionWhileDeleteEdgeByNodeNameOneAndNodeNameTwo() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("Edge for nodes [v1, v2] is not found");
        edgeService.delete("v1", "v2");
    }

    @Test
    public void getAllEdges() {
        List<Edge> edges = Stream.of(
            new Edge(5005, nodes.get(0), nodes.get(1)),
            new Edge(5006, nodes.get(0), nodes.get(2)),
            new Edge(5007, nodes.get(0), nodes.get(4)),
            new Edge(5008, nodes.get(2), nodes.get(3))
        ).collect(Collectors.toList());
        given(edgeRepository.findAll()).willReturn(edges);
        Set<EdgeGraph> actual = edgeService.getAll();
        EdgeGraph edgeGraph = GraphUtil.edgeToEdgeGraph(edges.get(2));
        Assert.assertEquals(4, actual.size());
        assertThat(actual).contains(edgeGraph);
        assertThat(actual.stream()
            .filter(eg -> eg.getId().equals(5007))
            .findFirst()
            .get().getNodeOne()).isEqualTo("v1");
        assertThat(actual.stream()
            .filter(eg -> eg.getId().equals(5007))
            .findFirst()
            .get().getNodeTwo()).isEqualTo("v5");
    }

    @Test
    public void getEdgeById() {
        Node nodeOne = nodes.get(0);
        Node nodeTwo = nodes.get(1);
        Edge edge = new Edge(5005, nodeOne, nodeTwo);
        given(edgeRepository.findById(5005)).willReturn(Optional.of(edge));
        EdgeGraph actual = edgeService.getById(5005);
        Assert.assertEquals("v1", actual.getNodeOne());
        Assert.assertEquals("v2", actual.getNodeTwo());
    }

    @Test
    public void exceptionWhileGetEdgeById() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("EDGE with ID = 7000 is not found");
        edgeService.getById(7000);
    }

    @Test
    public void getEdgesByNodeName() {
        List<Edge> edges = Stream.of(
            new Edge(5005, nodes.get(0), nodes.get(1)),
            new Edge(5006, nodes.get(0), nodes.get(2)),
            new Edge(5007, nodes.get(0), nodes.get(4))
        ).collect(Collectors.toList());
        given(nodeRepository.getByName("v1")).willReturn(nodes.get(0));
        given(edgeRepository.findEdgesByNodeOneOrNodeTwo(nodes.get(0),nodes.get(0)))
            .willReturn(edges);
        Set<EdgeGraph> actual = edgeService.get("v1");
        EdgeGraph edgeGraph = GraphUtil.edgeToEdgeGraph(edges.get(0));
        assertThat(actual).contains(edgeGraph);
        assertThat(actual.stream()
            .filter(eg -> eg.getId().equals(5005))
            .findFirst()
            .get().getNodeOne()).isEqualTo("v1");
        assertThat(actual.stream()
            .filter(eg -> eg.getId().equals(5005))
            .findFirst()
            .get().getNodeTwo()).isEqualTo("v2");
    }

    @Test
    public void getEdgeByNodeOneAndNodeTwoNames() {
        Node nodeOne = nodes.get(0);
        Node nodeTwo = nodes.get(1);
        Edge edge = new Edge(5005, nodeOne, nodeTwo);
        given(nodeRepository.getByName("v1")).willReturn(nodeOne);
        given(nodeRepository.getByName("v2")).willReturn(nodeTwo);
        given(edgeRepository.findEdgeByNodeOneAndNodeTwo(nodeOne, nodeTwo)).willReturn(Optional.of(edge));
        EdgeGraph actual = edgeService.get("v1", "v2");
        Assert.assertNotNull(actual);
        Assert.assertEquals("v1", actual.getNodeOne());
        Assert.assertEquals("v2", actual.getNodeTwo());
    }

    @Test
    public void exceptionWhileEdgeByNodeOneAndNodeTwoNames() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("Edge for nodes [v1, v2] is not found");
        edgeService.get("v1", "v2");
    }

}
