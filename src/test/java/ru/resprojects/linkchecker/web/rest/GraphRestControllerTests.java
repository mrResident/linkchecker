package ru.resprojects.linkchecker.web.rest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.resprojects.linkchecker.AppProperties;
import ru.resprojects.linkchecker.LinkcheckerApplication;
import ru.resprojects.linkchecker.TestUtils;
import ru.resprojects.linkchecker.dto.GraphDto;
import ru.resprojects.linkchecker.util.exeptions.ErrorInfo;
import ru.resprojects.linkchecker.util.exeptions.ErrorPlaceType;
import ru.resprojects.linkchecker.util.exeptions.ErrorType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.resprojects.linkchecker.dto.GraphDto.NodeGraph;
import static ru.resprojects.linkchecker.dto.GraphDto.EdgeGraph;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LinkcheckerApplication.class)
@ActiveProfiles(profiles = "test")
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
    scripts = {"classpath:schema-h2.sql", "classpath:data-h2.sql"},
    config = @SqlConfig(encoding = "UTF-8"))
@WebAppConfiguration
public class GraphRestControllerTests {

    private static final Logger LOG = LoggerFactory.getLogger(GraphRestControllerTests.class);

    private MockMvc mvc;

    @Autowired
    WebApplicationContext webContext;

    @Autowired
    private AppProperties properties;

    @Before
    public void init() {
        mvc = MockMvcBuilders.webAppContextSetup(webContext).build();
    }

    @Test
    public void getGraph() throws Exception {
        this.mvc.perform(get(GraphRestController.REST_URL).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(TestUtils.mapToJson(TestUtils.graph)));
    }

    @Test
    public void exportGraphToGraphVizFormat() throws Exception {
        MvcResult result = this.mvc.perform(get(GraphRestController.REST_URL + "/export").accept(MediaType.TEXT_HTML_VALUE))
            .andReturn();
        Assert.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        String content = result.getResponse().getContentAsString();
        Assert.assertFalse(content.isEmpty());
        Assert.assertTrue(content.contains("strict graph G"));
    }

    @Test
    public void createGraph() throws Exception {
        Set<NodeGraph> nodesGraph = TestUtils.nodesGraph.stream()
            .map(ng -> new GraphDto.NodeGraph(ng.getName()))
            .collect(Collectors.toSet());
        Set<EdgeGraph> edgesGraph = TestUtils.edgesGraph.stream()
            .map(eg -> new EdgeGraph(eg.getNodeOne(), eg.getNodeTwo()))
            .collect(Collectors.toSet());
        GraphDto graph = new GraphDto(nodesGraph, edgesGraph);
        MvcResult result = this.mvc.perform(post(GraphRestController.REST_URL)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(TestUtils.mapToJson(graph))).andReturn();
        Assert.assertEquals(HttpStatus.CREATED.value(), result.getResponse().getStatus());
        GraphDto returnedGraph = TestUtils.mapFromJson(result.getResponse().getContentAsString(), GraphDto.class);
        Assert.assertNotNull(returnedGraph);
        Assert.assertEquals(nodesGraph.size(), returnedGraph.getNodes().size());
        Assert.assertEquals(edgesGraph.size(), returnedGraph.getEdges().size());
        Assert.assertNotNull(returnedGraph.getNodes().iterator().next().getId());
        Assert.assertNotNull(returnedGraph.getEdges().iterator().next().getId());
    }

    @Test
    public void deleteGraph() throws Exception {
        this.mvc.perform(delete(GraphRestController.REST_URL).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());
        this.mvc.perform(get(GraphRestController.REST_URL).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(TestUtils.mapToJson(new GraphDto())));
    }

    @Test
    public void getOptions() throws Exception {
        MvcResult result = this.mvc.perform(options(GraphRestController.REST_URL)
            .accept(MediaType.APPLICATION_JSON)).andReturn();
        Assert.assertTrue(result.getResponse().containsHeader("Allow"));
        Assert.assertEquals("GET,POST,DELETE,OPTIONS", result.getResponse().getHeader("Allow"));
    }

    @Test
    public void checkRouteEmptyInputCollectionException() throws Exception {
        List<String> route = new ArrayList<>();
        MvcResult result = this.mvc.perform(post(GraphRestController.REST_URL + "/checkroute")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(TestUtils.mapToJson(route))).andReturn();
        Assert.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), result.getResponse().getStatus());
        ErrorInfo error = TestUtils.mapFromJson(result.getResponse().getContentAsString(), ErrorInfo.class);
        Assert.assertEquals(ErrorType.DATA_ERROR, error.getType());
        Assert.assertEquals(ErrorPlaceType.GRAPH, error.getPlace());
        List<String> errMsgs = Arrays.asList(error.getMessages());
        Assert.assertTrue(errMsgs.contains(properties.getAppMsg().get("MSG_COLLECTION_EMPTY")));
    }

    @Test
    public void checkRouteNotEnoughDataException() throws Exception {
        List<String> route = Collections.singletonList("v1");
        MvcResult result = this.mvc.perform(post(GraphRestController.REST_URL + "/checkroute")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(TestUtils.mapToJson(route))).andReturn();
        Assert.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), result.getResponse().getStatus());
        ErrorInfo error = TestUtils.mapFromJson(result.getResponse().getContentAsString(), ErrorInfo.class);
        Assert.assertEquals(ErrorType.DATA_ERROR, error.getType());
        Assert.assertEquals(ErrorPlaceType.GRAPH, error.getPlace());
        List<String> errMsgs = Arrays.asList(error.getMessages());
        Assert.assertTrue(errMsgs.contains(properties.getAppMsg().get("MSG_COLLECTION_CONTAIN_ONE_ELEMENT")));
    }

    @Test
    public void checkRouteNotFoundException() throws Exception {
        List<String> route = Stream.of("v7", "v2", "v1").collect(Collectors.toList());
        MvcResult result = this.mvc.perform(post(GraphRestController.REST_URL + "/checkroute")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(TestUtils.mapToJson(route))).andReturn();
        Assert.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), result.getResponse().getStatus());
        ErrorInfo error = TestUtils.mapFromJson(result.getResponse().getContentAsString(), ErrorInfo.class);
        Assert.assertEquals(ErrorType.DATA_NOT_FOUND, error.getType());
        Assert.assertEquals(ErrorPlaceType.GRAPH, error.getPlace());
        List<String> errMsgs = Arrays.asList(error.getMessages());
        Assert.assertTrue(errMsgs.contains(String.format(properties.getNodeMsg().get("NODE_MSG_BY_NAME_ERROR"), "v7")));
    }


}
