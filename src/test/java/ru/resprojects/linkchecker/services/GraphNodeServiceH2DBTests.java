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
import ru.resprojects.linkchecker.AppProperties;
import ru.resprojects.linkchecker.LinkcheckerApplication;
import ru.resprojects.linkchecker.util.exeptions.ApplicationException;
import ru.resprojects.linkchecker.util.exeptions.NotFoundException;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.resprojects.linkchecker.dto.GraphDto.NodeGraph;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LinkcheckerApplication.class)
@ActiveProfiles(profiles = "test")
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
    scripts = {"classpath:schema-h2.sql", "classpath:data-h2.sql"},
    config = @SqlConfig(encoding = "UTF-8"))
public class GraphNodeServiceH2DBTests {

    private static final Logger LOG = LoggerFactory.getLogger(GraphNodeServiceH2DBTests.class);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    GraphNodeService nodeService;

    @Autowired
    AppProperties properties;

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
    public void getNodeByNameNotFoundException() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage( String.format(properties.getNodeMsg().get("NODE_MSG_BY_NAME_ERROR"), "v11"));
        nodeService.get("v11");
    }

    @Test
    public void getNodeByIdNotFoundException() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage(String.format(properties.getAppMsg().get("MSG_BY_ID_ERROR"), "NODE", 5050));
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
        NodeGraph nodeGraph = new NodeGraph(5000, "v1", 0);
        nodeService.delete(nodeGraph);
        Set<NodeGraph> actual = nodeService.getAll();
        Assert.assertEquals(4, actual.size());
    }

    @Test
    public void deleteNodeByNodeGraphNotFoundException() {
        NodeGraph nodeGraph = new NodeGraph(5020, "v1", 0);
        thrown.expect(NotFoundException.class);
        thrown.expectMessage(String.format(properties.getNodeMsg().get("NODE_MSG_BY_OBJECT_ERROR"), nodeGraph.toString()));
        nodeService.delete(nodeGraph);
    }

    @Test
    public void deleteNodeByNodeGraphAnotherNotFoundException() {
        NodeGraph nodeGraph = new NodeGraph(5000, "v1", 1);
        thrown.expect(NotFoundException.class);
        thrown.expectMessage(String.format(properties.getNodeMsg().get("NODE_MSG_BY_OBJECT_ERROR"), nodeGraph.toString()));
        nodeService.delete(nodeGraph);
    }

    @Test
    public void deleteNodeByName() {
        nodeService.delete("v1");
        Set<NodeGraph> actual = nodeService.getAll();
        Assert.assertEquals(4, actual.size());
    }

    @Test
    public void deleteNodeByNameNotFoundException() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage( String.format(properties.getNodeMsg().get("NODE_MSG_BY_NAME_ERROR"), "v10"));
        nodeService.delete("v10");
    }

    @Test
    public void deleteNodeById() {
        nodeService.delete(5000);
        Set<NodeGraph> actual = nodeService.getAll();
        Assert.assertEquals(4, actual.size());
    }

    @Test
    public void deleteNodeByIdNotFoundException() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage(String.format(properties.getAppMsg().get("MSG_BY_ID_ERROR"), "NODE", 5100));
        nodeService.delete(5100);
    }

    @Test
    public void deleteAllNodes() {
        nodeService.deleteAll();
        Set<NodeGraph> nodeGraphs = nodeService.getAll();
        Assert.assertNotNull(nodeGraphs);
        Assert.assertEquals(0, nodeGraphs.size());
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
    public void createNodes() {
        Set<NodeGraph> nodeGraphs = new HashSet<>();
        IntStream.range(1, 6).forEach(i -> {
            nodeGraphs.add(new NodeGraph("w" + i));
        });
        nodeService.create(nodeGraphs);
        Set<NodeGraph> actual = nodeService.getAll();
        Assert.assertNotNull(actual);
        Assert.assertEquals(10, actual.size());
        Assert.assertEquals("w1", actual.stream()
            .filter(ng -> "w1".equals(ng.getName()))
            .findFirst().get().getName());
        actual.forEach(ng -> LOG.debug("---- NODE: " + ng));
    }

    @Test
    public void createNodeNullArgumentException() {
        thrown.expect(ApplicationException.class);
        thrown.expectMessage(properties.getAppMsg().get("MSG_ARGUMENT_NULL"));
        nodeService.create((NodeGraph) null);
    }

    @Test
    public void nodeUpdate() {
        NodeGraph nodeGraph = new NodeGraph(5000, "v1", 1);
        nodeService.update(nodeGraph);
        NodeGraph actual = nodeService.get("v1");
        Assert.assertNotNull(actual);
        Assert.assertEquals(1, actual.getCounter(), 0);
        Set<NodeGraph> nodeGraphs = nodeService.getAll();
        nodeGraphs.forEach(ng -> LOG.debug("---- NODE: " + ng));
    }

    @Test
    public void nodeUpdateNullArgumentException() {
        thrown.expect(ApplicationException.class);
        thrown.expectMessage(properties.getAppMsg().get("MSG_ARGUMENT_NULL"));
        nodeService.update(null);
    }

}
