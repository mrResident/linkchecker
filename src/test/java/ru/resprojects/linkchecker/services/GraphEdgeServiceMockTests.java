package ru.resprojects.linkchecker.services;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import ru.resprojects.linkchecker.AppProperties;
import ru.resprojects.linkchecker.LinkcheckerApplication;
import ru.resprojects.linkchecker.model.Edge;
import ru.resprojects.linkchecker.model.Node;
import ru.resprojects.linkchecker.repositories.EdgeRepository;
import ru.resprojects.linkchecker.repositories.NodeRepository;
import ru.resprojects.linkchecker.util.GraphUtil;
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
@SpringBootTest(classes = LinkcheckerApplication.class)
@ActiveProfiles(profiles = "moc_test")
public class GraphEdgeServiceMockTests {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @MockBean
    private EdgeRepository edgeRepository;

    @MockBean
    private NodeRepository nodeRepository;

    @Autowired
    private AppProperties properties;

    private GraphEdgeService edgeService;

    private List<Node> nodes;

    @Before
    public void init() {
        edgeService = new GraphEdgeServiceImpl(edgeRepository, nodeRepository, properties);
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
    public void createEdgeNodeNotFoundException() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage(String.format(properties.getNodeMsg().get("NODE_MSG_BY_NAME_ERROR"), "v1"));
        EdgeGraph edgeGraph = new EdgeGraph("v1", "v2");
        given(nodeRepository.getByName("v1")).willReturn(null);
        edgeService.create(edgeGraph);
    }

    @Test
    public void createEdgeNullArgumentException() {
        thrown.expect(ApplicationException.class);
        thrown.expectMessage(properties.getAppMsg().get("MSG_ARGUMENT_NULL"));
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
    public void createEdgesNodeNotFoundException() {
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
        thrown.expectMessage(String.format(properties.getNodeMsg().get("NODE_MSG_BY_NAME_ERROR"), "v1"));
        given(nodeRepository.getByName("v1")).willReturn(null);
        edgeService.create(edgeGraphs);
    }

    @Test
    public void createEdgesNullArgumentException() {
        thrown.expect(ApplicationException.class);
        thrown.expectMessage(properties.getAppMsg().get("MSG_ARGUMENT_NULL"));
        edgeService.create((Set<EdgeGraph>) null);
    }

    @Test
    public void createEdgesEmptyCollectionException() {
        thrown.expect(ApplicationException.class);
        thrown.expectMessage(properties.getAppMsg().get("MSG_COLLECTION_EMPTY"));
        edgeService.create(new HashSet<>());
    }

    @Test
    public void createEdgesCollectionContainNullException() {
        thrown.expect(ApplicationException.class);
        thrown.expectMessage(properties.getAppMsg().get("MSG_COLLECTION_CONTAIN_NULL"));
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
    public void deleteEdgeByIdNotFoundException() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage(String.format(properties.getAppMsg().get("MSG_BY_ID_ERROR"), "EDGE", 5050));
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
    public void deleteEdgesByNodeNameNotFoundException() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage(String.format(properties.getEdgeMsg().get("EDGE_MSG_GET_BY_NAME_ERROR"), "v1"));
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
    public void deleteEdgeByNodeNameOneAndNodeNameTwoNotFoundException() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage(String.format(properties.getEdgeMsg().get("EDGE_MSG_GET_ERROR"), "v1", "v2"));
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
    public void getEdgeByIdNotFoundException() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage(String.format(properties.getAppMsg().get("MSG_BY_ID_ERROR"), "EDGE", 7000));
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
        thrown.expectMessage(String.format(properties.getEdgeMsg().get("EDGE_MSG_GET_ERROR"), "v1", "v2"));
        edgeService.get("v1", "v2");
    }

}
