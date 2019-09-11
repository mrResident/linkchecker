package ru.resprojects.linkchecker.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.resprojects.linkchecker.dto.GraphDto;
import ru.resprojects.linkchecker.model.Node;
import ru.resprojects.linkchecker.repositories.EdgeRepository;
import ru.resprojects.linkchecker.repositories.NodeRepository;
import ru.resprojects.linkchecker.util.GraphUtil;
import ru.resprojects.linkchecker.util.exeptions.NotFoundException;

import static ru.resprojects.linkchecker.dto.GraphDto.*;

import java.util.Set;

@Service
public class GraphServiceImpl implements GraphService {

    private static Logger LOG = LoggerFactory.getLogger(GraphServiceImpl.class);

    private final EdgeRepository edgeRepository;
    private final NodeRepository nodeRepository;

    @Autowired
    public GraphServiceImpl(EdgeRepository edgeRepository, NodeRepository nodeRepository) {
        this.edgeRepository = edgeRepository;
        this.nodeRepository = nodeRepository;
    }

    @Override
    public GraphDto create(GraphDto graphTo) {
        return null;
    }

    @Override
    public GraphDto get() throws NotFoundException {
        return null;
    }

    @Override
    public void clear() {

    }

    @Override
    public void delete(GraphDto graphDto) throws NotFoundException {

    }

    @Override
    public String checkRoute(Set<NodeGraph> nodes) {
        return null;
    }

    @Override
    public EdgeGraph createEdge(EdgeGraph edge) {
        return null;
    }

    @Override
    public void deleteEdge(EdgeGraph edge) throws NotFoundException {

    }

    @Override
    public NodeGraph createNode(NodeGraph node) {
        return null;
    }

    @Override
    public void updateNode(NodeGraph node) {

    }

    @Override
    public void deleteNode(Integer id) throws NotFoundException {
        if (nodeRepository.existsById(id)) {
            nodeRepository.deleteById(id);
        } else {
            throw new NotFoundException(String.format("Node with ID = %d is not found", id));
        }
    }

    @Override
    public void deleteNode(String name) throws NotFoundException {
        if (nodeRepository.existsByName(name)) {
            nodeRepository.deleteByName(name);
        } else {
            throw new NotFoundException(String.format("Node with NAME = %s is not found", name));
        }
    }

    @Override
    public void deleteNode(NodeGraph nodeGraph) throws NotFoundException {
        if (nodeGraph == null) {
            throw new NotFoundException("Node null is not found");
        }
        NodeGraph nodeFromRepo = GraphUtil.nodeToNodeGraph(nodeRepository
            .findById(nodeGraph.getId()).orElse(null));
        if (nodeGraph.equals(nodeFromRepo)) {
            nodeRepository.deleteById(nodeGraph.getId());
        } else {
            throw new NotFoundException(String.format(
                "Node %s is not found", nodeGraph.toString())
            );
        }
    }

    @Override
    public Set<NodeGraph> getAllNodes() {
        return GraphUtil.nodesToNodeGraphs(nodeRepository.findAll());
    }

    @Override
    public Set<EdgeGraph> getAllEdges() {
        return GraphUtil.edgesToEdgeGraphs(edgeRepository.findAll());
    }

    @Override
    public NodeGraph getNode(String name) throws NotFoundException {
        NodeGraph nodeGraph = GraphUtil.nodeToNodeGraph(nodeRepository.getByName(name));
        if (nodeGraph == null) {
            throw new NotFoundException(String.format("Node with name %s is not found", name));
        }
        return nodeGraph;
    }

    @Override
    public NodeGraph getNodeById(Integer id) throws NotFoundException {
        NodeGraph nodeGraph = GraphUtil.nodeToNodeGraph(nodeRepository.findById(id).orElse(null));
        if (nodeGraph == null) {
            throw new NotFoundException(String.format("Node with ID = %d is not found", id));
        }
        return nodeGraph;
    }

    @Override
    public EdgeGraph getEdge(String nodeNameOne, String nodeNameTwo) throws NotFoundException {
        Node nodeOne = nodeRepository.getByName(nodeNameOne);
        Node nodeTwo = nodeRepository.getByName(nodeNameTwo);
        EdgeGraph edgeGraph = GraphUtil.edgeToEdgeGraph(edgeRepository.
            findEdgeByNodeOneAndNodeTwo(nodeOne, nodeTwo).orElse(null));
        if (edgeGraph == null) {
            throw new NotFoundException(String.format(
                "Edge for nodes [%s, %s] is not found",
                nodeNameOne,
                nodeNameTwo
            ));
        }
        return edgeGraph;
    }

    @Override
    public EdgeGraph getEdgeById(Integer id) throws NotFoundException {
        EdgeGraph edgeGraph = GraphUtil.edgeToEdgeGraph(edgeRepository.findById(id)
            .orElse(null));
        if (edgeGraph == null) {
            throw new NotFoundException(String.format("Edge with ID = %d is not found", id));
        }
        return edgeGraph;
    }
}
