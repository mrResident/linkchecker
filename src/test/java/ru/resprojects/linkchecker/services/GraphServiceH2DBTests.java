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
import ru.resprojects.linkchecker.dto.GraphDto;
import ru.resprojects.linkchecker.util.exeptions.ApplicationException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.resprojects.linkchecker.dto.GraphDto.EdgeGraph;
import static ru.resprojects.linkchecker.dto.GraphDto.NodeGraph;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LinkcheckerApplication.class)
@ActiveProfiles(profiles = {"test", "debug"})
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
	scripts = {"classpath:schema-h2.sql", "classpath:data-h2.sql"},
	config = @SqlConfig(encoding = "UTF-8"))
public class GraphServiceH2DBTests {

	private static final Logger LOG = LoggerFactory.getLogger(GraphServiceH2DBTests.class);

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Autowired
	private GraphService graphService;

	@Autowired
	private AppProperties properties;

	private Set<EdgeGraph> edgeGraphSet = Stream.of(
		new EdgeGraph("v1", "v2"),
		new EdgeGraph("v1", "v3"),
		new EdgeGraph("v1", "v4"),
		new EdgeGraph("v2", "v3"),
		new EdgeGraph("v2", "v4"),
		new EdgeGraph("v3", "v4")
	).collect(Collectors.toSet());

	@Test
	public void createGraph() {
		GraphDto graphDto = new GraphDto();
		graphDto.setNodes(Stream.of(
			new NodeGraph("v1"),
			new NodeGraph("v2"),
			new NodeGraph("v3"),
			new NodeGraph("v4")
		).collect(Collectors.toSet()));
		graphDto.setEdges(edgeGraphSet);
		GraphDto actual = graphService.create(graphDto);
		Assert.assertNotNull(actual);
		LOG.info(actual.toString());
	}

	@Test
	public void createGraphWithExtraEdges() {
		GraphDto graphDto = new GraphDto();
		graphDto.setNodes(Stream.of(
			new NodeGraph("v1"),
			new NodeGraph("v2"),
			new NodeGraph("v3")
		).collect(Collectors.toSet()));
		graphDto.setEdges(edgeGraphSet);
		GraphDto actual = graphService.create(graphDto);
		Assert.assertNotNull(actual);
		LOG.info(actual.toString());
	}

	@Test
	public void createGraphNullArgumentException() {
		thrown.expect(ApplicationException.class);
		thrown.expectMessage(properties.getAppMsg().get("MSG_ARGUMENT_NULL"));
		graphService.create(null);
	}

	@Test
	public void createGraphEmptyCollectionException() {
		thrown.expect(ApplicationException.class);
		thrown.expectMessage("NODES: " + properties.getAppMsg().get("MSG_COLLECTION_EMPTY"));
		GraphDto graphDto = new GraphDto();
		graphDto.setNodes(new HashSet<>());
		graphDto.setEdges(edgeGraphSet);
		graphService.create(graphDto);
	}

	@Test
	public void getGraphWithRemovedEdges() {
		graphService.getEdges().create(Stream.of(
			new EdgeGraph("v2", "v3"),
			new EdgeGraph("v3", "v5"),
			new EdgeGraph("v2", "v4"),
			new EdgeGraph("v5", "v4")
		).collect(Collectors.toSet()));
		Set<EdgeGraph> edgeGraphs = graphService.getEdges().getAll();
		LOG.info(edgeGraphs.toString());
		GraphDto actual = graphService.get();
		Assert.assertNotNull(actual);
		Assert.assertNotEquals(edgeGraphs.size(), actual.getEdges().size());
		LOG.info(actual.toString());
	}

	@Test
	public void getGraphWithoutRemovingEdges() {
		GraphDto actual = graphService.get();
		Assert.assertNotNull(actual);
		Assert.assertEquals(4, actual.getEdges().size());
		LOG.info(actual.toString());
	}

	@Test
	public void deleteGraph() {
		graphService.clear();
		GraphDto actual = graphService.get();
		Assert.assertNotNull(actual);
		Assert.assertEquals(0, actual.getEdges().size());
		Assert.assertEquals(0, actual.getNodes().size());
	}

	@Test
	public void getGraphAfterAddedNewNode() {
		graphService.getNodes().create(new NodeGraph("v6"));
		GraphDto actual = graphService.get();
		Assert.assertNotNull(actual);
		Assert.assertEquals(6, actual.getNodes().size());
		Assert.assertEquals(4, actual.getEdges().size());
	}

	@Test
	public void checkNodesRoute() {
		Set<String> nodeNames = Stream.of("v1", "v2", "v3", "v5").collect(Collectors.toSet());
		int faultCount = 0;
		Map<String, Integer> nodeErrorStat = new HashMap<>();
		for (int i = 0; i < 100; i++) {
			try {
				LOG.info(graphService.checkRoute(nodeNames));
			} catch (ApplicationException e) {
				String node = e.getMessage().split(" ")[1];
				LOG.info(node);
				if (!nodeErrorStat.containsKey(node)) {
					nodeErrorStat.put(node, 1);
				} else {
					Integer val = nodeErrorStat.get(node);
					nodeErrorStat.put(node, ++val);
				}
				faultCount++;
			}
		}
		LOG.info(graphService.get().toString());
		LOG.info("FAULT COUNT for CHECK ROUTE = " + faultCount);
		nodeErrorStat.forEach((key, value) -> LOG.info("NODE " + key + " error count = " + value));
	}

	@Test
	public void checkRouteNullCollectionException() {
		thrown.expect(ApplicationException.class);
		thrown.expectMessage(properties.getAppMsg().get("MSG_ARGUMENT_NULL"));
		graphService.checkRoute(null);
	}

	@Test
	public void checkRouteEmptyCollectionException() {
		thrown.expect(ApplicationException.class);
		thrown.expectMessage(properties.getAppMsg().get("MSG_COLLECTION_EMPTY"));
		graphService.checkRoute(new HashSet<>());
	}

	@Test
	public void checkRouteCollectionHaveOneElementException() {
		Set<String> nodeNames = Stream.of("v10").collect(Collectors.toSet());
		thrown.expect(ApplicationException.class);
		thrown.expectMessage(properties.getAppMsg().get("MSG_COLLECTION_CONTAIN_ONE_ELEMENT"));
		graphService.checkRoute(nodeNames);
	}

}
