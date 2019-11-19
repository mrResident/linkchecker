package ru.resprojects.linkchecker.web.rest;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.resprojects.linkchecker.TestUtils;
import ru.resprojects.linkchecker.services.GraphService;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@RunWith(SpringRunner.class)
@WebMvcTest(GraphRestController.class)
public class GraphRestControllerMockTests {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private GraphService graphService;

    @Test
    public void checkRouteInGraph() throws Exception {
        List<String> route = Stream.of("v1", "v2", "v3").collect(Collectors.toList());
        String returnedResult = String.format("Route for nodes %s is found", route.toString());
        given(this.graphService.checkRoute(anySet())).willReturn(returnedResult);
        MvcResult result = this.mvc.perform(post(GraphRestController.REST_URL + "/checkroute")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(TestUtils.mapToJson(route))).andReturn();
        Assert.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        Assert.assertEquals(result.getResponse().getContentAsString(), returnedResult);
    }

}
