package ru.resprojects.linkchecker.web.rest;

import com.google.gson.reflect.TypeToken;
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
import ru.resprojects.linkchecker.util.exeptions.ErrorInfo;
import ru.resprojects.linkchecker.util.exeptions.ErrorPlaceType;
import ru.resprojects.linkchecker.util.exeptions.ErrorType;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.resprojects.linkchecker.dto.GraphDto.NodeGraph;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LinkcheckerApplication.class)
@ActiveProfiles(profiles = {"test", "debug"})
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
    scripts = {"classpath:schema-h2.sql", "classpath:data-h2.sql"},
    config = @SqlConfig(encoding = "UTF-8"))
@WebAppConfiguration
public class GraphNodeRestControllerTests {

    private static final Logger LOG = LoggerFactory.getLogger(GraphNodeRestControllerTests.class);

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
    public void getNodes() throws Exception {
        this.mvc.perform(get(GraphNodeRestController.NODES_REST_URL).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(TestUtils.mapToJson(TestUtils.nodesGraph)));
    }

    @Test
    public void getNodeById() throws Exception {
        this.mvc.perform(get(GraphNodeRestController.NODES_REST_URL + "/byId/5000").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(TestUtils.mapToJson(TestUtils.nodeGraph)));
    }

    @Test
    public void getNodeByName() throws Exception {
        this.mvc.perform(get(GraphNodeRestController.NODES_REST_URL + "/byName/v1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(TestUtils.mapToJson(TestUtils.nodeGraph)));
    }

    @Test
    public void getNodeByNameNotFoundException() throws Exception {
        MvcResult result = this.mvc.perform(get(GraphNodeRestController.NODES_REST_URL + "/byName/v10")
            .accept(MediaType.APPLICATION_JSON)).andReturn();
        Assert.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), result.getResponse().getStatus());
        ErrorInfo error = TestUtils.mapFromJson(result.getResponse().getContentAsString(), ErrorInfo.class);
        Assert.assertEquals(ErrorType.DATA_NOT_FOUND, error.getType());
        Assert.assertEquals(ErrorPlaceType.NODE, error.getPlace());
        List<String> errMsgs = Arrays.asList(error.getMessages());
        Assert.assertTrue(errMsgs.contains(String.format(properties.getNodeMsg().get("NODE_MSG_BY_NAME_ERROR"), "v10")));
    }

