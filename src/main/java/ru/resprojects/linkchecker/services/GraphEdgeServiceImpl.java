package ru.resprojects.linkchecker.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import ru.resprojects.linkchecker.model.Edge;
import ru.resprojects.linkchecker.model.Node;
import ru.resprojects.linkchecker.repositories.EdgeRepository;
import ru.resprojects.linkchecker.repositories.NodeRepository;
import ru.resprojects.linkchecker.util.GraphUtil;
import ru.resprojects.linkchecker.util.exeptions.NotFoundException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.resprojects.linkchecker.dto.GraphDto.EdgeGraph;
import static ru.resprojects.linkchecker.util.ValidationUtil.checkNotFound;
import static ru.resprojects.linkchecker.util.Messages.*;

@Service
public class GraphEdgeServiceImpl implements GraphEdgeService {

    private final EdgeRepository edgeRepository;
    private final NodeRepository nodeRepository;
    private boolean isStateChanged;

    @Autowired
    public GraphEdgeServiceImpl(EdgeRepository edgeRepository, NodeRepository nodeRepository) {
        this.edgeRepository = edgeRepository;
        this.nodeRepository = nodeRepository;
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
    public boolean isStateChanged() {
        return isStateChanged;
    }

    @Override
    public void resetCurrentState() {
        isStateChanged = false;
    }

    @Override
    public EdgeGraph create(final EdgeGraph edgeGraph) throws NotFoundException {
        Assert.notNull(edgeGraph, MSG_NOT_NULL);
        Node nodeOne = checkNotFound(
            nodeRepository.getByName(edgeGraph.getNodeOne()),
            String.format(NODE_MSG_BY_NAME_ERROR, edgeGraph.getNodeOne())
        );
        Node nodeTwo = checkNotFound(
            nodeRepository.getByName(edgeGraph.getNodeTwo()),
            String.format(NODE_MSG_BY_NAME_ERROR, edgeGraph.getNodeTwo())
        );
        if (isPresent(nodeOne, nodeTwo)) {
            throw new IllegalArgumentException(
                String.format(
                    EDGE_MSG_ALREADY_PRESENT_ERROR,
                    nodeOne.getName(),
                    nodeTwo.getName(),
                    nodeTwo.getName(),
                    nodeOne.getName()
                )
            );
        }
        Edge edge = new Edge(nodeOne, nodeTwo);
        isStateChanged = true;
        return GraphUtil.edgeToEdgeGraph(edgeRepository.save(edge));
    }

    @Override
    public Set<EdgeGraph> create(final Set<EdgeGraph> edgeGraphs) throws NotFoundException {
        Assert.notNull(edgeGraphs, MSG_NOT_NULL);
        Assert.notEmpty(edgeGraphs, MSG_COLLECTION_EMPTY);
        Map<EdgeGraph, Map<String, Node>> nodes = new HashMap<>();
        for (EdgeGraph edgeGraph : edgeGraphs) {
            Assert.notNull(edgeGraph, MSG_COLLECTION_CONTAIN_NULL);
            Node nodeOne = checkNotFound(
                nodeRepository.getByName(edgeGraph.getNodeOne()),
                String.format(NODE_MSG_BY_NAME_ERROR, edgeGraph.getNodeOne())
            );
            Node nodeTwo = checkNotFound(
                nodeRepository.getByName(edgeGraph.getNodeTwo()),
                String.format(NODE_MSG_BY_NAME_ERROR, edgeGraph.getNodeTwo())
            );
            if (isPresent(nodeOne, nodeTwo)) {
                throw new IllegalArgumentException(
                    String.format(
                        EDGE_MSG_ALREADY_PRESENT_ERROR,
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
        isStateChanged = true;
        return GraphUtil.edgesToEdgeGraphs(edgeRepository.saveAll(edges));
    }

    @Override
    public void delete(final Integer id) throws NotFoundException {
        if (edgeRepository.existsById(id)) {
            edgeRepository.deleteById(id);
            isStateChanged = true;
        } else {
            throw new NotFoundException(String.format(MSG_BY_ID_ERROR, "Edge", id));
        }
    }

    @Override
    public void delete(final String nodeName) throws NotFoundException {
        List<Edge> edges = getEdges(nodeName);
        if (edges.isEmpty()) {
            throw new NotFoundException(String.format(EDGE_MSG_GET_BY_NAME_ERROR, nodeName));
        }
        edgeRepository.deleteInBatch(edges);
        isStateChanged = true;
    }

    @Override
    public void delete(final String nodeNameOne, final String nodeNameTwo) throws NotFoundException {
        Edge edge = checkNotFound(getEdge(nodeNameOne, nodeNameTwo),
            String.format(EDGE_MSG_GET_ERROR, nodeNameOne, nodeNameTwo));
        edgeRepository.delete(edge);
        isStateChanged = true;
    }

    @Override
    public void deleteAll() {
        edgeRepository.deleteAllInBatch();
        isStateChanged = true;
    }

    @Override
    public EdgeGraph get(final String nodeNameOne, final String nodeNameTwo) throws NotFoundException {
        EdgeGraph edgeGraph = GraphUtil.edgeToEdgeGraph(getEdge(nodeNameOne, nodeNameTwo));
        return checkNotFound(edgeGraph,
            String.format(EDGE_MSG_GET_ERROR, nodeNameOne, nodeNameTwo));
    }

    private Edge getEdge(final String nodeNameOne, final String nodeNameTwo) {
        Node nodeOne = nodeRepository.getByName(nodeNameOne);
        Node nodeTwo = nodeRepository.getByName(nodeNameTwo);
        return edgeRepository.findEdgeByNodeOneAndNodeTwo(nodeOne, nodeTwo)
            .orElse(edgeRepository.findEdgeByNodeOneAndNodeTwo(nodeTwo, nodeOne).orElse(null));
    }

    @Override
    public Set<EdgeGraph> get(final String nodeName) {
        return GraphUtil.edgesToEdgeGraphs(getEdges(nodeName));
    }

    private List<Edge> getEdges(final String nodeName) {
        Node node = nodeRepository.getByName(nodeName);
        return edgeRepository.findEdgesByNodeOneOrNodeTwo(node, node);
    }

    @Override
    public EdgeGraph getById(final Integer id) throws NotFoundException {
        EdgeGraph edgeGraph = GraphUtil.edgeToEdgeGraph(edgeRepository.findById(id)
            .orElse(null));
        return checkNotFound(edgeGraph, String.format(MSG_BY_ID_ERROR, "Edge", id));
    }

    @Override
    public Set<EdgeGraph> getAll() {
        return GraphUtil.edgesToEdgeGraphs(edgeRepository.findAll());
    }
}
