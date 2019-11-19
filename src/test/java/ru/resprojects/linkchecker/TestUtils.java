package ru.resprojects.linkchecker;

import com.google.gson.Gson;
import ru.resprojects.linkchecker.dto.GraphDto;

import java.lang.reflect.Type;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Helper class for unit testing.
 */
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

    public static String mapToJson(Object obj) {
        return new Gson().toJson(obj);
    }

    public static <T> T mapFromJson(String json, Type type) {
        return new Gson().fromJson(json, type);
    }

    public static <T> T mapFromJson(String json, Class<T> clazz) {
        return new Gson().fromJson(json, clazz);
    }

}
