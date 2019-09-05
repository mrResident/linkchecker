package ru.resprojects.linkchecker.repositories;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import ru.resprojects.linkchecker.LinkcheckerApplication;
import ru.resprojects.linkchecker.model.Node;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LinkcheckerApplication.class)
@ActiveProfiles(profiles = "test")
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
	scripts = {"classpath:schema-h2.sql", "classpath:data-h2.sql"},
	config = @SqlConfig(encoding = "UTF-8"))
public class RepositoriesTests {

	@Autowired
	EdgeRepository edgeRepository;

	@Autowired
	NodeRepository nodeRepository;

	@Test
	public void test1() {
		Node node = new Node(null, "v11", 0.5f, 1);
		nodeRepository.save(node);
		System.out.println(nodeRepository.getByName("v11"));
	}

	@Test
	public void test2() {
		System.out.println(nodeRepository.getByName("v11"));
		System.out.println(nodeRepository.getByName("v1"));
	}

	@Test
	public void test3() {
		System.out.println(edgeRepository.findEdgeById(5005));
	}

}
