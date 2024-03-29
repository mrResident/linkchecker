package ru.resprojects.linkchecker.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.resprojects.linkchecker.AppProperties;
import ru.resprojects.linkchecker.model.Edge;
import ru.resprojects.linkchecker.model.Node;
import ru.resprojects.linkchecker.repositories.EdgeRepository;
import ru.resprojects.linkchecker.repositories.NodeRepository;
import ru.resprojects.linkchecker.util.GraphUtil;
import ru.resprojects.linkchecker.util.exeptions.ApplicationException;
import ru.resprojects.linkchecker.util.exeptions.ErrorPlaceType;
import ru.resprojects.linkchecker.util.exeptions.ErrorType;
import ru.resprojects.linkchecker.util.exeptions.NotFoundException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.resprojects.linkchecker.dto.GraphDto.EdgeGraph;
import static ru.resprojects.linkchecker.util.ValidationUtil.checkNotFound;

@Service
public class GraphEdgeServiceImpl implements GraphEdgeService {

    private final EdgeRepository edgeRepository;
    private final NodeRepository nodeRepository;
    private final AppProperties properties;

    @Autowired
    public GraphEdgeServiceImpl(final EdgeRepository edgeRepository,
        final NodeRepository nodeRepository, final AppProperties properties) {
        this.edgeRepository = edgeRepository;
        this.nodeRepository = nodeRepository;
        this.properties = properties;
    }

    private boolean isPresent(final Node nodeOne, final Node nodeTwo) {
        try {
            get(nodeOne.getName(), nodeTwo.getName());
            return true;
        } catch (NotFoundException e) {
            return false;
        }
    }

    @Override
    public EdgeGraph create(final EdgeGraph edgeGraph) throws NotFoundException {
        if (Objects.isNull(edgeGraph)) {
            throw new ApplicationException(
                ErrorType.DATA_ERROR,
                ErrorPlaceType.EDGE,
                HttpStatus.UNPROCESSABLE_ENTITY,
                properties.getAppMsg().get("MSG_ARGUMENT_NULL")
            );
        }
        Node nodeOne = checkNotFound(
            nodeRepository.getByName(edgeGraph.getNodeOne()),
            String.format(properties.getNodeMsg().get("NODE_MSG_BY_NAME_ERROR"), edgeGraph.getNodeOne()),
            ErrorPlaceType.EDGE
        );
        Node nodeTwo = checkNotFound(
            nodeRepository.getByName(edgeGraph.getNodeTwo()),
            String.format(properties.getNodeMsg().get("NODE_MSG_BY_NAME_ERROR"), edgeGraph.getNodeTwo()),
            ErrorPlaceType.EDGE
        );
        if (isPresent(nodeOne, nodeTwo)) {
            throw new ApplicationException(
                ErrorType.DATA_ERROR,
                ErrorPlaceType.EDGE,
                HttpStatus.UNPROCESSABLE_ENTITY,
                String.format(
                    properties.getEdgeMsg().get("EDGE_MSG_ALREADY_PRESENT_ERROR"),
                    nodeOne.getName(),
                    nodeTwo.getName(),
                    nodeTwo.getName(),
                    nodeOne.getName()
                )
            );
        }
        Edge edge = new Edge(nodeOne, nodeTwo);
        return GraphUtil.edgeToEdgeGraph(edgeRepository.save(edge));
    }

    @Override
    public Set<EdgeGraph> create(final Set<EdgeGraph> edgeGraphs) throws NotFoundException {
        if (Objects.isNull(edgeGraphs)) {
            throw new ApplicationException(
                ErrorType.DATA_ERROR,
                ErrorPlaceType.EDGE,
                HttpStatus.UNPROCESSABLE_ENTITY,
                properties.getAppMsg().get("MSG_ARGUMENT_NULL")
            );
        }
        if (edgeGraphs.isEmpty()) {
            throw new ApplicationException(
                ErrorType.DATA_ERROR,
                ErrorPlaceType.EDGE,
                HttpStatus.UNPROCESSABLE_ENTITY,
                properties.getAppMsg().get("MSG_COLLECTION_EMPTY")
            );
        }
        Map<EdgeGraph, Map<String, Node>> nodes = new HashMap<>();
        for (EdgeGraph edgeGraph : edgeGraphs) {
            if (Objects.isNull(edgeGraph)) {
                throw new ApplicationException(
                    ErrorType.DATA_ERROR,
                    ErrorPlaceType.EDGE,
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    properties.getAppMsg().get("MSG_COLLECTION_CONTAIN_NULL")
                );
            }
            Node nodeOne = checkNotFound(
                nodeRepository.getByName(edgeGraph.getNodeOne()),
                String.format(properties.getNodeMsg().get("NODE_MSG_BY_NAME_ERROR"), edgeGraph.getNodeOne()),
                ErrorPlaceType.EDGE
            );
            Node nodeTwo = checkNotFound(
                nodeRepository.getByName(edgeGraph.getNodeTwo()),
                String.format(properties.getNodeMsg().get("NODE_MSG_BY_NAME_ERROR"), edgeGraph.getNodeTwo()),
                ErrorPlaceType.EDGE
            );
            if (isPresent(nodeOne, nodeTwo)) {
                throw new ApplicationException(
                    ErrorType.DATA_ERROR,
                    ErrorPlaceType.EDGE,
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    String.format(
                        properties.getEdgeMsg().get("EDGE_MSG_ALREADY_PRESENT_ERROR"),
                        nodeOne.getName(),
                        nodeTwo.getName(),
                        nodeTwo.getName(),
                        nodeOne.getName()
                    )
                );
            }
            Map<String, Node> nodeMap = new HashMap<>();
            nodeMap.put(edgeGraph.getNodeOne(), nodeOne);
            nodeMap.put(edgeGraph.getNodeTwo(), nodeTwo);
            nodes.put(edgeGraph, nodeMap);
        }
        List<Edge> edges = edgeGraphs.stream()
            .map(eg -> new Edge(
                nodes.get(eg).get(eg.getNodeOne()),
                nodes.get(eg).get(eg.getNodeTwo()))
            ).collect(Collectors.toList());
        return GraphUtil.edgesToEdgeGraphs(edgeRepository.saveAll(edges));
    }

