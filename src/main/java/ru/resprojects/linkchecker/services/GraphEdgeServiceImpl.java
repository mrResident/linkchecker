package ru.resprojects.linkchecker.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.resprojects.linkchecker.model.Node;
import ru.resprojects.linkchecker.repositories.EdgeRepository;
import ru.resprojects.linkchecker.repositories.NodeRepository;
import ru.resprojects.linkchecker.util.GraphUtil;
import ru.resprojects.linkchecker.util.exeptions.NotFoundException;

import java.util.Set;

import static ru.resprojects.linkchecker.dto.GraphDto.EdgeGraph;
import static ru.resprojects.linkchecker.util.ValidationUtil.checkNotFound;
import static ru.resprojects.linkchecker.util.Messages.*;

@Service
public class GraphEdgeServiceImpl implements GraphEdgeService {

    private final EdgeRepository edgeRepository;
    private final NodeRepository nodeRepository;

    @Autowired
    public GraphEdgeServiceImpl(EdgeRepository edgeRepository, NodeRepository nodeRepository) {
        this.edgeRepository = edgeRepository;
        this.nodeRepository = nodeRepository;
    }

    @Override
    public EdgeGraph create(EdgeGraph edgeGraph) {
        return null;
    }

    @Override
    public void delete(Integer id) throws NotFoundException {

    }

    @Override
    public void delete(EdgeGraph edgeGraph) throws NotFoundException {

    }

    @Override
    public EdgeGraph get(String nodeNameOne, String nodeNameTwo) throws NotFoundException {
        Node nodeOne = nodeRepository.getByName(nodeNameOne);
        Node nodeTwo = nodeRepository.getByName(nodeNameTwo);
        EdgeGraph edgeGraph = GraphUtil.edgeToEdgeGraph(edgeRepository.
            findEdgeByNodeOneAndNodeTwo(nodeOne, nodeTwo).orElse(null));
        return checkNotFound(edgeGraph, String.format(
            EDGE_MSG_GET_ERROR,
            nodeNameOne,
            nodeNameTwo));
    }

    @Override
    public EdgeGraph getById(Integer id) throws NotFoundException {
        EdgeGraph edgeGraph = GraphUtil.edgeToEdgeGraph(edgeRepository.findById(id)
            .orElse(null));
        return checkNotFound(edgeGraph, String.format(MSG_BY_ID_ERROR, "Edge", id));
    }

    @Override
    public Set<EdgeGraph> getAll() {
        return GraphUtil.edgesToEdgeGraphs(edgeRepository.findAll());
    }
}
