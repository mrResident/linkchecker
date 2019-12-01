package ru.resprojects.linkchecker.web.rest.apidocs;

import com.google.gson.reflect.TypeToken;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.resprojects.linkchecker.TestUtils;
import ru.resprojects.linkchecker.dto.GraphDto;
import ru.resprojects.linkchecker.util.exeptions.ErrorInfo;
import ru.resprojects.linkchecker.util.exeptions.ErrorPlaceType;
import ru.resprojects.linkchecker.util.exeptions.ErrorType;
import ru.resprojects.linkchecker.web.rest.GraphEdgeRestController;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(profiles = {"test"})
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
    scripts = {"classpath:schema-h2.sql", "classpath:data-h2.sql"},
    config = @SqlConfig(encoding = "UTF-8"))
@AutoConfigureMockMvc
@AutoConfigureRestDocs(outputDir = "target/generated-snippets")
public class GraphEdgeApiDocumentation {

    @Autowired
    private MockMvc mvc;

    @Test
    public void getEdges() throws Exception {
        this.mvc.perform(get(GraphEdgeRestController.EDGE_REST_URL).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(ResponseEdgesDoc("get-edges"));
    }

    @Test
    public void getEdgeById() throws Exception {
        this.mvc.perform(get(GraphEdgeRestController.EDGE_REST_URL + "/byId/5005").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(ResponseEdgeDoc("get-edge-by-id"));
    }

    @Test
    public void getEdgesByNodeName() throws Exception {
        this.mvc.perform(get(GraphEdgeRestController.EDGE_REST_URL + "/byName/v1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(ResponseEdgesDoc("get-edges-by-node-name"));
    }

    @Test
    public void getEdgeByNodeNames() throws Exception {
        this.mvc.perform(get(GraphEdgeRestController.EDGE_REST_URL + "/byName?nodeOne=v1&nodeTwo=v2").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(ResponseEdgeDoc("get-edge-by-nodes-name"));
    }

    @Test
    public void getEdgeByIdNotFoundException() throws Exception {
       this.mvc.perform(get(GraphEdgeRestController.EDGE_REST_URL + "/byId/5050")
            .accept(MediaType.APPLICATION_JSON))
            .andDo(GraphApiDocumentation.ErrorResponseDoc("get-edge-exception-1"));
    }

    @Test
    public void getEdgesByNameNotFoundException() throws Exception {
        this.mvc.perform(get(GraphEdgeRestController.EDGE_REST_URL + "/byName/v100")
            .accept(MediaType.APPLICATION_JSON))
            .andDo(GraphApiDocumentation.ErrorResponseDoc("get-edge-exception-2"));
    }

    @Test
    public void addNewEdge() throws Exception {
        String jsonEdge = "{\"nodeOne\": \"v1\", \"nodeTwo\": \"v4\"}";
        this.mvc.perform(post(GraphEdgeRestController.EDGE_REST_URL + "/create")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(jsonEdge))
            .andExpect(status().isCreated())
            .andDo(document("create-edge",
                requestFields(
                    fieldWithPath("nodeOne")
                        .description("Уникальное имя вершины графа"),
                    fieldWithPath("nodeTwo")
                        .description("Уникальное имя вершины графа")
                )))
            .andDo(ResponseEdgeDoc("create-edge"));
    }

    @Test
    public void addNewEdges() throws Exception {
        String jsonEdge = "[{\"nodeOne\": \"v1\", \"nodeTwo\": \"v4\"},{\"nodeOne\": \"v2\", \"nodeTwo\": \"v4\"}]";
        this.mvc.perform(post(GraphEdgeRestController.EDGE_REST_URL + "/create/byBatch")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(jsonEdge))
            .andExpect(status().isCreated())
            .andDo(document("create-edges",
                requestFields(
                    fieldWithPath("[]")
                        .description("Коллекция рёбер графа"),
                    fieldWithPath("[].nodeOne")
                        .description("Уникальное имя вершины графа"),
                    fieldWithPath("[].nodeTwo")
                        .description("Уникальное имя вершины графа")
                )))
            .andDo(ResponseEdgesDoc("create-edges"));
    }

    @Test
    public void addNewEdgeValidationException() throws Exception {
        this.mvc.perform(post(GraphEdgeRestController.EDGE_REST_URL + "/create")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content("{\"nodeOne\": \"\", \"nodeTwo\": \"\"}"))
            .andDo(GraphApiDocumentation.ErrorResponseDoc("create-edge-exception-1"));
    }

    @Test
    public void addNewEdgeAlreadyPresentException() throws Exception {
        this.mvc.perform(post(GraphEdgeRestController.EDGE_REST_URL + "/create")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content("{\"nodeOne\": \"v1\", \"nodeTwo\": \"v2\"}"))
            .andDo(GraphApiDocumentation.ErrorResponseDoc("create-edge-exception-2"));
    }

    @Test
    public void addNewEdgesEmptyCollectionException() throws Exception {
        this.mvc.perform(post(GraphEdgeRestController.EDGE_REST_URL + "/create/byBatch")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content("[]"))
            .andDo(GraphApiDocumentation.ErrorResponseDoc("create-edge-exception-3"));
    }

    @Test
    public void addNewEdgesCollectionContainNullObjectException() throws Exception {
        String jsonEdge = "[null,{\"nodeOne\": \"v2\", \"nodeTwo\": \"v4\"}]";
        this.mvc.perform(post(GraphEdgeRestController.EDGE_REST_URL + "/create/byBatch")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(jsonEdge))
            .andDo(GraphApiDocumentation.ErrorResponseDoc("create-edge-exception-4"));
    }

    @Test
    public void addNewEdgesCollectionContainAlreadyPresentNodeException() throws Exception {
        String jsonEdge = "[{\"nodeOne\": \"v1\", \"nodeTwo\": \"v2\"},{\"nodeOne\": \"v1\", \"nodeTwo\": \"v4\"},{\"nodeOne\": \"v2\", \"nodeTwo\": \"v4\"}]";
        this.mvc.perform(post(GraphEdgeRestController.EDGE_REST_URL + "/create/byBatch")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(jsonEdge))
            .andDo(GraphApiDocumentation.ErrorResponseDoc("create-edge-exception-5"));
    }

    @Test
    public void deleteAllEdges() throws Exception {
        this.mvc.perform(delete(GraphEdgeRestController.EDGE_REST_URL).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent())
            .andDo(document("delete-all-edges"));
    }

    @Test
    public void deleteEdgeById() throws Exception {
        this.mvc.perform(delete(GraphEdgeRestController.EDGE_REST_URL + "/byId/5005").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent())
            .andDo(document("delete-edge-by-id"));
    }

    @Test
    public void deleteEdgesByNodeName() throws Exception {
        this.mvc.perform(delete(GraphEdgeRestController.EDGE_REST_URL + "/byName/v4").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent())
            .andDo(document("delete-edges-by-node-name"));
    }

    @Test
    public void deleteEdgeByNodeNames() throws Exception {
        this.mvc.perform(delete(GraphEdgeRestController.EDGE_REST_URL + "/byName?nodeOne=v1&nodeTwo=v2").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent())
            .andDo(document("delete-edge-by-nodes-name"));
    }

    @Test
    public void deleteEdgeByIdNotFoundException() throws Exception {
        this.mvc.perform(delete(GraphEdgeRestController.EDGE_REST_URL + "/byId/5050")
            .accept(MediaType.APPLICATION_JSON))
            .andDo(GraphApiDocumentation.ErrorResponseDoc("delete-edge-exception-1"));
    }

    @Test
    public void deleteEdgesByNodeNameNotFoundException() throws Exception {
        this.mvc.perform(delete(GraphEdgeRestController.EDGE_REST_URL + "/byName/v50")
            .accept(MediaType.APPLICATION_JSON))
            .andDo(GraphApiDocumentation.ErrorResponseDoc("delete-edge-exception-2"));
    }

    @Test
    public void deleteEdgeByNodeNamesNotFoundException() throws Exception {
        this.mvc.perform(delete(GraphEdgeRestController.EDGE_REST_URL + "/byName?nodeOne=v50&nodeTwo=v2")
            .accept(MediaType.APPLICATION_JSON))
            .andDo(GraphApiDocumentation.ErrorResponseDoc("delete-edge-exception-3"));
    }

    private static RestDocumentationResultHandler ResponseEdgeDoc(String documentIdentifier) {
        return document(documentIdentifier,
            responseFields(
                fieldWithPath("id")
                    .description("Идентификатор ребра графа"),
                fieldWithPath("nodeOne")
                    .description("Уникальное имя первой вершины графа, которое связывается текущим ребром"),
                fieldWithPath("nodeTwo")
                    .description("Уникальное имя второй вершины графа, которое связывается текущим ребром")
            ));
    }

    private static RestDocumentationResultHandler ResponseEdgesDoc(String documentIdentifier) {
        return document(documentIdentifier,
            responseFields(
                fieldWithPath("[]")
                    .description("Коллекция рёбер"),
                fieldWithPath("[].id")
                    .description("Идентификатор ребра графа"),
                fieldWithPath("[].nodeOne")
                    .description("Уникальное имя первой вершины графа, которое связывается текущим ребром"),
                fieldWithPath("[].nodeTwo")
                    .description("Уникальное имя второй вершины графа, которое связывается текущим ребром")
            ));
    }

}
