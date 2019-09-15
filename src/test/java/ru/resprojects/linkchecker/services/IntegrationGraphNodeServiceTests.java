package ru.resprojects.linkchecker.services;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.TransactionSystemException;
import ru.resprojects.linkchecker.LinkcheckerApplication;
import ru.resprojects.linkchecker.util.exeptions.NotFoundException;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.resprojects.linkchecker.dto.GraphDto.NodeGraph;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LinkcheckerApplication.class)
@ActiveProfiles(profiles = "test")
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
    scripts = {"classpath:schema-h2.sql", "classpath:data-h2.sql"},
    config = @SqlConfig(encoding = "UTF-8"))
public class IntegrationGraphNodeServiceTests {

    private static final Logger LOG = LoggerFactory.getLogger(IntegrationGraphNodeServiceTests.class);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    GraphNodeService nodeService;

    @Test
    public void getNodeByName() {
        NodeGraph nodeGraph = nodeService.get("v1");
        Assert.assertNotNull(nodeGraph);
        Assert.assertEquals("v1", nodeGraph.getName());
        LOG.debug("NODE DTO: " + nodeGraph);
    }

    @Test
    public void getNodeById() {
        NodeGraph nodeGraph = nodeService.getById(5000);
        Assert.assertNotNull(nodeGraph);
        Assert.assertEquals("v1", nodeGraph.getName());
        LOG.debug("NODE DTO: " + nodeGraph);
    }

    @Test
    public void getNodeByNameNotFound() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("Node with name v11 is not found");
        nodeService.get("v11");
    }

    @Test
    public void getNodeByIdNotFound() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("Node with ID = 5050 is not found");
        nodeService.getById(5050);
    }

    @Test
    public void getAllNodes() {
        Set<NodeGraph> actual = nodeService.getAll();

        Assert.assertEquals(5, actual.size());
        assertThat(actual.stream()
            .filter(eg -> eg.getId().equals(5000))
            .findFirst()
            .get().getName()).isEqualTo("v1");
        actual.forEach(ng -> LOG.debug("---- NODE: " + ng));
    }

    @Test
    public void deleteNodeByNodeGraph() {
        NodeGraph nodeGraph = new NodeGraph(5000, "v1", 43, 0);
        nodeService.delete(nodeGraph);
        Set<NodeGraph> actual = nodeService.getAll();
        Assert.assertEquals(4, actual.size());
    }

    @Test
    public void exceptionOneWhileDeleteNodeByNodeGraph() {
        NodeGraph nodeGraph = new NodeGraph(5020, "v1", 43, 0);
        thrown.expect(NotFoundException.class);
        thrown.expectMessage(String.format("Node %s is not found", nodeGraph.toString()));
        nodeService.delete(nodeGraph);
    }

    @Test
    public void exceptionTwoWhileDeleteNodeByNodeGraph() {
        NodeGraph nodeGraph = new NodeGraph(5000, "v1", 43, 1);
        thrown.expect(NotFoundException.class);
        thrown.expectMessage(String.format("Node %s is not found", nodeGraph.toString()));
        nodeService.delete(nodeGraph);
    }

    @Test
    public void deleteNodeByName() {
        nodeService.delete("v1");
        Set<NodeGraph> actual = nodeService.getAll();
        Assert.assertEquals(4, actual.size());
    }

    @Test
    public void exceptionWhileDeleteNodeByName() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("Node with NAME = v10 is not found");
        nodeService.delete("v10");
    }

    @Test
    public void deleteNodeById() {
        nodeService.delete(5000);
        Set<NodeGraph> actual = nodeService.getAll();
        Assert.assertEquals(4, actual.size());
    }

    @Test
    public void exceptionWhileDeleteNodeById() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("Node with ID = 5100 is not found");
        nodeService.delete(5100);
    }

    @Test
    public void createNode() {
        NodeGraph nodeGraph = new NodeGraph("v6");
        nodeService.create(nodeGraph);
        NodeGraph actual = nodeService.get("v6");
        Assert.assertNotNull(actual);
        Set<NodeGraph> nodeGraphs = nodeService.getAll();
        nodeGraphs.forEach(ng -> LOG.debug("---- NODE: " + ng));
    }

    @Test
    public void exceptionWhileCreateNode() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Node must not be null");
        nodeService.create(null);
    }

    @Test
    public void nodeUpdate() {
        NodeGraph nodeGraph = new NodeGraph(5000, "v1", 23, 1);
        nodeService.update(nodeGraph);
        NodeGraph actual = nodeService.get("v1");
        Assert.assertNotNull(actual);
        Assert.assertEquals(23, actual.getProbability(), 0);
        Assert.assertEquals(1, actual.getCounter(), 0);
        Set<NodeGraph> nodeGraphs = nodeService.getAll();
        nodeGraphs.forEach(ng -> LOG.debug("---- NODE: " + ng));
    }

    @Test
    public void exceptionOneWhileNodeUpdate() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Node must not be null");
        nodeService.update(null);
    }

}
