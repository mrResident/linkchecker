package ru.resprojects.linkchecker.services;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import ru.resprojects.linkchecker.util.GraphUtil;
import ru.resprojects.linkchecker.util.exeptions.NotFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.when;
import static org.powermock.api.mockito.PowerMockito.spy;
import static ru.resprojects.linkchecker.dto.GraphDto.NodeGraph;
import static ru.resprojects.linkchecker.dto.GraphDto.EdgeGraph;
import static ru.resprojects.linkchecker.util.Messages.NODE_MSG_BY_NAME_ERROR;
import static ru.resprojects.linkchecker.util.Messages.NODE_MSG_IS_FAULT;
import static ru.resprojects.linkchecker.util.Messages.NODE_MSG_NOT_REACHABLE;

//How to use PowerMock https://www.baeldung.com/intro-to-powermock
//How to use PowerMock and SpringRunner https://stackoverflow.com/a/57780838
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@PrepareForTest(GraphUtil.class)
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "javax.xml.transform.*", "org.xml.*", "javax.management.*", "javax.net.ssl.*", "com.sun.org.apache.xalan.internal.xsltc.trax.*"})
public class GraphServiceMockTests {

    private static final Logger LOG = LoggerFactory.getLogger(GraphServiceMockTests.class);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private GraphService graphService;

    @MockBean
    private GraphEdgeService edgeService;

    @MockBean
    private GraphNodeService nodeService;

    private Set<NodeGraph> nodeGraphSet;
    private Set<EdgeGraph> edgeGraphSet;

    @Before
    public void init() {
        graphService = new GraphServiceImpl(edgeService, nodeService);
        nodeGraphSet = Stream.of(
            new NodeGraph(5000, "v1", 0),
            new NodeGraph(5001, "v2", 0),
            new NodeGraph(5002, "v3", 0),
            new NodeGraph(5003, "v4", 0),
            new NodeGraph(5004, "v5", 0)
        ).collect(Collectors.toSet());
        edgeGraphSet = Stream.of(
            new EdgeGraph(5005, "v1", "v2"),
            new EdgeGraph(5006, "v1", "v3"),
            new EdgeGraph(5007, "v1", "v5"),
            new EdgeGraph(5008, "v3", "v4")
        ).collect(Collectors.toSet());
    }

    @Test
    public void checkRouteNodeFaultException() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage(String.format(NODE_MSG_IS_FAULT, "v1"));
        spy(GraphUtil.class);
        Map<String, Boolean> nodesFault = new HashMap<>();
        nodesFault.put("v1", true);
        nodesFault.put("v2", false);
        nodesFault.put("v3", false);
        given(nodeService.getAll()).willReturn(nodeGraphSet);
        given(edgeService.getAll()).willReturn(edgeGraphSet);
        when(GraphUtil.getRandomNodeFault(anyCollection())).thenReturn(nodesFault);
        graphService.checkRoute(Stream.of("v1", "v2", "v3").collect(Collectors.toSet()));
    }

    @Test
    public void checkRouteNodeNotReachableException() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage(String.format(NODE_MSG_NOT_REACHABLE, "v1", "v4"));
        spy(GraphUtil.class);
        Map<String, Boolean> nodesFault = new HashMap<>();
        nodesFault.put("v1", false);
        nodesFault.put("v2", false);
        nodesFault.put("v3", false);
        nodesFault.put("v4", false);
        given(nodeService.getAll()).willReturn(nodeGraphSet);
        given(edgeService.getAll()).willReturn(edgeGraphSet);
        when(GraphUtil.getRandomNodeFault(anyCollection())).thenReturn(nodesFault);
        graphService.checkRoute(Stream.of("v1", "v2", "v4").collect(Collectors.toSet()));
    }

    @Test
    public void checkRouteNodeNotFoundException() {
        thrown.expect(NotFoundException.class);
        thrown.expectMessage(String.format(NODE_MSG_BY_NAME_ERROR, "v7"));
        spy(GraphUtil.class);
        Map<String, Boolean> nodesFault = new HashMap<>();
        nodesFault.put("v1", false);
        nodesFault.put("v2", false);
        nodesFault.put("v3", false);
        given(nodeService.getAll()).willReturn(nodeGraphSet);
        given(edgeService.getAll()).willReturn(edgeGraphSet);
        when(GraphUtil.getRandomNodeFault(anyCollection())).thenReturn(nodesFault);
        graphService.checkRoute(Stream.of("v1", "v2", "v3", "v7").collect(Collectors.toSet()));
    }

}