    @Override
    public void delete(final Integer id) throws NotFoundException {
        if (edgeRepository.existsById(id)) {
            edgeRepository.deleteById(id);
        } else {
            throw new NotFoundException(String.format(properties.getAppMsg().get("MSG_BY_ID_ERROR"), ErrorPlaceType.EDGE, id), ErrorPlaceType.EDGE);
        }
    }

    @Override
    public void delete(final String nodeName) throws NotFoundException {
        List<Edge> edges = getEdges(nodeName);
        if (edges.isEmpty()) {
            throw new NotFoundException(String.format(properties.getEdgeMsg().get("EDGE_MSG_GET_BY_NAME_ERROR"), nodeName), ErrorPlaceType.EDGE);
        }
        edgeRepository.deleteInBatch(edges);
    }

    @Override
    public void delete(final String nodeNameOne, final String nodeNameTwo) throws NotFoundException {
        Edge edge = checkNotFound(getEdge(nodeNameOne, nodeNameTwo),
            String.format(properties.getEdgeMsg().get("EDGE_MSG_GET_ERROR"), nodeNameOne, nodeNameTwo),
            ErrorPlaceType.EDGE);
        edgeRepository.delete(edge);
    }

    @Override
    public void delete(Set<Edge> edges) throws NotFoundException {
        edgeRepository.deleteInBatch(edges);
    }

    @Override
    public void deleteAll() {
        edgeRepository.deleteAllInBatch();
    }

    @Override
    public EdgeGraph get(final String nodeNameOne, final String nodeNameTwo) throws NotFoundException {
        EdgeGraph edgeGraph = GraphUtil.edgeToEdgeGraph(getEdge(nodeNameOne, nodeNameTwo));
        return checkNotFound(edgeGraph,
            String.format(properties.getEdgeMsg().get("EDGE_MSG_GET_ERROR"), nodeNameOne, nodeNameTwo),
            ErrorPlaceType.EDGE);
    }

    private Edge getEdge(final String nodeNameOne, final String nodeNameTwo) {
        Node nodeOne = nodeRepository.getByName(nodeNameOne);
        Node nodeTwo = nodeRepository.getByName(nodeNameTwo);
        return edgeRepository.findEdgeByNodeOneAndNodeTwo(nodeOne, nodeTwo)
            .orElse(edgeRepository.findEdgeByNodeOneAndNodeTwo(nodeTwo, nodeOne).orElse(null));
    }

    @Override
    public Set<EdgeGraph> get(final String nodeName) {
        List<Edge> result = getEdges(nodeName);
        if (result.isEmpty()) {
            throw new NotFoundException(String.format(properties.getEdgeMsg()
                .get("EDGE_MSG_GET_BY_NAME_ERROR"), nodeName), ErrorPlaceType.EDGE);
        }
        return GraphUtil.edgesToEdgeGraphs(result);
    }

    private List<Edge> getEdges(final String nodeName) {
        Node node = nodeRepository.getByName(nodeName);
        return edgeRepository.findEdgesByNodeOneOrNodeTwo(node, node);
    }

    @Override
    public EdgeGraph getById(final Integer id) throws NotFoundException {
        EdgeGraph edgeGraph = GraphUtil.edgeToEdgeGraph(edgeRepository.findById(id)
            .orElse(null));
        return checkNotFound(edgeGraph, String.format(properties.getAppMsg().get("MSG_BY_ID_ERROR"),
            ErrorPlaceType.EDGE, id), ErrorPlaceType.EDGE);
    }

    @Override
    public Set<EdgeGraph> getAll() {
        return GraphUtil.edgesToEdgeGraphs(edgeRepository.findAll());
    }
}
