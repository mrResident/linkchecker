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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static ru.resprojects.linkchecker.dto.GraphDto.EdgeGraph;

@RunWith(SpringRunner.class)
public class EdgeGraphServiceMockTests {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @MockBean
    private EdgeRepository edgeRepository;

    @MockBean
    private NodeRepository nodeRepository;

    private GraphEdgeService edgeService;

    @Before
    public void init() {
        edgeService = new GraphEdgeServiceImpl(edgeRepository, nodeRepository);
    }

    @Test
    public void getAllEdges() {
        List<Node> nodes = Stream.of(
            new Node(5000, "v1", 43, 0),
            new Node(5001, "v2", 60, 0),
            new Node(5002, "v3", 35, 0),
            new Node(5003, "v4", 56, 0),
            new Node(5004, "v5", 20, 0)
        ).collect(Collectors.toList());
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

}