    @Test
    public void getNodeByIdNotFoundException() throws Exception {
        MvcResult result = this.mvc.perform(get(GraphNodeRestController.NODES_REST_URL + "/byId/5050")
            .accept(MediaType.APPLICATION_JSON)).andReturn();
        Assert.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), result.getResponse().getStatus());
        ErrorInfo error = TestUtils.mapFromJson(result.getResponse().getContentAsString(), ErrorInfo.class);
        Assert.assertEquals(ErrorType.DATA_NOT_FOUND, error.getType());
        Assert.assertEquals(ErrorPlaceType.NODE, error.getPlace());
        List<String> errMsgs = Arrays.asList(error.getMessages());
        Assert.assertTrue(errMsgs.contains(String.format(properties.getAppMsg().get("MSG_BY_ID_ERROR"), ErrorPlaceType.NODE, 5050)));
    }

    @Test
    public void addNewNodeToGraph() throws Exception {
        NodeGraph newNode = new NodeGraph("v6");
        MvcResult result = this.mvc.perform(post(GraphNodeRestController.NODES_REST_URL + "/create")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(TestUtils.mapToJson(newNode))).andReturn();
        Assert.assertEquals(HttpStatus.CREATED.value(), result.getResponse().getStatus());
        NodeGraph returnedNode = TestUtils.mapFromJson(result.getResponse().getContentAsString(), NodeGraph.class);
        Assert.assertNotNull(returnedNode);
        Assert.assertEquals(newNode.getName(), returnedNode.getName());
        Assert.assertNotNull(returnedNode.getId());
    }

    @Test
    public void addNewNodesToGraph() throws Exception {
        Set<NodeGraph> newNodes = Stream.of(
            new NodeGraph("v6"),
            new NodeGraph("v7"),
            new NodeGraph("v8")
        ).collect(Collectors.toSet());
        MvcResult result = this.mvc.perform(post(GraphNodeRestController.NODES_REST_URL + "/create/byBatch")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(TestUtils.mapToJson(newNodes))).andReturn();
        Assert.assertEquals(HttpStatus.CREATED.value(), result.getResponse().getStatus());
        Type listType = new TypeToken<HashSet<NodeGraph>>() {}.getType();
        Set<NodeGraph> returnedNodes = TestUtils.mapFromJson(result.getResponse().getContentAsString(), listType);
        Assert.assertEquals(newNodes.size(), returnedNodes.size());
        Assert.assertTrue(returnedNodes.stream().anyMatch(ng -> ng.getName().equals("v6")));
    }

    @Test
    public void addNewNodeValidationException() throws Exception {
        MvcResult result = this.mvc.perform(post(GraphNodeRestController.NODES_REST_URL + "/create")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(TestUtils.mapToJson(new NodeGraph()))).andReturn();
        Assert.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), result.getResponse().getStatus());
        ErrorInfo error = TestUtils.mapFromJson(result.getResponse().getContentAsString(), ErrorInfo.class);
        Assert.assertEquals(ErrorType.VALIDATION_ERROR, error.getType());
        Assert.assertEquals(ErrorPlaceType.APP, error.getPlace());
        LOG.info(Arrays.asList(error.getMessages()).toString());
    }

    @Test
    public void addNewNodeAlreadyPresentException() throws Exception {
        NodeGraph newNode = new NodeGraph("v1");
        MvcResult result = this.mvc.perform(post(GraphNodeRestController.NODES_REST_URL + "/create")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(TestUtils.mapToJson(newNode))).andReturn();
        Assert.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), result.getResponse().getStatus());
        ErrorInfo error = TestUtils.mapFromJson(result.getResponse().getContentAsString(), ErrorInfo.class);
        Assert.assertEquals(ErrorType.DATA_ERROR, error.getType());
        Assert.assertEquals(ErrorPlaceType.NODE, error.getPlace());
        List<String> errMsgs = Arrays.asList(error.getMessages());
        Assert.assertTrue(errMsgs.contains(String.format(properties.getNodeMsg().get("NODE_MSG_ALREADY_PRESENT_ERROR"), newNode.getName())));
        LOG.info(errMsgs.toString());
    }

    @Test
    public void addNewNodesEmptyCollectionException() throws Exception {
        Set<NodeGraph> newNodes = Collections.emptySet();
        MvcResult result = this.mvc.perform(post(GraphNodeRestController.NODES_REST_URL + "/create/byBatch")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(TestUtils.mapToJson(newNodes))).andReturn();
        Assert.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), result.getResponse().getStatus());
        ErrorInfo error = TestUtils.mapFromJson(result.getResponse().getContentAsString(), ErrorInfo.class);
        Assert.assertEquals(ErrorType.DATA_ERROR, error.getType());
        Assert.assertEquals(ErrorPlaceType.NODE, error.getPlace());
        List<String> errMsgs = Arrays.asList(error.getMessages());
        Assert.assertTrue(errMsgs.contains(properties.getAppMsg().get("MSG_COLLECTION_EMPTY")));
        LOG.info(errMsgs.toString());
    }

    @Test
    public void addNewNodesCollectionContainNullObjectException() throws Exception {
        Set<NodeGraph> newNodes = Stream.of(
            null,
            new NodeGraph("v7"),
            new NodeGraph("v8")
        ).collect(Collectors.toSet());
        MvcResult result = this.mvc.perform(post(GraphNodeRestController.NODES_REST_URL + "/create/byBatch")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(TestUtils.mapToJson(newNodes))).andReturn();
        Assert.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), result.getResponse().getStatus());
        ErrorInfo error = TestUtils.mapFromJson(result.getResponse().getContentAsString(), ErrorInfo.class);
        Assert.assertEquals(ErrorType.DATA_ERROR, error.getType());
        Assert.assertEquals(ErrorPlaceType.NODE, error.getPlace());
        List<String> errMsgs = Arrays.asList(error.getMessages());
        Assert.assertTrue(errMsgs.contains(properties.getAppMsg().get("MSG_COLLECTION_CONTAIN_NULL")));
        LOG.info(errMsgs.toString());
    }

    @Test
    public void addNewNodesCollectionContainAlreadyPresentNodeException() throws Exception {
        Set<NodeGraph> newNodes = Stream.of(
            new NodeGraph("v1"),
            new NodeGraph("v7"),
            new NodeGraph("v8")
        ).collect(Collectors.toSet());
        MvcResult result = this.mvc.perform(post(GraphNodeRestController.NODES_REST_URL + "/create/byBatch")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(TestUtils.mapToJson(newNodes))).andReturn();
        Assert.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), result.getResponse().getStatus());
        ErrorInfo error = TestUtils.mapFromJson(result.getResponse().getContentAsString(), ErrorInfo.class);
        Assert.assertEquals(ErrorType.DATA_ERROR, error.getType());
        Assert.assertEquals(ErrorPlaceType.NODE, error.getPlace());
        List<String> errMsgs = Arrays.asList(error.getMessages());
        Assert.assertTrue(errMsgs.contains(String.format(
            properties.getNodeMsg().get("NODE_MSG_ALREADY_PRESENT_ERROR"),"v1")));
        LOG.info(errMsgs.toString());
    }

    @Test
    public void deleteAllNodes() throws Exception {
        this.mvc.perform(delete(GraphNodeRestController.NODES_REST_URL).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());
        this.mvc.perform(get(GraphNodeRestController.NODES_REST_URL).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(TestUtils.mapToJson(Collections.EMPTY_SET)));
    }

    @Test
    public void deleteNodeById() throws Exception {
        Set<NodeGraph> nodes = TestUtils.nodesGraph.stream().filter(ng -> ng.getId() != 5000).collect(Collectors.toSet());
        this.mvc.perform(delete(GraphNodeRestController.NODES_REST_URL + "/byId/5000").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());
        this.mvc.perform(get(GraphNodeRestController.NODES_REST_URL).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(TestUtils.mapToJson(nodes)));
    }

    @Test
    public void deleteNodeByName() throws Exception {
        Set<NodeGraph> nodes = TestUtils.nodesGraph.stream().filter(ng -> !ng.getName().equals("v1")).collect(Collectors.toSet());
        this.mvc.perform(delete(GraphNodeRestController.NODES_REST_URL + "/byName/v1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());
        this.mvc.perform(get(GraphNodeRestController.NODES_REST_URL).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(TestUtils.mapToJson(nodes)));
    }

    @Test
    public void deleteNodeByObject() throws Exception {
        Set<NodeGraph> nodes = TestUtils.nodesGraph.stream().filter(ng -> !ng.getName().equals("v1")).collect(Collectors.toSet());
        this.mvc.perform(delete(GraphNodeRestController.NODES_REST_URL + "/byObj").contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(TestUtils.mapToJson(TestUtils.nodeGraph)))
            .andExpect(status().isNoContent());
        this.mvc.perform(get(GraphNodeRestController.NODES_REST_URL).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(TestUtils.mapToJson(nodes)));
    }

    @Test
    public void deleteNodeByIdNotFoundException() throws Exception {
        MvcResult result = this.mvc.perform(delete(GraphNodeRestController.NODES_REST_URL + "/byId/5050")
            .accept(MediaType.APPLICATION_JSON)).andReturn();
        Assert.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), result.getResponse().getStatus());
        ErrorInfo error = TestUtils.mapFromJson(result.getResponse().getContentAsString(), ErrorInfo.class);
        Assert.assertEquals(ErrorType.DATA_NOT_FOUND, error.getType());
        Assert.assertEquals(ErrorPlaceType.NODE, error.getPlace());
        List<String> errMsgs = Arrays.asList(error.getMessages());
        Assert.assertTrue(errMsgs.contains(String.format(
            properties.getAppMsg().get("MSG_BY_ID_ERROR"), ErrorPlaceType.NODE, 5050)));
        LOG.info(errMsgs.toString());
    }

    @Test
    public void deleteNodeByNameNotFoundException() throws Exception {
        MvcResult result = this.mvc.perform(delete(GraphNodeRestController.NODES_REST_URL + "/byName/v10")
            .accept(MediaType.APPLICATION_JSON)).andReturn();
        Assert.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), result.getResponse().getStatus());
        ErrorInfo error = TestUtils.mapFromJson(result.getResponse().getContentAsString(), ErrorInfo.class);
        Assert.assertEquals(ErrorType.DATA_NOT_FOUND, error.getType());
        Assert.assertEquals(ErrorPlaceType.NODE, error.getPlace());
        List<String> errMsgs = Arrays.asList(error.getMessages());
        Assert.assertTrue(errMsgs.contains(String.format(
            properties.getNodeMsg().get("NODE_MSG_BY_NAME_ERROR"), "v10")));
        LOG.info(errMsgs.toString());
    }

    @Test
    public void deleteNodeByObjectWithNullIdNotFoundException() throws Exception {
        NodeGraph newNode = new NodeGraph(null, "v10", 0);
        MvcResult result = this.mvc.perform(delete(GraphNodeRestController.NODES_REST_URL + "/byObj")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(TestUtils.mapToJson(newNode))).andReturn();
        Assert.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), result.getResponse().getStatus());
        ErrorInfo error = TestUtils.mapFromJson(result.getResponse().getContentAsString(), ErrorInfo.class);
        Assert.assertEquals(ErrorType.DATA_NOT_FOUND, error.getType());
        Assert.assertEquals(ErrorPlaceType.NODE, error.getPlace());
        List<String> errMsgs = Arrays.asList(error.getMessages());
        Assert.assertTrue(errMsgs.contains(String.format(
            properties.getNodeMsg().get("NODE_MSG_BY_OBJECT_ERROR"), newNode.toString())));
        LOG.info(errMsgs.toString());
    }

    @Test
    public void deleteNodeByObjectNotFoundException() throws Exception {
        NodeGraph newNode = new NodeGraph(5020, "v10", 0);
        MvcResult result = this.mvc.perform(delete(GraphNodeRestController.NODES_REST_URL + "/byObj")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(TestUtils.mapToJson(newNode))).andReturn();
        Assert.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), result.getResponse().getStatus());
        ErrorInfo error = TestUtils.mapFromJson(result.getResponse().getContentAsString(), ErrorInfo.class);
        Assert.assertEquals(ErrorType.DATA_NOT_FOUND, error.getType());
        Assert.assertEquals(ErrorPlaceType.NODE, error.getPlace());
        List<String> errMsgs = Arrays.asList(error.getMessages());
        Assert.assertTrue(errMsgs.contains(String.format(
            properties.getNodeMsg().get("NODE_MSG_BY_OBJECT_ERROR"), newNode.toString())));
        LOG.info(errMsgs.toString());
    }

    @Test
    public void deleteNodeByObjectWithIncorrectIdException() throws Exception {
        NodeGraph newNode = new NodeGraph(5000, "v10", 0);
        MvcResult result = this.mvc.perform(delete(GraphNodeRestController.NODES_REST_URL + "/byObj")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(TestUtils.mapToJson(newNode))).andReturn();
        Assert.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), result.getResponse().getStatus());
        ErrorInfo error = TestUtils.mapFromJson(result.getResponse().getContentAsString(), ErrorInfo.class);
        Assert.assertEquals(ErrorType.DATA_NOT_FOUND, error.getType());
        Assert.assertEquals(ErrorPlaceType.NODE, error.getPlace());
        List<String> errMsgs = Arrays.asList(error.getMessages());
        Assert.assertTrue(errMsgs.contains(String.format(
            properties.getNodeMsg().get("NODE_MSG_BY_OBJECT_ERROR"), newNode.toString())));
        LOG.info(errMsgs.toString());
    }


}
