package ru.resprojects.linkchecker.services;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import ru.resprojects.linkchecker.LinkcheckerApplication;
import ru.resprojects.linkchecker.dto.GraphDto;
import ru.resprojects.linkchecker.model.AbstractNamedEntity;
import ru.resprojects.linkchecker.model.Node;
import ru.resprojects.linkchecker.util.GraphUtil;
import ru.resprojects.linkchecker.util.Messages;
import ru.resprojects.linkchecker.util.exeptions.ApplicationException;
import ru.resprojects.linkchecker.util.exeptions.ErrorPlaceType;
import ru.resprojects.linkchecker.util.exeptions.ErrorType;
import ru.resprojects.linkchecker.util.exeptions.NotFoundException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.resprojects.linkchecker.dto.GraphDto.EdgeGraph;
import static ru.resprojects.linkchecker.dto.GraphDto.NodeGraph;
import static ru.resprojects.linkchecker.util.GraphUtil.getRandomEvent;
import static ru.resprojects.linkchecker.util.GraphUtil.graphBuilder;
import static ru.resprojects.linkchecker.util.GraphUtil.nodeGraphToNode;
import static ru.resprojects.linkchecker.util.Messages.MSG_ARGUMENT_NULL;
import static ru.resprojects.linkchecker.util.Messages.MSG_COLLECTION_EMPTY;
import static ru.resprojects.linkchecker.util.Messages.NODE_MSG_BY_NAME_ERROR;
import static ru.resprojects.linkchecker.util.Messages.NODE_MSG_NOT_REACHABLE;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LinkcheckerApplication.class)
@ActiveProfiles(profiles = "test")
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
	scripts = {"classpath:schema-h2.sql", "classpath:data-h2.sql"},
	config = @SqlConfig(encoding = "UTF-8"))
public class GraphServiceH2DBTests {

	private static final Logger LOG = LoggerFactory.getLogger(GraphServiceH2DBTests.class);

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Autowired
	GraphService graphService;

	@Test
	public void createGraph() {
		GraphDto graphDto = new GraphDto();
		graphDto.setNodes(Stream.of(
			new NodeGraph("v1"),
			new NodeGraph("v2"),
			new NodeGraph("v3"),
			new NodeGraph("v4")
		).collect(Collectors.toSet()));
		graphDto.setEdges(Stream.of(
			new EdgeGraph("v1", "v2"),
			new EdgeGraph("v1", "v3"),
			new EdgeGraph("v1", "v4"),
			new EdgeGraph("v2", "v3"),
			new EdgeGraph("v2", "v4"),
			new EdgeGraph("v3", "v4")
		).collect(Collectors.toSet()));
		GraphDto actual = graphService.create(graphDto);
		Assert.assertNotNull(actual);
		LOG.debug(actual.toString());
	}

	@Test
	public void createGraphWithExtraEdges() {
		GraphDto graphDto = new GraphDto();
		graphDto.setNodes(Stream.of(
			new NodeGraph("v1"),
			new NodeGraph("v2"),
			new NodeGraph("v3")
		).collect(Collectors.toSet()));
		graphDto.setEdges(Stream.of(
			new EdgeGraph("v1", "v2"),
			new EdgeGraph("v1", "v3"),
			new EdgeGraph("v1", "v4"),
			new EdgeGraph("v2", "v3"),
			new EdgeGraph("v2", "v4"),
			new EdgeGraph("v3", "v4")
		).collect(Collectors.toSet()));
		GraphDto actual = graphService.create(graphDto);
		Assert.assertNotNull(actual);
		LOG.debug(actual.toString());
	}

	@Test
	public void exceptionOneWhileCreateGraph() {
		thrown.expect(ApplicationException.class);
		thrown.expectMessage(Messages.MSG_ARGUMENT_NULL);
		graphService.create(null);
	}

