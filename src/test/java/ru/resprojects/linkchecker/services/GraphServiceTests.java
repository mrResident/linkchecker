package ru.resprojects.linkchecker.services;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import ru.resprojects.linkchecker.model.Node;
import ru.resprojects.linkchecker.repositories.EdgeRepository;
import ru.resprojects.linkchecker.repositories.NodeRepository;
import ru.resprojects.linkchecker.util.exeptions.NotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static ru.resprojects.linkchecker.dto.GraphDto.NodeGraph;

@RunWith(SpringRunner.class)
public class GraphServiceTests {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private GraphService graphService;

    @MockBean
    private EdgeRepository edgeRepository;

    @MockBean
    private NodeRepository nodeRepository;

    @Before
    public void init() {
        graphService = new GraphServiceImpl(edgeRepository, nodeRepository);
    }


    @Test
    public void getNodeByNameTest() {
        given(nodeRepository.getByName("v1")).willReturn(
            new Node(5000, "v1", 0.5f, 0)
        );

        NodeGraph actual = graphService.getNode("v1");

        assertThat(actual.getName()).isEqualTo("v1");
        assertThat(actual.getProbability()).isEqualTo(0.5f);
        assertThat(actual.getCounter()).isEqualTo(0);
    }

    @Test
    public void getNodeByNameNotFoundTest() throws NotFoundException {

        thrown.expect(NotFoundException.class);
        thrown.expectMessage("Node with name v1 is not found");

        graphService.getNode("v1");
    }

    @Test
    public void getNodeByIdTest() {
        given(nodeRepository.findById(5000)).willReturn(
            Optional.of(new Node(5000, "v1", 0.5f, 0))
        );

        NodeGraph actual = graphService.getNodeById(5000);

        assertThat(actual.getName()).isEqualTo("v1");
        assertThat(actual.getProbability()).isEqualTo(0.5f);
        assertThat(actual.getCounter()).isEqualTo(0);
    }

    @Test
    public void getNodeByIdNotFoundTest() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage("Node with ID = 5000 is not found");

        graphService.getNodeById(5000);
    }
}
