package ru.resprojects.linkchecker.services;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import ru.resprojects.linkchecker.model.Node;
import ru.resprojects.linkchecker.repositories.NodeRepository;
import ru.resprojects.linkchecker.util.GraphUtil;
import ru.resprojects.linkchecker.util.exeptions.NotFoundException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.when;
import static ru.resprojects.linkchecker.dto.GraphDto.NodeGraph;

@RunWith(SpringRunner.class)
public class GraphNodeServiceMockTests {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private GraphNodeService graphNodeService;

    @MockBean
    private NodeRepository nodeRepository;

    @Before
    public void init() {
        graphNodeService = new GraphNodeServiceImpl(nodeRepository);
    }

    @Test
    public void getNodeByName() {
        given(nodeRepository.getByName("v1")).willReturn(
            new Node(5000, "v1", 50, 0)
        );
        NodeGraph actual = graphNodeService.get("v1");
        assertThat(actual.getName()).isEqualTo("v1");
        assertThat(actual.getProbability()).isEqualTo(50);
        assertThat(actual.getCounter()).isEqualTo(0);
    }

    @Test
    public void getNodeByNameNotFound() throws NotFoundException {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("Node with NAME = v1 is not found");
        graphNodeService.get("v1");
    }

    @Test
    public void getNodeById() {
        given(nodeRepository.findById(5000)).willReturn(
            Optional.of(new Node(5000, "v1", 50, 0))
        );
        NodeGraph actual = graphNodeService.getById(5000);
        assertThat(actual.getName()).isEqualTo("v1");
        assertThat(actual.getProbability()).isEqualTo(50);
        assertThat(actual.getCounter()).isEqualTo(0);
    }

