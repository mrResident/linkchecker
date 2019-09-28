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
import ru.resprojects.linkchecker.LinkcheckerApplication;
import ru.resprojects.linkchecker.util.Messages;
import ru.resprojects.linkchecker.util.exeptions.ApplicationException;
import ru.resprojects.linkchecker.util.exeptions.NotFoundException;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.resprojects.linkchecker.dto.GraphDto.EdgeGraph;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LinkcheckerApplication.class)
@ActiveProfiles(profiles = "test")
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
    scripts = {"classpath:schema-h2.sql", "classpath:data-h2.sql"},
    config = @SqlConfig(encoding = "UTF-8"))
public class GraphEdgeServiceH2DBTests {

    private static final Logger LOG = LoggerFactory.getLogger(GraphEdgeServiceH2DBTests.class);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private GraphEdgeService edgeService;

    @Test
    public void createEdge() {
        EdgeGraph edgeGraph = new EdgeGraph("v1", "v4");
        EdgeGraph actual = edgeService.create(edgeGraph);
        Assert.assertNotNull(actual);
        Assert.assertEquals(new Integer(5009), actual.getId());
        Set<EdgeGraph> egList = edgeService.getAll();
        Assert.assertEquals(5, egList.size());
        egList.forEach(eg -> LOG.debug("---- EDGE: " + eg));
    }

    @Test
    public void exceptionOneWhileCreateEdge() {
        thrown.expect(ApplicationException.class);
        thrown.expectMessage(Messages.MSG_ARGUMENT_NULL);
        edgeService.create((EdgeGraph) null);
    }

    @Test
    public void exceptionTwoWhileCreateEdge() {
        EdgeGraph edgeGraph = new EdgeGraph("v10", "v4");
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("Node with NAME = v10 is not found");
        edgeService.create(edgeGraph);
    }

    @Test
    public void exceptionThreeWhileCreateEdge() {
        EdgeGraph edgeGraph = new EdgeGraph("v1", "v2");
        thrown.expect(ApplicationException.class);
        thrown.expectMessage("Edge for nodes ([v1, v2], [v2, v1]) already present in the graph.");
        edgeService.create(edgeGraph);
    }

    @Test
    public void exceptionFourWhileCreateEdge() {
        EdgeGraph edgeGraph = new EdgeGraph("v2", "v1");
        thrown.expect(ApplicationException.class);
        thrown.expectMessage("Edge for nodes ([v2, v1], [v1, v2]) already present in the graph.");
        edgeService.create(edgeGraph);
    }

    @Test
    public void createEdges() {
        Set<EdgeGraph> edgeGraphs = Stream.of(
            new EdgeGraph("v2", "v3"),
            new EdgeGraph("v2", "v5"),
            new EdgeGraph("v3", "v5")
        ).collect(Collectors.toSet());
        Set<EdgeGraph> actual = edgeService.create(edgeGraphs);
        Assert.assertFalse(actual.isEmpty());
        Assert.assertFalse(actual.iterator().next().isNew());
        actual.forEach(eg -> LOG.debug("---- RETURNED EDGE: " + eg));
        Set<EdgeGraph> egList = edgeService.getAll();
        Assert.assertEquals(7, egList.size());
        egList.forEach(eg -> LOG.debug("---- EDGE: " + eg));
    }

    @Test
    public void exceptionOneWhileCreateEdges() {
        thrown.expect(ApplicationException.class);
        thrown.expectMessage("Collection does not be empty");
        edgeService.create(new HashSet<>());
    }

