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
import ru.resprojects.linkchecker.repositories.EdgeRepository;
import ru.resprojects.linkchecker.repositories.NodeRepository;
import ru.resprojects.linkchecker.util.exeptions.NotFoundException;

import static ru.resprojects.linkchecker.dto.GraphDto.NodeGraph;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LinkcheckerApplication.class)
@ActiveProfiles(profiles = "test")
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
	scripts = {"classpath:schema-h2.sql", "classpath:data-h2.sql"},
	config = @SqlConfig(encoding = "UTF-8"))
public class IntegrationGraphServiceTests {

	private static final Logger LOG = LoggerFactory.getLogger(IntegrationGraphServiceTests.class);

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Autowired
	NodeRepository nodeRepository;

	@Autowired
	EdgeRepository edgeRepository;

	@Autowired
	GraphService graphService;

	@Test
	public void getNodeByNameTest() {
		NodeGraph nodeGraph = graphService.getNode("v1");

		Assert.assertNotNull(nodeGraph);
		Assert.assertEquals("v1", nodeGraph.getName());

		LOG.debug("NODE DTO: " + nodeGraph);
	}

	@Test
	public void getNodeByIdTest() {
		NodeGraph nodeGraph = graphService.getNodeById(5000);

		Assert.assertNotNull(nodeGraph);
		Assert.assertEquals("v1", nodeGraph.getName());

		LOG.debug("NODE DTO: " + nodeGraph);
	}

	@Test
	public void getNodeByNameNotFoundTest() {
		thrown.expect(NotFoundException.class);
		thrown.expectMessage("Node with name v11 is not found");

		graphService.getNode("v11");
	}

	@Test
	public void getNodeByIdNotFoundTest() {
		thrown.expect(NotFoundException.class);
		thrown.expectMessage("Node with ID = 5050 is not found");

		graphService.getNodeById(5050);
	}

}
