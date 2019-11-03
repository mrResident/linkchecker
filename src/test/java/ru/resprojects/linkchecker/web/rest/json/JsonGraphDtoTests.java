package ru.resprojects.linkchecker.web.rest.json;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.junit4.SpringRunner;
import ru.resprojects.linkchecker.dto.GraphDto;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.resprojects.linkchecker.TestUtils.*;

@RunWith(SpringRunner.class)
@JsonTest
public class JsonGraphDtoTests {

    @Autowired
    private JacksonTester<GraphDto> jsonGraph;

    @Autowired
    private JacksonTester<GraphDto.NodeGraph> jsonNodeGraph;

    @Autowired
    private JacksonTester<GraphDto.EdgeGraph> jsonEdgeGraph;

    @Autowired
    private JacksonTester<Set<GraphDto.NodeGraph>> jsonNodesGraph;

    @Autowired
    private JacksonTester<Set<GraphDto.EdgeGraph>> jsonEdgesGraph;


    @Test
    public void serializeJsonGraphDto() throws Exception {
        assertThat(this.jsonGraph.write(graph)).isEqualTo("graph.json");
        assertThat(this.jsonGraph.write(graph)).isEqualToJson("graph.json");
        assertThat(this.jsonGraph.write(graph)).hasJsonPathArrayValue("@.nodes");
        assertThat(this.jsonGraph.write(graph)).hasJsonPathArrayValue("@.edges");
        assertThat(this.jsonGraph.write(graph))
            .extractingJsonPathArrayValue("@.nodes")
            .hasSameSizeAs(graph.getNodes());
        assertThat(this.jsonGraph.write(graph))
            .extractingJsonPathArrayValue("@.edges")
            .hasSameSizeAs(graph.getEdges());
    }

    @Test
    public void serializeJsonNodes() throws Exception {
        assertThat(this.jsonNodesGraph.write(nodesGraph)).isEqualTo("nodes.json");
        assertThat(this.jsonNodesGraph.write(nodesGraph)).isEqualToJson("nodes.json");
    }

    @Test
    public void serializeJsonEdges() throws Exception {
        assertThat(this.jsonEdgesGraph.write(edgesGraph)).isEqualTo("edges.json");
        assertThat(this.jsonEdgesGraph.write(edgesGraph)).isEqualToJson("edges.json");
    }

    @Test
    public void serializeJsonNode() throws Exception {
        assertThat(this.jsonNodeGraph.write(nodeGraph)).isEqualTo("node.json");
        assertThat(this.jsonNodeGraph.write(nodeGraph)).isEqualToJson("node.json");
        assertThat(this.jsonNodeGraph.write(nodeGraph)).extractingJsonPathStringValue("@.name")
            .isEqualTo(nodeGraph.getName());
        assertThat(this.jsonNodeGraph.write(nodeGraph))
            .extractingJsonPathNumberValue("@.id")
            .isEqualTo(nodeGraph.getId());
    }

    @Test
    public void serializeJsonEdge() throws Exception {
        assertThat(this.jsonEdgeGraph.write(edgeGraph)).isEqualTo("edge.json");
        assertThat(this.jsonEdgeGraph.write(edgeGraph)).isEqualToJson("edge.json");
        assertThat(this.jsonEdgeGraph.write(edgeGraph)).extractingJsonPathStringValue("@.nodeOne")
            .isEqualTo(edgeGraph.getNodeOne());
        assertThat(this.jsonEdgeGraph.write(edgeGraph))
            .extractingJsonPathStringValue("@.nodeTwo")
            .isEqualTo(edgeGraph.getNodeTwo());
        assertThat(this.jsonEdgeGraph.write(edgeGraph))
            .extractingJsonPathNumberValue("@.id")
            .isEqualTo(edgeGraph.getId());
    }

    @Test
    public void deserializeJsonNode() throws Exception {
        String content = "{\"id\": 5000, \"name\": \"v1\", \"counter\": 0}";
        assertThat(this.jsonNodeGraph.parse(content)).isEqualTo(nodeGraph);
    }

    @Test
    public void deserializeJsonEdge() throws Exception {
        String content = "{\"id\": 5005, \"nodeOne\": \"v1\", \"nodeTwo\": \"v2\"}";
        assertThat(this.jsonEdgeGraph.parse(content)).isEqualTo(edgeGraph);
    }

}