    @Test
    public void exceptionTwoWhileCreateEdges() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("Node with NAME = v21 is not found");
        Set<EdgeGraph> edgeGraphs = Stream.of(
            new EdgeGraph("v21", "v3"),
            new EdgeGraph("v2", "v5"),
            new EdgeGraph("v3", "v5")
        ).collect(Collectors.toSet());
        edgeService.create(edgeGraphs);
    }

    @Test
    public void exceptionThreeWhileCreateEdges() {
        thrown.expect(ApplicationException.class);
        thrown.expectMessage("Collection does not be contain null element");
        Set<EdgeGraph> edgeGraphs = Stream.of(
            null,
            new EdgeGraph("v2", "v5"),
            new EdgeGraph("v3", "v5")
        ).collect(Collectors.toSet());
        edgeService.create(edgeGraphs);
    }

    @Test
    public void exceptionFourWhileCreateEdges() {
        thrown.expect(ApplicationException.class);
        thrown.expectMessage("Edge for nodes ([v1, v2], [v2, v1]) already present in the graph.");
        Set<EdgeGraph> edgeGraphs = Stream.of(
            new EdgeGraph("v1", "v2"),
            new EdgeGraph("v2", "v5")
        ).collect(Collectors.toSet());
        edgeService.create(edgeGraphs);
    }

    @Test
    public void deleteEdgeById() {
        edgeService.delete(5005);
        Set<EdgeGraph> egList = edgeService.getAll();
        Assert.assertEquals(3, egList.size());
        egList.forEach(eg -> LOG.debug("---- EDGE: " + eg));
    }

    @Test
    public void exceptionWhileDeleteEdgeById() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("EDGE with ID = 5022 is not found");
        edgeService.delete(5022);
    }

    @Test
    public void deleteEdgeByNodeOneAndNodeTwoNames() {
        edgeService.delete("v1", "v2");
        Set<EdgeGraph> actual = edgeService.getAll();
        actual.forEach(eg -> LOG.debug("---- EDGE: " + eg));
    }

    @Test
    public void exceptionWhileDeleteEdgeByNodeOneAndNodeTwoNames() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("Edge for nodes [v15, v2] is not found");
        edgeService.delete("v15", "v2");
    }

    @Test
    public void deleteEdgeByNodeName() {
        edgeService.delete("v1");
        Set<EdgeGraph> actual = edgeService.getAll();
        actual.forEach(eg -> LOG.debug("---- EDGE: " + eg));
    }

    @Test
    public void exceptionWhileDeleteEdgeByNodeName() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("Edges for node v15 is not found");
        edgeService.delete("v15");
    }

    @Test
    public void deleteAllEdges() {
        edgeService.deleteAll();
        Set<EdgeGraph> actual = edgeService.getAll();
        Assert.assertTrue(actual.isEmpty());
    }

    @Test
    public void getAllEdges() {
        Set<EdgeGraph> actual = edgeService.getAll();
        Assert.assertEquals(4, actual.size());
        assertThat(actual.stream()
            .filter(eg -> eg.getId().equals(5007))
            .findFirst()
            .get().getNodeOne()).isEqualTo("v1");
        assertThat(actual.stream()
            .filter(eg -> eg.getId().equals(5007))
            .findFirst()
            .get().getNodeTwo()).isEqualTo("v5");
        actual.forEach(eg -> LOG.debug("---- EDGE: " + eg));
    }

    @Test
    public void getEdgeById() {
        EdgeGraph actual = edgeService.getById(5005);
        Assert.assertNotNull(actual);
        LOG.debug(actual.toString());
    }

    @Test
    public void getEdgesByNodeName() {
        Set<EdgeGraph> actual = edgeService.get("v1");
        Assert.assertFalse(actual.isEmpty());
        actual.forEach(eg -> LOG.debug("---- EDGE: " + eg));
    }

    @Test
    public void getEdgeByNodeNameOneAndNodeNameTwo() {
        EdgeGraph actual = edgeService.get("v1", "v2");
        Assert.assertNotNull(actual);
        Integer actualId = actual.getId();
        LOG.debug(actual.toString());
        actual = edgeService.get("v2", "v1");
        Assert.assertEquals(actualId, actual.getId()); //must equal because graph is undirected
    }

}
