package ru.resprojects.linkchecker.services;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import ru.resprojects.linkchecker.model.Edge;
import ru.resprojects.linkchecker.model.Node;
import ru.resprojects.linkchecker.repositories.EdgeRepository;
import ru.resprojects.linkchecker.repositories.NodeRepository;
import ru.resprojects.linkchecker.util.GraphUtil;
import ru.resprojects.linkchecker.util.exeptions.NotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.when;
import static ru.resprojects.linkchecker.dto.GraphDto.NodeGraph;
import static ru.resprojects.linkchecker.dto.GraphDto.EdgeGraph;

@RunWith(SpringRunner.class)
public class GraphServiceMockTests {

    private static final Logger LOG = LoggerFactory.getLogger(GraphServiceMockTests.class);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private GraphService graphService;

    @MockBean
    private GraphEdgeService edgeService;

    @MockBean
    private GraphNodeService nodeService;

    @Before
    public void init() {
        graphService = new GraphServiceImpl(edgeService, nodeService);
    }

    @Test
    public void stub() {
        LOG.debug("NOTHING");
    }

}
