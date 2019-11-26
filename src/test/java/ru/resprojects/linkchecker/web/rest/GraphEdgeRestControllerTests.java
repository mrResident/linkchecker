package ru.resprojects.linkchecker.web.rest;

import com.google.gson.reflect.TypeToken;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
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
import static ru.resprojects.linkchecker.dto.GraphDto.EdgeGraph;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LinkcheckerApplication.class)
@ActiveProfiles(profiles = {"test", "debug"})
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
    scripts = {"classpath:schema-h2.sql", "classpath:data-h2.sql"},
    config = @SqlConfig(encoding = "UTF-8"))
@AutoConfigureMockMvc
public class GraphEdgeRestControllerTests {

    private static final Logger LOG = LoggerFactory.getLogger(GraphRestControllerTests.class);

    @Autowired
    private MockMvc mvc;

    @Autowired
    private AppProperties properties;

    @Test
    public void addNewEdge() throws Exception {
        EdgeGraph newEdge = new EdgeGraph("v1", "v4");
        MvcResult result = this.mvc.perform(post(GraphEdgeRestController.EDGE_REST_URL + "/create")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(TestUtils.mapToJson(newEdge))).andReturn();
        Assert.assertEquals(HttpStatus.CREATED.value(), result.getResponse().getStatus());
        EdgeGraph returnedEdge = TestUtils.mapFromJson(result.getResponse().getContentAsString(), EdgeGraph.class);
        Assert.assertNotNull(returnedEdge);
        Assert.assertEquals(newEdge.getNodeOne(), returnedEdge.getNodeOne());
        Assert.assertEquals(newEdge.getNodeTwo(), returnedEdge.getNodeTwo());
        Assert.assertNotNull(returnedEdge.getId());
    }

