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
import ru.resprojects.linkchecker.util.exeptions.NotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.when;
import static ru.resprojects.linkchecker.dto.GraphDto.NodeGraph;
import static ru.resprojects.linkchecker.dto.GraphDto.EdgeGraph;

@RunWith(SpringRunner.class)
public class GraphServiceTests {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private GraphService graphService;

    @MockBean
    private EdgeRepository edgeRepository;

    @MockBean
    private NodeRepository nodeRepository;

    @Before
    public void init() {
        graphService = new GraphServiceImpl(edgeRepository, nodeRepository);
    }


    @Test
    public void getNodeByNameTest() {
        given(nodeRepository.getByName("v1")).willReturn(
            new Node(5000, "v1", 0.5f, 0)
        );

        NodeGraph actual = graphService.getNode("v1");

        assertThat(actual.getName()).isEqualTo("v1");
        assertThat(actual.getProbability()).isEqualTo(0.5f);
        assertThat(actual.getCounter()).isEqualTo(0);
    }

    @Test
    public void getNodeByNameNotFoundTest() throws NotFoundException {

        thrown.expect(NotFoundException.class);
        thrown.expectMessage("Node with name v1 is not found");

        graphService.getNode("v1");
    }

    @Test
    public void getNodeByIdTest() {
        given(nodeRepository.findById(5000)).willReturn(
            Optional.of(new Node(5000, "v1", 0.5f, 0))
        );

        NodeGraph actual = graphService.getNodeById(5000);

        assertThat(actual.getName()).isEqualTo("v1");
        assertThat(actual.getProbability()).isEqualTo(0.5f);
        assertThat(actual.getCounter()).isEqualTo(0);
    }

    @Test
    public void getNodeByIdNotFoundTest() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("Node with ID = 5000 is not found");

        graphService.getNodeById(5000);
    }

    @Test
    public void getAllNodesTest() {
        List<Node> nodes = Stream.of(
            new Node(5000, "v1", 0.43f, 0),
            new Node(5001, "v2", 0.6f, 0),
            new Node(5002, "v3", 0.35f, 0),
            new Node(5003, "v4", 0.56f, 0),
            new Node(5004, "v5", 0.2f, 0)
        ).collect(Collectors.toList());
        given(nodeRepository.findAll()).willReturn(nodes);

        Set<NodeGraph> actual = graphService.getAllNodes();
        NodeGraph nodeGraph = GraphUtil.nodeToNodeGraph(nodes.get(4));

        Assert.assertEquals(5, actual.size());
        assertThat(actual).contains(nodeGraph);
    }

    @Test
    public void getAllEdgesTest() {
        List<Node> nodes = Stream.of(
            new Node(5000, "v1", 0.43f, 0),
            new Node(5001, "v2", 0.6f, 0),
            new Node(5002, "v3", 0.35f, 0),
            new Node(5003, "v4", 0.56f, 0),
            new Node(5004, "v5", 0.2f, 0)
        ).collect(Collectors.toList());
        List<Edge> edges = Stream.of(
            new Edge(5005, nodes.get(0), nodes.get(1)),
            new Edge(5006, nodes.get(0), nodes.get(2)),
            new Edge(5007, nodes.get(0), nodes.get(4)),
            new Edge(5008, nodes.get(2), nodes.get(3))
        ).collect(Collectors.toList());
        given(edgeRepository.findAll()).willReturn(edges);

        Set<EdgeGraph> actual = graphService.getAllEdges();
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
    public void deleteNodeByNodeGraphTest() {
        List<Node> nodes = Stream.of(
            new Node(5001, "v2", 0.6f, 0),
            new Node(5002, "v3", 0.35f, 0),
            new Node(5003, "v4", 0.56f, 0),
            new Node(5004, "v5", 0.2f, 0)
        ).collect(Collectors.toList());

        given(nodeRepository.findById(5000)).willReturn(
            Optional.of(new Node(5000, "v1", 0.43f, 0))
        );
        given(nodeRepository.findAll()).willReturn(nodes);

        NodeGraph nodeGraph = new NodeGraph(5000, "v1", 0.43f, 0);
        graphService.deleteNode(nodeGraph);
        Set<NodeGraph> actual = graphService.getAllNodes();
        Assert.assertEquals(4, actual.size());
    }

    @Test
    public void exceptionOneWhileDeleteNodeByNodeGraphTest() {

        NodeGraph nodeGraph = new NodeGraph(5000, "v1", 0.43f, 0);

        thrown.expect(NotFoundException.class);
        thrown.expectMessage(String.format("Node %s is not found", nodeGraph.toString()));

        graphService.deleteNode(nodeGraph);
    }

    @Test
    public void exceptionTwoWhileDeleteNodeByNodeGraphTest() {

        NodeGraph nodeGraph = new NodeGraph(null, "v1", 0.43f, 0);

        thrown.expect(NotFoundException.class);
        thrown.expectMessage(String.format("Node %s is not found", nodeGraph.toString()));

        graphService.deleteNode(nodeGraph);
    }

    @Test
    public void exceptionThreeWhileDeleteNodeByNodeGraphTest() {

        thrown.expect(NotFoundException.class);
        thrown.expectMessage("Node null is not found");

        NodeGraph nodeGraph = null;
        graphService.deleteNode(nodeGraph);
    }

    @Test
    public void deleteNodeByNameTest() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("Node with NAME = v1 is not found");

        List<Node> nodes = Stream.of(
            new Node(5001, "v2", 0.6f, 0),
            new Node(5002, "v3", 0.35f, 0),
            new Node(5003, "v4", 0.56f, 0),
            new Node(5004, "v5", 0.2f, 0)
        ).collect(Collectors.toList());

        when(nodeRepository.existsByName("v1")).thenReturn(true).thenReturn(false);
        given(nodeRepository.findAll()).willReturn(nodes);

        graphService.deleteNode("v1");
        Set<NodeGraph> actual = graphService.getAllNodes();
        Assert.assertEquals(4, actual.size());

        graphService.deleteNode("v1");
    }

    @Test
    public void exceptionOneWhileDeleteNodeByNameTest() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("Node with name null is not found");

        String name = null;

        graphService.deleteNode(name);
    }

    @Test
    public void deleteNodeByIdTest() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("Node with ID = 5000 is not found");

        List<Node> nodes = Stream.of(
            new Node(5001, "v2", 0.6f, 0),
            new Node(5002, "v3", 0.35f, 0),
            new Node(5003, "v4", 0.56f, 0),
            new Node(5004, "v5", 0.2f, 0)
        ).collect(Collectors.toList());

        when(nodeRepository.existsById(5000)).thenReturn(true).thenReturn(false);
        given(nodeRepository.findAll()).willReturn(nodes);

        graphService.deleteNode(5000);
        Set<NodeGraph> actual = graphService.getAllNodes();
        Assert.assertEquals(4, actual.size());

        graphService.deleteNode(5000);
    }

    @Test
    public void exceptionOneWhileDeleteNodeByIdTest() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("Node with ID = null is not found");

        Integer id = null;

        graphService.deleteNode(id);
    }
}
