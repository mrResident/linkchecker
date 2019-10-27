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
import ru.resprojects.linkchecker.model.Node;
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
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.when;
import static ru.resprojects.linkchecker.dto.GraphDto.NodeGraph;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LinkcheckerApplication.class)
@ActiveProfiles(profiles = "moc_test")
public class GraphNodeServiceMockTests {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private GraphNodeService graphNodeService;

    @MockBean
    private NodeRepository nodeRepository;

    @Autowired
    private AppProperties properties;

    @Before
    public void init() {
        graphNodeService = new GraphNodeServiceImpl(nodeRepository, properties);
    }

    @Test
    public void getNodeByName() {
        given(nodeRepository.getByName("v1")).willReturn(
            new Node(5000, "v1", 0)
        );
        NodeGraph actual = graphNodeService.get("v1");
        assertThat(actual.getName()).isEqualTo("v1");
        assertThat(actual.getCounter()).isEqualTo(0);
    }

    @Test
    public void getNodeByNameNotFoundException() throws NotFoundException {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage(String.format(properties.getNodeMsg().get("NODE_MSG_BY_NAME_ERROR"), "v1"));
        graphNodeService.get("v1");
    }

    @Test
    public void getNodeById() {
        given(nodeRepository.findById(5000)).willReturn(
            Optional.of(new Node(5000, "v1", 0))
        );
        NodeGraph actual = graphNodeService.getById(5000);
        assertThat(actual.getName()).isEqualTo("v1");
        assertThat(actual.getCounter()).isEqualTo(0);
    }

