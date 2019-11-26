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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.resprojects.linkchecker.TestUtils;
import ru.resprojects.linkchecker.dto.GraphDto;
import ru.resprojects.linkchecker.web.rest.GraphRestController;

import java.util.ArrayList;
import java.util.Collections;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(profiles = {"test"})
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
    scripts = {"classpath:schema-h2.sql", "classpath:data-h2.sql"},
    config = @SqlConfig(encoding = "UTF-8"))
@AutoConfigureMockMvc
@AutoConfigureRestDocs(outputDir = "target/generated-snippets")
public class GraphApiDocumentation {

    @Autowired
    private MockMvc mvc;

    @Test
    public void getGraph() throws Exception {
        this.mvc.perform(MockMvcRequestBuilders.get(GraphRestController.REST_URL).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(getGraphResponseDoc("get-graph"));
    }

    @Test
    public void exportGraphToGraphVizFormat() throws Exception {
        this.mvc.perform(get(GraphRestController.REST_URL + "/export").accept(MediaType.TEXT_HTML_VALUE))
            .andExpect(status().isOk())
            .andDo(document("export-graph"));
    }

    @Test
    public void createGraph() throws Exception {
        String jsonGraph = "{\n" +
            "    \"nodes\":[\n" +
            "      {\"name\":\"v1\"},\n" +
            "      {\"name\":\"v2\"},\n" +
            "      {\"name\":\"v3\"},\n" +
            "      {\"name\":\"v4\"},\n" +
            "      {\"name\":\"v5\"}\n" +
            "    ],\n" +
            "    \"edges\":[\n" +
            "      {\"nodeOne\":\"v1\",\"nodeTwo\":\"v2\"},\n" +
            "      {\"nodeOne\":\"v2\",\"nodeTwo\":\"v3\"},\n" +
            "      {\"nodeOne\":\"v3\",\"nodeTwo\":\"v4\"},\n" +
            "      {\"nodeOne\":\"v3\",\"nodeTwo\":\"v5\"},\n" +
            "      {\"nodeOne\":\"v5\",\"nodeTwo\":\"v4\"},\n" +
            "      {\"nodeOne\":\"v5\",\"nodeTwo\":\"v2\"}\n" +
            "    ]\n" +
            "}";
        this.mvc.perform(post(GraphRestController.REST_URL + "/create")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(jsonGraph))
            .andExpect(status().isCreated())
            .andDo(document("create-graph",
                requestFields(
                    fieldWithPath("nodes")
                        .description("Collection of graph nodes"),
                    fieldWithPath("nodes[].name")
                        .description("The name of the node."),
                    fieldWithPath("edges")
                        .description("Collection of graph edges."),
                    fieldWithPath("edges[].nodeOne")
                        .description("Unique name of first graph node."),
                    fieldWithPath("edges[].nodeTwo")
                        .description("Unique name of second graph node.")
                )))
            .andDo(getGraphResponseDoc("create-graph"));
    }

    @Test
    public void createGraphEmptyNodeCollectionException() throws Exception {
        Set<GraphDto.NodeGraph> nodesGraph = Collections.emptySet();
        Set<GraphDto.EdgeGraph> edgesGraph = TestUtils.edgesGraph.stream()
            .map(eg -> new GraphDto.EdgeGraph(eg.getNodeOne(), eg.getNodeTwo()))
            .collect(Collectors.toSet());
        GraphDto graph = new GraphDto(nodesGraph, edgesGraph);
        this.mvc.perform(post(GraphRestController.REST_URL + "/create")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(TestUtils.mapToJson(graph)))
            .andDo(document( "create-graph-exception"));
    }

    @Test
    public void checkRouteEmptyInputCollectionException() throws Exception {
        List<String> route = new ArrayList<>();
        this.mvc.perform(post(GraphRestController.REST_URL + "/checkroute")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(TestUtils.mapToJson(route)))
            .andDo(document( "checkroute-graph-exception-1"));
    }

    @Test
    public void checkRouteNotEnoughDataException() throws Exception {
        List<String> route = Collections.singletonList("v1");
        this.mvc.perform(post(GraphRestController.REST_URL + "/checkroute")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(TestUtils.mapToJson(route)))
            .andDo(document( "checkroute-graph-exception-2"));
    }

    @Test
    public void checkRouteNotFoundException() throws Exception {
        List<String> route = Stream.of("v7", "v2", "v1").collect(Collectors.toList());
        this.mvc.perform(post(GraphRestController.REST_URL + "/checkroute")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(TestUtils.mapToJson(route)))
            .andDo(document( "checkroute-graph-exception-3"));
    }

    @Test
    public void deleteGraph() throws Exception {
        this.mvc.perform(delete(GraphRestController.REST_URL).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent())
            .andDo(document("delete-graph"));
    }

    private RestDocumentationResultHandler getGraphResponseDoc(String documentIdentifier) {
        return document(documentIdentifier,
            responseFields(
                fieldWithPath("nodes")
                    .description("Collection of graph nodes"),
                fieldWithPath("nodes[].id")
                    .description("ID of the node."),
                fieldWithPath("nodes[].name")
                    .description("The name of the node."),
                fieldWithPath("nodes[].counter")
                    .description("The number of passes through the graph node."),
                fieldWithPath("edges")
                    .description("Collection of graph edges."),
                fieldWithPath("edges[].id")
                    .description("ID of the edge."),
                fieldWithPath("edges[].nodeOne")
                    .description("Unique name of first graph node."),
                fieldWithPath("edges[].nodeTwo")
                    .description("Unique name of second graph node.")
            ));
    }

}