    @Test
    public void addNewEdgeValidationException() throws Exception {
        MvcResult result = this.mvc.perform(post(GraphEdgeRestController.EDGE_REST_URL + "/create")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(TestUtils.mapToJson(new EdgeGraph()))).andReturn();
        Assert.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), result.getResponse().getStatus());
        ErrorInfo error = TestUtils.mapFromJson(result.getResponse().getContentAsString(), ErrorInfo.class);
        Assert.assertEquals(ErrorType.VALIDATION_ERROR, error.getType());
        Assert.assertEquals(ErrorPlaceType.APP, error.getPlace());
        LOG.info(Arrays.asList(error.getMessages()).toString());
    }

    @Test
    public void addNewEdgeAlreadyPresentException() throws Exception {
        EdgeGraph newEdge = new EdgeGraph("v1", "v2");
        MvcResult result = this.mvc.perform(post(GraphEdgeRestController.EDGE_REST_URL + "/create")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(TestUtils.mapToJson(newEdge))).andReturn();
        Assert.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), result.getResponse().getStatus());
        ErrorInfo error = TestUtils.mapFromJson(result.getResponse().getContentAsString(), ErrorInfo.class);
        Assert.assertEquals(ErrorType.DATA_ERROR, error.getType());
        Assert.assertEquals(ErrorPlaceType.EDGE, error.getPlace());
        List<String> errMsgs = Arrays.asList(error.getMessages());
        Assert.assertTrue(errMsgs.contains(String.format(properties.getEdgeMsg().get("EDGE_MSG_ALREADY_PRESENT_ERROR"),
            newEdge.getNodeOne(), newEdge.getNodeTwo(),
            newEdge.getNodeTwo(), newEdge.getNodeOne())));
        LOG.info(errMsgs.toString());
    }

    @Test
    public void addNewEdges() throws Exception {
        Set<EdgeGraph> newEdges = Stream.of(
            new EdgeGraph("v1", "v4"),
            new EdgeGraph("v2", "v4")
        ).collect(Collectors.toSet());
        MvcResult result = this.mvc.perform(post(GraphEdgeRestController.EDGE_REST_URL + "/create/byBatch")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(TestUtils.mapToJson(newEdges))).andReturn();
        Assert.assertEquals(HttpStatus.CREATED.value(), result.getResponse().getStatus());
        Type listType = new TypeToken<HashSet<EdgeGraph>>() {}.getType();
        Set<EdgeGraph> returnedEdges = TestUtils.mapFromJson(result.getResponse().getContentAsString(), listType);
        Assert.assertEquals(newEdges.size(), returnedEdges.size());
        Assert.assertTrue(returnedEdges.stream().anyMatch(ng -> ng.getNodeOne().equals("v1") && ng.getNodeTwo().equals("v4")));
    }

    @Test
    public void addNewEdgesEmptyCollectionException() throws Exception {
        MvcResult result = this.mvc.perform(post(GraphEdgeRestController.EDGE_REST_URL + "/create/byBatch")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(TestUtils.mapToJson(Collections.emptySet()))).andReturn();
        Assert.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), result.getResponse().getStatus());
        ErrorInfo error = TestUtils.mapFromJson(result.getResponse().getContentAsString(), ErrorInfo.class);
        Assert.assertEquals(ErrorType.DATA_ERROR, error.getType());
        Assert.assertEquals(ErrorPlaceType.EDGE, error.getPlace());
        List<String> errMsgs = Arrays.asList(error.getMessages());
        Assert.assertTrue(errMsgs.contains(properties.getAppMsg().get("MSG_COLLECTION_EMPTY")));
        LOG.info(errMsgs.toString());
    }

    @Test
    public void addNewEdgesCollectionContainNullObjectException() throws Exception {
        Set<EdgeGraph> newEdges = Stream.of(
            null,
            new EdgeGraph("v1", "v4"),
            new EdgeGraph("v2", "v4")
        ).collect(Collectors.toSet());
        MvcResult result = this.mvc.perform(post(GraphEdgeRestController.EDGE_REST_URL + "/create/byBatch")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(TestUtils.mapToJson(newEdges))).andReturn();
        Assert.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), result.getResponse().getStatus());
        ErrorInfo error = TestUtils.mapFromJson(result.getResponse().getContentAsString(), ErrorInfo.class);
        Assert.assertEquals(ErrorType.DATA_ERROR, error.getType());
        Assert.assertEquals(ErrorPlaceType.EDGE, error.getPlace());
        List<String> errMsgs = Arrays.asList(error.getMessages());
        Assert.assertTrue(errMsgs.contains(properties.getAppMsg().get("MSG_COLLECTION_CONTAIN_NULL")));
        LOG.info(errMsgs.toString());
    }

    @Test
    public void addNewEdgesCollectionContainAlreadyPresentNodeException() throws Exception {
        EdgeGraph newEdge = new EdgeGraph("v1", "v2");
        Set<EdgeGraph> newEdges = Stream.of(
            newEdge,
            new EdgeGraph("v1", "v4"),
            new EdgeGraph("v2", "v4")
        ).collect(Collectors.toSet());
        MvcResult result = this.mvc.perform(post(GraphEdgeRestController.EDGE_REST_URL + "/create/byBatch")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(TestUtils.mapToJson(newEdges))).andReturn();
        Assert.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), result.getResponse().getStatus());
        ErrorInfo error = TestUtils.mapFromJson(result.getResponse().getContentAsString(), ErrorInfo.class);
        Assert.assertEquals(ErrorType.DATA_ERROR, error.getType());
        Assert.assertEquals(ErrorPlaceType.EDGE, error.getPlace());
        List<String> errMsgs = Arrays.asList(error.getMessages());
        Assert.assertTrue(errMsgs.contains(String.format(properties.getEdgeMsg().get("EDGE_MSG_ALREADY_PRESENT_ERROR"),
            newEdge.getNodeOne(), newEdge.getNodeTwo(),
            newEdge.getNodeTwo(), newEdge.getNodeOne())));
        LOG.info(errMsgs.toString());
    }

    @Test
    public void getEdges() throws Exception {
        this.mvc.perform(get(GraphEdgeRestController.EDGE_REST_URL).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(TestUtils.mapToJson(TestUtils.edgesGraph)));
    }

    @Test
    public void getEdgeById() throws Exception {
        this.mvc.perform(get(GraphEdgeRestController.EDGE_REST_URL + "/byId/5005").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(TestUtils.mapToJson(TestUtils.edgeGraph)));
    }

    @Test
    public void getEdgesByNodeName() throws Exception {
        Set<EdgeGraph> expected = TestUtils.edgesGraph.stream()
            .filter(eg -> eg.getId() != 5008)
            .collect(Collectors.toSet());
        this.mvc.perform(get(GraphEdgeRestController.EDGE_REST_URL + "/byName/v1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(TestUtils.mapToJson(expected)));
    }

    @Test
    public void getEdgeByNodeNames() throws Exception {
        this.mvc.perform(get(GraphEdgeRestController.EDGE_REST_URL + "/byName?nodeOne=v1&nodeTwo=v2").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(TestUtils.mapToJson(TestUtils.edgeGraph)));
    }

    @Test
    public void getEdgeByIdNotFoundException() throws Exception {
        MvcResult result = this.mvc.perform(get(GraphEdgeRestController.EDGE_REST_URL + "/byId/5050")
            .accept(MediaType.APPLICATION_JSON)).andReturn();
        Assert.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), result.getResponse().getStatus());
        ErrorInfo error = TestUtils.mapFromJson(result.getResponse().getContentAsString(), ErrorInfo.class);
        Assert.assertEquals(ErrorType.DATA_NOT_FOUND, error.getType());
        Assert.assertEquals(ErrorPlaceType.EDGE, error.getPlace());
        List<String> errMsgs = Arrays.asList(error.getMessages());
        Assert.assertTrue(errMsgs.contains(String.format(properties.getAppMsg().get("MSG_BY_ID_ERROR"), ErrorPlaceType.EDGE, 5050)));
    }

    @Test
    public void getEdgesByNameNotFoundException() throws Exception {
        MvcResult result = this.mvc.perform(get(GraphEdgeRestController.EDGE_REST_URL + "/byName/v100")
            .accept(MediaType.APPLICATION_JSON)).andReturn();
        Assert.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), result.getResponse().getStatus());
        ErrorInfo error = TestUtils.mapFromJson(result.getResponse().getContentAsString(), ErrorInfo.class);
        Assert.assertEquals(ErrorType.DATA_NOT_FOUND, error.getType());
        Assert.assertEquals(ErrorPlaceType.EDGE, error.getPlace());
        List<String> errMsgs = Arrays.asList(error.getMessages());
        Assert.assertTrue(errMsgs.contains(String.format(properties.getEdgeMsg().get("EDGE_MSG_GET_BY_NAME_ERROR"), "v100")));
    }

    @Test
    public void deleteAllEdges() throws Exception {
        this.mvc.perform(delete(GraphEdgeRestController.EDGE_REST_URL).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());
        this.mvc.perform(get(GraphEdgeRestController.EDGE_REST_URL).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(TestUtils.mapToJson(Collections.EMPTY_SET)));
    }

    @Test
    public void deleteEdgeById() throws Exception {
        Set<EdgeGraph> expected = TestUtils.edgesGraph.stream()
            .filter(eg -> eg.getId() != 5005)
            .collect(Collectors.toSet());
        this.mvc.perform(delete(GraphEdgeRestController.EDGE_REST_URL + "/byId/5005").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());
        this.mvc.perform(get(GraphEdgeRestController.EDGE_REST_URL).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(TestUtils.mapToJson(expected)));
    }

    @Test
    public void deleteEdgeByIdNotFoundException() throws Exception {
        MvcResult result = this.mvc.perform(delete(GraphEdgeRestController.EDGE_REST_URL + "/byId/5050")
            .accept(MediaType.APPLICATION_JSON)).andReturn();
        Assert.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), result.getResponse().getStatus());
        ErrorInfo error = TestUtils.mapFromJson(result.getResponse().getContentAsString(), ErrorInfo.class);
        Assert.assertEquals(ErrorType.DATA_NOT_FOUND, error.getType());
        Assert.assertEquals(ErrorPlaceType.EDGE, error.getPlace());
        List<String> errMsgs = Arrays.asList(error.getMessages());
        Assert.assertTrue(errMsgs.contains(String.format(properties.getAppMsg().get("MSG_BY_ID_ERROR"), ErrorPlaceType.EDGE, 5050)));
    }

    @Test
    public void deleteEdgesByNodeName() throws Exception {
        Set<EdgeGraph> expected = TestUtils.edgesGraph.stream()
            .filter(eg -> eg.getId() != 5008)
            .collect(Collectors.toSet());
        this.mvc.perform(delete(GraphEdgeRestController.EDGE_REST_URL + "/byName/v4").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());
        this.mvc.perform(get(GraphEdgeRestController.EDGE_REST_URL).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(TestUtils.mapToJson(expected)));
    }

    @Test
    public void deleteEdgesByNodeNameNotFoundException() throws Exception {
        MvcResult result = this.mvc.perform(delete(GraphEdgeRestController.EDGE_REST_URL + "/byName/v50")
            .accept(MediaType.APPLICATION_JSON)).andReturn();
        Assert.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), result.getResponse().getStatus());
        ErrorInfo error = TestUtils.mapFromJson(result.getResponse().getContentAsString(), ErrorInfo.class);
        Assert.assertEquals(ErrorType.DATA_NOT_FOUND, error.getType());
        Assert.assertEquals(ErrorPlaceType.EDGE, error.getPlace());
        List<String> errMsgs = Arrays.asList(error.getMessages());
        Assert.assertTrue(errMsgs.contains(String.format(properties.getEdgeMsg().get("EDGE_MSG_GET_BY_NAME_ERROR"), "v50")));
    }

    @Test
    public void deleteEdgeByNodeNames() throws Exception {
        Set<EdgeGraph> expected = TestUtils.edgesGraph.stream()
            .filter(eg -> eg.getId() != 5005)
            .collect(Collectors.toSet());
        this.mvc.perform(delete(GraphEdgeRestController.EDGE_REST_URL + "/byName?nodeOne=v1&nodeTwo=v2").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());
        this.mvc.perform(get(GraphEdgeRestController.EDGE_REST_URL).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(TestUtils.mapToJson(expected)));
    }

    @Test
    public void deleteEdgeByNodeNamesNotFoundException() throws Exception {
        MvcResult result = this.mvc.perform(delete(GraphEdgeRestController.EDGE_REST_URL + "/byName?nodeOne=v50&nodeTwo=v2")
            .accept(MediaType.APPLICATION_JSON)).andReturn();
        Assert.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), result.getResponse().getStatus());
        ErrorInfo error = TestUtils.mapFromJson(result.getResponse().getContentAsString(), ErrorInfo.class);
        Assert.assertEquals(ErrorType.DATA_NOT_FOUND, error.getType());
        Assert.assertEquals(ErrorPlaceType.EDGE, error.getPlace());
        List<String> errMsgs = Arrays.asList(error.getMessages());
        Assert.assertTrue(errMsgs.contains(String.format(properties.getEdgeMsg().get("EDGE_MSG_GET_ERROR"), "v50", "v2")));
    }

}
