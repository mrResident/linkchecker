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

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.when;
import static ru.resprojects.linkchecker.dto.GraphDto.NodeGraph;

@RunWith(SpringRunner.class)
public class NodeGraphServiceTests {

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
    public void getNodeByNameTest() {
        given(nodeRepository.getByName("v1")).willReturn(
            new Node(5000, "v1", 0.5f, 0)
        );

        NodeGraph actual = graphNodeService.get("v1");

        assertThat(actual.getName()).isEqualTo("v1");
        assertThat(actual.getProbability()).isEqualTo(0.5f);
        assertThat(actual.getCounter()).isEqualTo(0);
    }

    @Test
    public void getNodeByNameNotFoundTest() throws NotFoundException {

        thrown.expect(NotFoundException.class);
        thrown.expectMessage("Node with name v1 is not found");

        graphNodeService.get("v1");
    }

    @Test
    public void getNodeByIdTest() {
        given(nodeRepository.findById(5000)).willReturn(
            Optional.of(new Node(5000, "v1", 0.5f, 0))
        );

        NodeGraph actual = graphNodeService.getById(5000);

        assertThat(actual.getName()).isEqualTo("v1");
        assertThat(actual.getProbability()).isEqualTo(0.5f);
        assertThat(actual.getCounter()).isEqualTo(0);
    }

    @Test
    public void getNodeByIdNotFoundTest() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("Node with ID = 5000 is not found");

        graphNodeService.getById(5000);
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

        Set<NodeGraph> actual = graphNodeService.getAll();
        NodeGraph nodeGraph = GraphUtil.nodeToNodeGraph(nodes.get(4));

        Assert.assertEquals(5, actual.size());
        assertThat(actual).contains(nodeGraph);
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
        graphNodeService.delete(nodeGraph);
        Set<NodeGraph> actual = graphNodeService.getAll();
        Assert.assertEquals(4, actual.size());
    }

    @Test
    public void exceptionOneWhileDeleteNodeByNodeGraphTest() {

        NodeGraph nodeGraph = new NodeGraph(5000, "v1", 0.43f, 0);

        thrown.expect(NotFoundException.class);
        thrown.expectMessage(String.format("Node %s is not found", nodeGraph.toString()));

        graphNodeService.delete(nodeGraph);
    }

    @Test
    public void exceptionTwoWhileDeleteNodeByNodeGraphTest() {

        NodeGraph nodeGraph = new NodeGraph(null, "v1", 0.43f, 0);

        thrown.expect(NotFoundException.class);
        thrown.expectMessage(String.format("Node %s is not found", nodeGraph.toString()));

        graphNodeService.delete(nodeGraph);
    }

    @Test
    public void exceptionThreeWhileDeleteNodeByNodeGraphTest() {

        thrown.expect(NotFoundException.class);
        thrown.expectMessage("Node null is not found");

        NodeGraph nodeGraph = null;
        graphNodeService.delete(nodeGraph);
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

        graphNodeService.delete("v1");
        Set<NodeGraph> actual = graphNodeService.getAll();
        Assert.assertEquals(4, actual.size());

        graphNodeService.delete("v1");
    }

    @Test
    public void exceptionOneWhileDeleteNodeByNameTest() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("Node with NAME = null is not found");

        String name = null;

        graphNodeService.delete(name);
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

        graphNodeService.delete(5000);
        Set<NodeGraph> actual = graphNodeService.getAll();
        Assert.assertEquals(4, actual.size());

        graphNodeService.delete(5000);
    }

    @Test
    public void exceptionOneWhileDeleteNodeByIdTest() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("Node with ID = null is not found");

        Integer id = null;

        graphNodeService.delete(id);
    }

    @Test
    public void createNodeTest() {
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
    public void exceptionWhileCreateNodeTest() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Node must not be null");

        graphNodeService.create(null);
    }

}
