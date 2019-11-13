package ru.resprojects.linkchecker.repositories;

import org.junit.Assert;
import org.junit.Test;
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
import ru.resprojects.linkchecker.model.Edge;
import ru.resprojects.linkchecker.model.Node;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LinkcheckerApplication.class)
@ActiveProfiles(profiles = {"test", "debug"})
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
	scripts = {"classpath:schema-h2.sql", "classpath:data-h2.sql"},
	config = @SqlConfig(encoding = "UTF-8"))
public class EdgeRepositoryH2DBTests {

	private static final Logger LOG = LoggerFactory.getLogger(EdgeRepositoryH2DBTests.class);

	@Autowired
	EdgeRepository edgeRepository;

	@Autowired
	NodeRepository nodeRepository;

	@Test
	public void persistNewEdge() {
		Node nodeOne = nodeRepository.save(new Node("v6"));
		Node nodeTwo = nodeRepository.getByName("v5");
		Edge edge = edgeRepository.save(new Edge(nodeOne, nodeTwo));
		Assert.assertNotNull(edge);
		LOG.info("New Edge: " + edge);
		List<Edge> edges = edgeRepository.findAll();
		Assert.assertNotNull(edges);
		Assert.assertEquals(5, edges.size());
		LOG.info("EDGES count = " + edges.size());
	}

	@Test
	public void persistEdgeList() {
		List<Node> nodes = nodeRepository.findAll();
		Node nodeOne = nodeRepository.save(new Node("v6"));
		List<Edge> edges = new ArrayList<>();
		nodes.forEach(node -> edges.add(new Edge(nodeOne, node)));
		edgeRepository.saveAll(edges);
		List<Edge> newEdgeList = edgeRepository.findAll();
		Assert.assertNotNull(newEdgeList);
		Assert.assertEquals(9, newEdgeList.size());
		LOG.info("Size: " + newEdgeList.size());
		newEdgeList.forEach(edge -> LOG.info(edge.toString()));
	}

	@Test
	public void getAllEdges() {
		List<Edge> edges = edgeRepository.findAll();
		Assert.assertNotNull(edges);
		Assert.assertEquals(4, edges.size());
		LOG.info("LIST count = " + edges.size());
		edges.forEach(edge -> LOG.info(edge.toString()));
	}

	@Test
	public void getEdgeById() {
		Edge edge = edgeRepository.findById(5005).orElse(null);
		Assert.assertNotNull(edge);
		Assert.assertEquals("v1", edge.getNodeOne().getName());
		LOG.info("EDGE INFO WITH ID = 5005: " + edge);
	}

	@Test
	public void getEdgeByNodeOneAndNodeTwo() {
		Node nodeOne = nodeRepository.getByName("v1");
		Node nodeTwo = nodeRepository.getByName("v2");
		Edge edge = edgeRepository.findEdgeByNodeOneAndNodeTwo(nodeOne, nodeTwo).orElse(null);
		Assert.assertNotNull(edge);
		Assert.assertEquals(new Integer(5005), edge.getId());
		LOG.info("EDGE FOR NODES v1 and v2: " + edge);
	}

	@Test
	public void getEdgeByNodeOneOrNodeTwo() {
		Node nodeOne = nodeRepository.getByName("v1");
		Node nodeTwo = nodeRepository.getByName("v2");
		List<Edge> edges = edgeRepository.findEdgesByNodeOneOrNodeTwo(nodeOne, null);
		Assert.assertNotNull(edges);
		Assert.assertNotEquals(0, edges.size());
		edges.forEach(edge -> LOG.info(edge.toString()));
		LOG.info("-------------");
		edges = edgeRepository.findEdgesByNodeOneOrNodeTwo(null, nodeTwo);
		Assert.assertNotNull(edges);
		Assert.assertNotEquals(0, edges.size());
		edges.forEach(edge -> LOG.info(edge.toString()));
		LOG.info("-------------");
		edges = edgeRepository.findEdgesByNodeOneOrNodeTwo(nodeOne, nodeTwo);
		Assert.assertNotNull(edges);
		Assert.assertNotEquals(0, edges.size());
		edges.forEach(edge -> LOG.info(edge.toString()));
		LOG.info("-------------");
		edges = edgeRepository.findEdgesByNodeOneOrNodeTwo(nodeTwo, nodeTwo);
		Assert.assertNotNull(edges);
		Assert.assertNotEquals(0, edges.size());
		edges.forEach(edge -> LOG.info(edge.toString()));
	}

	@Test
	public void deleteEdgesInBatch() {
		Node node = nodeRepository.getByName("v1");
		List<Edge> edges = edgeRepository.findEdgesByNodeOneOrNodeTwo(node, node);
		Assert.assertNotNull(edges);
		edgeRepository.deleteInBatch(edges);
		edges = edgeRepository.findAll();
		Assert.assertNotNull(edges);
		edges.forEach(edge -> LOG.info(edge.toString()));
	}

	@Test
	public void deleteById() {
		edgeRepository.deleteById(5005);
		List<Edge> edges = edgeRepository.findAll();
		Assert.assertNotNull(edges);
		Assert.assertEquals(3, edges.size());
		LOG.info("EDGES count = " + edges.size());
	}

	@Test
	public void deleteAllEdges() {
		edgeRepository.deleteAllInBatch();
		List<Edge> edges = edgeRepository.findAll();
		Assert.assertNotNull(edges);
		Assert.assertEquals(0, edges.size());
	}

}