    @Test
    public void getNodeByIdNotFound() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("Node with ID = 5000 is not found");
        graphNodeService.getById(5000);
    }

    @Test
    public void getAllNodes() {
        List<Node> nodes = Stream.of(
            new Node(5000, "v1", 43, 0),
            new Node(5001, "v2", 60, 0),
            new Node(5002, "v3", 35, 0),
            new Node(5003, "v4", 56, 0),
            new Node(5004, "v5", 20, 0)
        ).collect(Collectors.toList());
        given(nodeRepository.findAll()).willReturn(nodes);
        Set<NodeGraph> actual = graphNodeService.getAll();
        NodeGraph nodeGraph = GraphUtil.nodeToNodeGraph(nodes.get(4));
        Assert.assertEquals(5, actual.size());
        assertThat(actual).contains(nodeGraph);
    }

    @Test
    public void deleteNodeByNodeGraph() {
        List<Node> nodes = Stream.of(
            new Node(5001, "v2", 60, 0),
            new Node(5002, "v3", 35, 0),
            new Node(5003, "v4", 56, 0),
            new Node(5004, "v5", 20, 0)
        ).collect(Collectors.toList());
        given(nodeRepository.findById(5000)).willReturn(
            Optional.of(new Node(5000, "v1", 43, 0))
        );
        given(nodeRepository.findAll()).willReturn(nodes);
        NodeGraph nodeGraph = new NodeGraph(5000, "v1", 43, 0);
        graphNodeService.delete(nodeGraph);
        Set<NodeGraph> actual = graphNodeService.getAll();
        Assert.assertEquals(4, actual.size());
    }

    @Test
    public void exceptionOneWhileDeleteNodeByNodeGraph() {
        NodeGraph nodeGraph = new NodeGraph(5000, "v1", 43, 0);
        thrown.expect(NotFoundException.class);
        thrown.expectMessage(String.format("Node %s is not found", nodeGraph.toString()));
        graphNodeService.delete(nodeGraph);
    }

    @Test
    public void exceptionTwoWhileDeleteNodeByNodeGraph() {
        NodeGraph nodeGraph = new NodeGraph(null, "v1", 43, 0);
        thrown.expect(NotFoundException.class);
        thrown.expectMessage(String.format("Node %s is not found", nodeGraph.toString()));
        graphNodeService.delete(nodeGraph);
    }

    @Test
    public void exceptionThreeWhileDeleteNodeByNodeGraph() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Must not be null");
        graphNodeService.delete((NodeGraph) null);
    }

    @Test
    public void deleteNodeByName() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("Node with NAME = v1 is not found");
        List<Node> nodes = Stream.of(
            new Node(5001, "v2", 60, 0),
            new Node(5002, "v3", 35, 0),
            new Node(5003, "v4", 56, 0),
            new Node(5004, "v5", 20, 0)
        ).collect(Collectors.toList());
        when(nodeRepository.existsByName("v1")).thenReturn(true).thenReturn(false);
        given(nodeRepository.findAll()).willReturn(nodes);
        graphNodeService.delete("v1");
        Set<NodeGraph> actual = graphNodeService.getAll();
        Assert.assertEquals(4, actual.size());
        graphNodeService.delete("v1");
    }

    @Test
    public void exceptionOneWhileDeleteNodeByName() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("Node with NAME = null is not found");
        graphNodeService.delete((String) null);
    }

    @Test
    public void deleteNodeById() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("Node with ID = 5000 is not found");
        List<Node> nodes = Stream.of(
            new Node(5001, "v2", 60, 0),
            new Node(5002, "v3", 35, 0),
            new Node(5003, "v4", 56, 0),
            new Node(5004, "v5", 20, 0)
        ).collect(Collectors.toList());
        when(nodeRepository.existsById(5000)).thenReturn(true).thenReturn(false);
        given(nodeRepository.findAll()).willReturn(nodes);
        graphNodeService.delete(5000);
        Set<NodeGraph> actual = graphNodeService.getAll();
        Assert.assertEquals(4, actual.size());
        graphNodeService.delete(5000);
    }

    @Test
    public void exceptionOneWhileDeleteNodeById() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("Node with ID = null is not found");
        graphNodeService.delete((Integer) null);
    }

    @Test
    public void createNode() {
        NodeGraph nodeGraph = new NodeGraph("v1");
        Node node = new Node("v1");
        node.setId(5000);
        when(nodeRepository.save(any(Node.class))).thenReturn(node);
        NodeGraph actual = graphNodeService.create(nodeGraph);
        Assert.assertNotNull(actual);
        Assert.assertNotNull(actual.getId());
        Assert.assertEquals(5000, actual.getId().intValue());
        Assert.assertEquals("v1", actual.getName());
    }

    @Test
    public void exceptionWhileCreateNode() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Must not be null");
        graphNodeService.create((NodeGraph) null);
    }

    @Test
    public void createNodes() {
        Set<NodeGraph> nodeGraphs = new HashSet<>();
        List<Node> nodes = new ArrayList<>();
        IntStream.range(1, 6).forEach(i -> {
            nodeGraphs.add(new NodeGraph("w" + i));
            nodes.add(new Node(5000 + i, "w" + i, 50, 0));
        });
        when(nodeRepository.saveAll(anyIterable())).thenReturn(nodes);
        Set<NodeGraph> actual = graphNodeService.create(nodeGraphs);
        Assert.assertNotNull(actual);
        Assert.assertEquals(5, actual.size());
    }

    @Test
    public void exceptionOneWhileCreateNodes() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Must not be null");
        graphNodeService.create((Set<NodeGraph>) null);
    }

    @Test
    public void exceptionTwoWhileCreateNodes() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Collection does not be empty");
        graphNodeService.create(new HashSet<>());
    }

    @Test
    public void exceptionThreeWhileCreateNodes() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Collection does not be contain null element");
        Set<NodeGraph> nodeGraphs = new HashSet<>();
        nodeGraphs.add(new NodeGraph("v1"));
        nodeGraphs.add(null);
        graphNodeService.create(nodeGraphs);
    }

    @Test
    public void updateNode() {
        NodeGraph nodeGraph = new NodeGraph(5000, "v1", 23, 2);
        Node node = new Node(5000, "v1", 23, 2);
        when(nodeRepository.save(any(Node.class))).thenReturn(node);
        when(nodeRepository.findById(5000)).thenReturn(Optional.of(node));
        graphNodeService.update(nodeGraph);
        NodeGraph actual = graphNodeService.getById(5000);
        Assert.assertNotNull(actual);
        Assert.assertEquals(nodeGraph, GraphUtil.nodeToNodeGraph(node));
    }

    @Test
    public void exceptionOneWhileUpdateNode() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Must not be null");
        graphNodeService.update(null);
    }

    @Test
    public void exceptionTwoWhileUpdateNode() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("Error while update node with id = 5000");
        NodeGraph nodeGraph = new NodeGraph(5000, "v1", 50, 0);
        when(nodeRepository.save(any(Node.class))).thenReturn(null);
        graphNodeService.update(nodeGraph);
    }

}
