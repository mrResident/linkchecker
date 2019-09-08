package ru.resprojects.linkchecker.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.resprojects.linkchecker.dto.GraphDto;
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
    public EdgeGraph getEdge(String nodeNameOne, String nodeNameTwo) throws NotFoundException {
        return null;
    }

    @Override
    public EdgeGraph getEdgeById(Integer id) throws NotFoundException {
        return null;
    }

    @Override
    public Set<EdgeGraph> getAllEdges() {
        return GraphUtil.edgesToEdgeGraphs(edgeRepository.findAll());
    }

    @Override
    public NodeGraph createNode(NodeGraph node) {
        return null;
    }

    @Override
    public void updateNode(NodeGraph node) {

    }

    @Override
    public void deleteNode(String name) throws NotFoundException {

    }

    @Override
    public void deleteNode(NodeGraph nodeGraph) throws NotFoundException {

    }

    @Override
    public NodeGraph getNode(String name) throws NotFoundException {
        return null;
    }

    @Override
    public NodeGraph getNodeById(Integer id) throws NotFoundException {
        return null;
    }

    @Override
    public Set<NodeGraph> getAllNodes() {
        return GraphUtil.nodesToNodeGraphs(nodeRepository.findAll());
    }
}
