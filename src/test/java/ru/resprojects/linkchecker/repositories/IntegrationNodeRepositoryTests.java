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
import java.util.stream.IntStream;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LinkcheckerApplication.class)
@ActiveProfiles(profiles = "test")
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
	scripts = {"classpath:schema-h2.sql", "classpath:data-h2.sql"},
	config = @SqlConfig(encoding = "UTF-8"))
public class IntegrationNodeRepositoryTests {

	private static final Logger LOG = LoggerFactory.getLogger(IntegrationNodeRepositoryTests.class);

	@Autowired
	NodeRepository nodeRepository;

	@Autowired
	EdgeRepository edgeRepository;

	@Test
	public void persistNewNodeTest() {
		Node node = new Node("v11");
		Node savedNode = nodeRepository.save(node);
		Assert.assertNotNull(savedNode.getId());
		LOG.info(nodeRepository.getByName("v11").toString());
		int count = nodeRepository.findAll().size();
		Assert.assertEquals(6, count);
		LOG.info("LIST count = " + count);
	}

	@Test
	public void persistNodeListTest() {
		List<Node> nodes = new ArrayList<>();
		IntStream.range(1, 6).forEach(i -> nodes.add(new Node("w" + i)));
		nodeRepository.saveAll(nodes);
		List<Node> savedNodes = nodeRepository.findAll();
		Assert.assertNotNull(savedNodes);
		Assert.assertEquals(10, savedNodes.size());
		LOG.info("LIST count = " + savedNodes.size());
		savedNodes.forEach(node -> LOG.info(node.toString()));
	}

	@Test
	public void getAllNodesTest() {
		List<Node> nodes = nodeRepository.findAll();
		Assert.assertNotNull(nodes);
		Assert.assertEquals(5, nodes.size());
		LOG.info("LIST count = " + nodes.size());
		nodes.forEach(node -> LOG.info(node.toString()));
	}

	@Test
	public void getNodeByIdTest() {
		Node node = nodeRepository.findById(5000).orElse(null);
		Assert.assertNotNull(node);
		Assert.assertEquals("v1", node.getName());
		LOG.info("NODE INFO WITH ID = 5000: " + node);
	}

	@Test
	public void nodeNotFoundByIdTest() {
		Node node = nodeRepository.findById(5010).orElse(null);
		Assert.assertNull(node);
	}

	@Test
	public void getNodeByNameTest() {
		Node node = nodeRepository.getByName("v1");
		Assert.assertNotNull(node);
		Assert.assertEquals("v1", node.getName());
		LOG.info("NODE INFO WITH ID = 5000: " + node);
	}

	@Test
	public void nodeNotFoundIfNameIsNullTest() {
		Node node = nodeRepository.getByName(null);
		Assert.assertNull(node);
	}

	@Test
	public void nodeNotFoundByNameTest() {
		Node node = nodeRepository.getByName("v11");
		Assert.assertNull(node);
	}

	@Test
	public void deleteByIdTest() {
		nodeRepository.deleteById(5000);
		List<Node> nodes = nodeRepository.findAll();
		Assert.assertNotNull(nodes);
		Assert.assertEquals(4, nodes.size());
		LOG.info("LIST count = " + nodes.size());
		nodes.forEach(node -> LOG.info(node.toString()));
	}

	@Test
	public void deleteByNameTest() {
		nodeRepository.deleteByName("v1");
		List<Node> nodes = nodeRepository.findAll();
		Assert.assertNotNull(nodes);
		Assert.assertEquals(4, nodes.size());
		LOG.info("LIST count = " + nodes.size());
		nodes.forEach(node -> LOG.info(node.toString()));
	}

	@Test
	public void cascadeDeleteAllNodesAndEdgesTest() {
		nodeRepository.deleteAllInBatch();
		List<Node> nodes = nodeRepository.findAll();
		Assert.assertNotNull(nodes);
		Assert.assertEquals(0, nodes.size());
		List<Edge> edges = edgeRepository.findAll();
		Assert.assertNotNull(edges);
		Assert.assertEquals(0, edges.size());
	}

	@Test
	public void existNodeByNameTest() {
		Assert.assertTrue(nodeRepository.existsByName("v1"));
	}
}