	@Test
	public void exceptionTwoWhileCreateGraph() {
		thrown.expect(ApplicationException.class);
		thrown.expectMessage("NODES: " + Messages.MSG_COLLECTION_EMPTY);
		GraphDto graphDto = new GraphDto();
		graphDto.setNodes(new HashSet<>());
		graphDto.setEdges(Stream.of(
			new EdgeGraph("v1", "v2"),
			new EdgeGraph("v1", "v3"),
			new EdgeGraph("v1", "v4"),
			new EdgeGraph("v2", "v3"),
			new EdgeGraph("v2", "v4"),
			new EdgeGraph("v3", "v4")
		).collect(Collectors.toSet()));
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
		LOG.debug(edgeGraphs.toString());
		GraphDto actual = graphService.get();
		Assert.assertNotNull(actual);
		Assert.assertNotEquals(edgeGraphs.size(), actual.getEdges().size());
		LOG.debug(actual.toString());
	}

	@Test
	public void getGraphWithoutRemovingEdges() {
		GraphDto actual = graphService.get();
		Assert.assertNotNull(actual);
		Assert.assertEquals(4, actual.getEdges().size());
		LOG.debug(actual.toString());
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
		graphService.getNodes().create(new NodeGraph("v6", 53));
		GraphDto actual = graphService.get();
		Assert.assertNotNull(actual);
		Assert.assertEquals(6, actual.getNodes().size());
		Assert.assertEquals(4, actual.getEdges().size());
	}

	@Test
	public void exceptionOneCheckRoute() {
		Set<String> nodeNames = Stream.of("v1", "v2", "v3", "v7").collect(Collectors.toSet());
		thrown.expect(NotFoundException.class);
		graphService.checkRoute(nodeNames);
	}

	@Test
	public void findPath() {
		List<String> nodeNameList = Stream.of("v1", "v2", "v4").collect(Collectors.toList());
		GraphDto graphDto = graphService.get();
		Graph<Node, DefaultEdge> graph = graphBuilder(graphDto.getNodes(),
			graphDto.getEdges());
		DijkstraShortestPath<Node, DefaultEdge> dAlg = new DijkstraShortestPath<>(graph);
		Node firstNode = nodeGraphToNode(graphDto.getNodes().stream()
			.filter(ng -> ng.getName().equalsIgnoreCase(nodeNameList.get(0)))
			.findFirst()
			.orElse(null));
		if (Objects.isNull(firstNode)) {
			throw new NotFoundException(
				String.format(NODE_MSG_BY_NAME_ERROR, nodeNameList.get(0)),
				ErrorPlaceType.GRAPH
			);
		}
		if (getRandomEvent(firstNode.getProbability())) {
			LOG.debug("NODE " + firstNode.getName() + " get crash");
			return;
		}
		ShortestPathAlgorithm.SingleSourcePaths<Node, DefaultEdge> paths = dAlg.getPaths(firstNode);
		for (String name : nodeNameList) {
			if (name.equals(nodeNameList.get(0))) {
				continue;
			}
			Node nextNode = nodeGraphToNode(graphDto.getNodes().stream()
				.filter(ng -> ng.getName().equalsIgnoreCase(name))
				.findFirst()
				.orElse(null));
			if (Objects.isNull(nextNode)) {
				throw new NotFoundException(
					String.format(NODE_MSG_BY_NAME_ERROR, nodeNameList.get(0)),
					ErrorPlaceType.GRAPH
				);
			}
			LOG.debug("NEXT NODE: " + nextNode.getName());
			List<Node> findNodes = paths.getPath(nextNode).getVertexList();
			List<String> findNodesName = findNodes.stream()
				.map(AbstractNamedEntity::getName)
				.collect(Collectors.toList());
			if (!nodeNameList.containsAll(findNodesName)) {
				throw new NotFoundException(
					String.format(NODE_MSG_NOT_REACHABLE, nodeNameList.get(0), name),
					ErrorPlaceType.GRAPH
				);
			}
			if (getRandomEvent(nextNode.getProbability())) {
				LOG.debug("NODE " + nextNode.getName() + " get crash");
				break;
			}
		}
		LOG.debug("THE END");
	}
}
