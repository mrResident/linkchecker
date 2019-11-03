package ru.resprojects.linkchecker;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.resprojects.linkchecker.dto.GraphDto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestUtils {

    public static final GraphDto.NodeGraph nodeGraph = new GraphDto.NodeGraph(5000, "v1", 0);
    public static final GraphDto.EdgeGraph edgeGraph = new GraphDto.EdgeGraph(5005, "v1", "v2");
    public static final Set<GraphDto.NodeGraph> nodesGraph = Stream.of(
        nodeGraph,
        new GraphDto.NodeGraph(5001, "v2", 0),
        new GraphDto.NodeGraph(5002, "v3", 0),
        new GraphDto.NodeGraph(5003, "v4", 0),
        new GraphDto.NodeGraph(5004, "v5", 0)
    ).collect(Collectors.toSet());
    public static final Set<GraphDto.EdgeGraph> edgesGraph = Stream.of(
        edgeGraph,
        new GraphDto.EdgeGraph(5006, "v1", "v3"),
        new GraphDto.EdgeGraph(5007, "v1", "v5"),
        new GraphDto.EdgeGraph(5008, "v3", "v4")
    ).collect(Collectors.toSet());
    public static final GraphDto graph = new GraphDto(nodesGraph, edgesGraph);

    private TestUtils() {
    }

    public static String loadJsonFromFile(String sourceFileName) throws IOException {
        try(InputStream is = TestUtils.class.getResourceAsStream(sourceFileName)) {
            try(BufferedReader br = new BufferedReader(new InputStreamReader(is,
                StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                br.lines().forEach(s -> sb.append(s).append('\n'));
                return sb.toString();
            }
        }
    }

    public static String mapToJson(Object obj) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(obj);
    }

    public static <T> T mapFromJson(String json, Class<T> clazz) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, clazz);
    }

}