    @Test
    public void getNodeByIdNotFoundException() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage( String.format(properties.getAppMsg().get("MSG_BY_ID_ERROR"), "NODE", 5000));
        graphNodeService.getById(5000);
    }

    @Test
    public void getAllNodes() {
        List<Node> nodes = Stream.of(
            new Node(5000, "v1", 0),
            new Node(5001, "v2", 0),
            new Node(5002, "v3", 0),
            new Node(5003, "v4", 0),
            new Node(5004, "v5", 0)
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
            new Node(5001, "v2", 0),
            new Node(5002, "v3", 0),
            new Node(5003, "v4", 0),
            new Node(5004, "v5", 0)
        ).collect(Collectors.toList());
        given(nodeRepository.findById(5000)).willReturn(
            Optional.of(new Node(5000, "v1", 0))
        );
        given(nodeRepository.findAll()).willReturn(nodes);
        NodeGraph nodeGraph = new NodeGraph(5000, "v1", 0);
        graphNodeService.delete(nodeGraph);
        Set<NodeGraph> actual = graphNodeService.getAll();
        Assert.assertEquals(4, actual.size());
    }

    @Test
    public void deleteNodeByNodeGraphNotFoundException() {
        NodeGraph nodeGraph = new NodeGraph(5000, "v1", 0);
        thrown.expect(NotFoundException.class);
        thrown.expectMessage(String.format(properties.getNodeMsg().get("NODE_MSG_BY_OBJECT_ERROR"), nodeGraph.toString()));
        graphNodeService.delete(nodeGraph);
    }

    @Test
    public void deleteNodeByNodeGraphWithNullIdNotFoundException() {
        NodeGraph nodeGraph = new NodeGraph(null, "v1", 0);
        thrown.expect(NotFoundException.class);
        thrown.expectMessage(String.format(properties.getNodeMsg().get("NODE_MSG_BY_OBJECT_ERROR"), nodeGraph.toString()));
        graphNodeService.delete(nodeGraph);
    }

    @Test
    public void deleteNodeByNodeGraphNullArgumentException() {
        thrown.expect(ApplicationException.class);
        thrown.expectMessage(properties.getAppMsg().get("MSG_ARGUMENT_NULL"));
        graphNodeService.delete((NodeGraph) null);
    }

    @Test
    public void deleteNodeByName() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage(String.format(properties.getNodeMsg().get("NODE_MSG_BY_NAME_ERROR"), "v1"));
        List<Node> nodes = Stream.of(
            new Node(5001, "v2", 0),
            new Node(5002, "v3", 0),
            new Node(5003, "v4", 0),
            new Node(5004, "v5", 0)
        ).collect(Collectors.toList());
        when(nodeRepository.existsByName("v1")).thenReturn(true).thenReturn(false);
        given(nodeRepository.findAll()).willReturn(nodes);
        graphNodeService.delete("v1");
        Set<NodeGraph> actual = graphNodeService.getAll();
        Assert.assertEquals(4, actual.size());
        graphNodeService.delete("v1");
    }

    @Test
    public void deleteNodeByNameNotFoundException() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage(String.format(properties.getNodeMsg().get("NODE_MSG_BY_NAME_ERROR"), "null"));
        graphNodeService.delete((String) null);
    }

    @Test
    public void deleteNodeById() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage( String.format(properties.getAppMsg().get("MSG_BY_ID_ERROR"), "NODE", 5000));
        List<Node> nodes = Stream.of(
            new Node(5001, "v2", 0),
            new Node(5002, "v3", 0),
            new Node(5003, "v4", 0),
            new Node(5004, "v5", 0)
        ).collect(Collectors.toList());
        when(nodeRepository.existsById(5000)).thenReturn(true).thenReturn(false);
        given(nodeRepository.findAll()).willReturn(nodes);
        graphNodeService.delete(5000);
        Set<NodeGraph> actual = graphNodeService.getAll();
        Assert.assertEquals(4, actual.size());
        graphNodeService.delete(5000);
    }

    @Test
    public void deleteNodeByIdNotFoundException() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage( String.format(properties.getAppMsg().get("MSG_BY_ID_ERROR"), "NODE", null));
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
    public void createNodeIsPresentException() {
        thrown.expect(ApplicationException.class);
        NodeGraph nodeGraph = new NodeGraph("v1");
        thrown.expectMessage(String.format(
            properties.getNodeMsg().get("NODE_MSG_ALREADY_PRESENT_ERROR"),
            nodeGraph.getName()
        ));
        Node node = new Node("v1");
        node.setId(5000);
        when(nodeRepository.getByName(any(String.class))).thenReturn(node);
        graphNodeService.create(nodeGraph);
    }

    @Test
    public void createNodeNullArgumentException() {
        thrown.expect(ApplicationException.class);
        thrown.expectMessage(properties.getAppMsg().get("MSG_ARGUMENT_NULL"));
        graphNodeService.create((NodeGraph) null);
    }

    @Test
    public void createNodes() {
        Set<NodeGraph> nodeGraphs = new HashSet<>();
        List<Node> nodes = new ArrayList<>();
        IntStream.range(1, 6).forEach(i -> {
            nodeGraphs.add(new NodeGraph("w" + i));
            nodes.add(new Node(5000 + i, "w" + i, 0));
        });
        when(nodeRepository.saveAll(anyIterable())).thenReturn(nodes);
        Set<NodeGraph> actual = graphNodeService.create(nodeGraphs);
        Assert.assertNotNull(actual);
        Assert.assertEquals(5, actual.size());
    }

    @Test
    public void createNodesIsPresentException() {
        thrown.expect(ApplicationException.class);
        thrown.expectMessage(String.format(
            properties.getNodeMsg().get("NODE_MSG_ALREADY_PRESENT_ERROR"),
            "w1"
        ));
        Set<NodeGraph> nodeGraphs = new HashSet<>();
        nodeGraphs.add(new NodeGraph("w1"));
        Node node = new Node(5000, "w1", 0);
        when(nodeRepository.getByName(any(String.class))).thenReturn(node);
        graphNodeService.create(nodeGraphs);
    }

    @Test
    public void createNodesNullArgumentException() {
        thrown.expect(ApplicationException.class);
        thrown.expectMessage(properties.getAppMsg().get("MSG_ARGUMENT_NULL"));
        graphNodeService.create((Set<NodeGraph>) null);
    }

    @Test
    public void createNodesEmptyCollectionException() {
        thrown.expect(ApplicationException.class);
        thrown.expectMessage(properties.getAppMsg().get("MSG_COLLECTION_EMPTY"));
        graphNodeService.create(new HashSet<>());
    }

    @Test
    public void createNodesCollectionContainNullException() {
        thrown.expect(ApplicationException.class);
        thrown.expectMessage(properties.getAppMsg().get("MSG_COLLECTION_CONTAIN_NULL"));
        Set<NodeGraph> nodeGraphs = new HashSet<>();
        nodeGraphs.add(new NodeGraph("v1"));
        nodeGraphs.add(null);
        graphNodeService.create(nodeGraphs);
    }

    @Test
    public void updateNode() {
        NodeGraph nodeGraph = new NodeGraph(5000, "v1", 2);
        Node node = new Node(5000, "v1", 2);
        when(nodeRepository.save(any(Node.class))).thenReturn(node);
        when(nodeRepository.findById(5000)).thenReturn(Optional.of(node));
        graphNodeService.update(nodeGraph);
        NodeGraph actual = graphNodeService.getById(5000);
        Assert.assertNotNull(actual);
        Assert.assertEquals(nodeGraph, GraphUtil.nodeToNodeGraph(node));
    }

    @Test
    public void updateNodeNullArgumentException() {
        thrown.expect(ApplicationException.class);
        thrown.expectMessage(properties.getAppMsg().get("MSG_ARGUMENT_NULL"));
        graphNodeService.update(null);
    }

    @Test
    public void updateNodeWhileUpdateException() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage(String.format(properties.getNodeMsg().get("NODE_MSG_UPDATE_ERROR"), 5000));
        NodeGraph nodeGraph = new NodeGraph(5000, "v1", 0);
        when(nodeRepository.save(any(Node.class))).thenReturn(null);
        graphNodeService.update(nodeGraph);
    }

}
