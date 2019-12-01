package ru.resprojects.linkchecker.web.rest.apidocs;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import ru.resprojects.linkchecker.TestUtils;
import ru.resprojects.linkchecker.services.GraphService;
import ru.resprojects.linkchecker.web.rest.GraphRestController;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(GraphRestController.class)
@AutoConfigureRestDocs(outputDir = "target/generated-snippets")
public class GraphCheckRouteApiDocumentation {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private GraphService graphService;

    @Test
    public void checkRouteInGraph() throws Exception {
        List<String> route = Stream.of("v1", "v2", "v3").collect(Collectors.toList());
        String returnedResult = String.format("Route for nodes %s is found", route.toString());
        given(this.graphService.checkRoute(anySet())).willReturn(returnedResult);
        this.mvc.perform(post(GraphRestController.REST_URL + "/checkroute")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(TestUtils.mapToJson(route)))
            .andExpect(status().isOk())
            .andDo(document("checkroute-graph"))
        ;
    }

}
