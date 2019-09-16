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

import java.util.Set;

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

}
