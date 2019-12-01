package ru.resprojects.linkchecker.web.rest.apidocs;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import ru.resprojects.linkchecker.TestUtils;
import ru.resprojects.linkchecker.dto.GraphDto;
import ru.resprojects.linkchecker.web.rest.GraphNodeRestController;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(profiles = {"test"})
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
    scripts = {"classpath:schema-h2.sql", "classpath:data-h2.sql"},
    config = @SqlConfig(encoding = "UTF-8"))
@AutoConfigureMockMvc
@AutoConfigureRestDocs(outputDir = "target/generated-snippets")
public class GraphNodeApiDocumentation {

    @Autowired
    private MockMvc mvc;

    @Test
    public void getNodes() throws Exception {
        this.mvc.perform(get(GraphNodeRestController.NODES_REST_URL).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(ResponseNodesDoc("get-nodes"));
    }

    @Test
    public void getNodeById() throws Exception {
        this.mvc.perform(get(GraphNodeRestController.NODES_REST_URL + "/byId/5000").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(ResponseNodeDoc("get-node-by-id"));
    }

    @Test
    public void getNodeByName() throws Exception {
        this.mvc.perform(get(GraphNodeRestController.NODES_REST_URL + "/byName/v1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(ResponseNodeDoc("get-node-by-name"));
    }

    @Test
    public void getNodeByNameNotFoundException() throws Exception {
        this.mvc.perform(get(GraphNodeRestController.NODES_REST_URL + "/byName/v10")
            .accept(MediaType.APPLICATION_JSON))
            .andDo(GraphApiDocumentation.ErrorResponseDoc("get-node-by-name-exception"));
    }

    @Test
    public void addNewNodeToGraph() throws Exception {
        String jsonNode = "{\"name\": \"v6\"}";
        this.mvc.perform(post(GraphNodeRestController.NODES_REST_URL + "/create")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(jsonNode))
            .andExpect(status().isCreated())
            .andDo(document("create-node",
                requestFields(
                    fieldWithPath("name")
                        .description("Уникальное имя вершины графа")
                )))
            .andDo(ResponseNodeDoc("create-node"));
    }

    @Test
    public void addNewNodesToGraph() throws Exception {
        String jsonNodes = "[{\"name\":\"v6\"}, {\"name\":\"v7\"}, {\"name\":\"v8\"}]";
        this.mvc.perform(post(GraphNodeRestController.NODES_REST_URL + "/create/byBatch")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(jsonNodes))
            .andDo(document("create-nodes",
                requestFields(
                    fieldWithPath("[]")
                        .description("Коллекция вершин графа (ноды)"),
                    fieldWithPath("[].name")
                        .description("Уникальное имя вершины графа")
                )))
            .andDo(ResponseNodesDoc("create-nodes"))
        ;
    }

    @Test
    public void addNewNodeValidationException() throws Exception {
        String jsonNode = "{\"name\":\"\"}";
            this.mvc.perform(post(GraphNodeRestController.NODES_REST_URL + "/create")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(jsonNode))
            .andDo(GraphApiDocumentation.ErrorResponseDoc("create-node-exception-1"));
    }

    @Test
    public void addNewNodeAlreadyPresentException() throws Exception {
        String jsonNode = "{\"name\": \"v1\"}";
        this.mvc.perform(post(GraphNodeRestController.NODES_REST_URL + "/create")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(jsonNode))
            .andDo(GraphApiDocumentation.ErrorResponseDoc("create-node-exception-2"));
    }

    @Test
    public void addNewNodesEmptyCollectionException() throws Exception {
        this.mvc.perform(post(GraphNodeRestController.NODES_REST_URL + "/create/byBatch")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content("[]"))
            .andDo(GraphApiDocumentation.ErrorResponseDoc("create-node-exception-3"));
    }

    @Test
    public void addNewNodesCollectionContainNullObjectException() throws Exception {
        String jsonNodes = "[null, {\"name\": \"v7\"}, {\"name\": \"v8\"}]";
        this.mvc.perform(post(GraphNodeRestController.NODES_REST_URL + "/create/byBatch")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(jsonNodes))
            .andDo(GraphApiDocumentation.ErrorResponseDoc("create-node-exception-4"));
    }

    @Test
    public void addNewNodesCollectionContainAlreadyPresentNodeException() throws Exception {
        String jsonNodes = "[{\"name\":\"v1\"}, {\"name\":\"v7\"}, {\"name\":\"v8\"}]";
        this.mvc.perform(post(GraphNodeRestController.NODES_REST_URL + "/create/byBatch")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(jsonNodes))
            .andDo(GraphApiDocumentation.ErrorResponseDoc("create-node-exception-5"));
    }

    @Test
    public void deleteAllNodes() throws Exception {
        this.mvc.perform(delete(GraphNodeRestController.NODES_REST_URL).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent())
            .andDo(document("delete-all-nodes"));
    }

    @Test
    public void deleteNodeById() throws Exception {
        this.mvc.perform(delete(GraphNodeRestController.NODES_REST_URL + "/byId/5000").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent())
            .andDo(document("delete-node-by-id"));
    }

    @Test
    public void deleteNodeByName() throws Exception {
        this.mvc.perform(delete(GraphNodeRestController.NODES_REST_URL + "/byName/v1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent())
            .andDo(document("delete-node-by-name"));
    }

    @Test
    public void deleteNodeByObject() throws Exception {
        this.mvc.perform(delete(GraphNodeRestController.NODES_REST_URL + "/byObj").contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(TestUtils.mapToJson(TestUtils.nodeGraph)))
            .andExpect(status().isNoContent())
            .andDo(document("delete-node-by-obj"));
    }

    @Test
    public void deleteNodeByIdNotFoundException() throws Exception {
        this.mvc.perform(delete(GraphNodeRestController.NODES_REST_URL + "/byId/5050")
            .accept(MediaType.APPLICATION_JSON))
            .andDo(GraphApiDocumentation.ErrorResponseDoc("delete-node-exception-1"));
    }

    @Test
    public void deleteNodeByNameNotFoundException() throws Exception {
        this.mvc.perform(delete(GraphNodeRestController.NODES_REST_URL + "/byName/v10")
            .accept(MediaType.APPLICATION_JSON))
            .andDo(GraphApiDocumentation.ErrorResponseDoc("delete-node-exception-2"));
    }

    @Test
    public void deleteNodeByObjectWithNullIdNotFoundException() throws Exception {
        GraphDto.NodeGraph newNode = new GraphDto.NodeGraph(null, "v10", 0);
        this.mvc.perform(delete(GraphNodeRestController.NODES_REST_URL + "/byObj")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(TestUtils.mapToJson(newNode)))
            .andDo(GraphApiDocumentation.ErrorResponseDoc("delete-node-exception-3"));
    }

    @Test
    public void deleteNodeByObjectNotFoundException() throws Exception {
        GraphDto.NodeGraph newNode = new GraphDto.NodeGraph(5020, "v10", 0);
        this.mvc.perform(delete(GraphNodeRestController.NODES_REST_URL + "/byObj")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(TestUtils.mapToJson(newNode)))
            .andDo(GraphApiDocumentation.ErrorResponseDoc("delete-node-exception-4"));
    }

    @Test
    public void deleteNodeByObjectWithIncorrectIdException() throws Exception {
        GraphDto.NodeGraph newNode = new GraphDto.NodeGraph(5000, "v10", 0);
        this.mvc.perform(delete(GraphNodeRestController.NODES_REST_URL + "/byObj")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(TestUtils.mapToJson(newNode)))
            .andDo(GraphApiDocumentation.ErrorResponseDoc("delete-node-exception-5"));
    }

    private static RestDocumentationResultHandler ResponseNodeDoc(String documentIdentifier) {
        return document(documentIdentifier,
            responseFields(
                fieldWithPath("id")
                    .description("Идентификатор вершины графа"),
                fieldWithPath("name")
                    .description("Уникальное имя вершины графа"),
                fieldWithPath("counter")
                    .description("Колличество проходов через вершину графа")
            ));
    }

    private static RestDocumentationResultHandler ResponseNodesDoc(String documentIdentifier) {
        return document(documentIdentifier,
            responseFields(
                fieldWithPath("[]")
                    .description("Коллекция вершин графа (ноды)"),
                fieldWithPath("[].id")
                    .description("Идентификатор вершины графа"),
                fieldWithPath("[].name")
                    .description("Уникальное имя вершины графа"),
                fieldWithPath("[].counter")
                    .description("Колличество проходов через вершину графа")
            ));
    }

}
