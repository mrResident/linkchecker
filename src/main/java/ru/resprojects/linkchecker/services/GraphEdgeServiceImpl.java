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
    public void delete(EdgeGraph edgeGraph) throws NotFoundException {

    }

    @Override
    public EdgeGraph get(String nodeNameOne, String nodeNameTwo) throws NotFoundException {
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
    public EdgeGraph getById(Integer id) throws NotFoundException {
        EdgeGraph edgeGraph = GraphUtil.edgeToEdgeGraph(edgeRepository.findById(id)
            .orElse(null));
        if (edgeGraph == null) {
            throw new NotFoundException(String.format("Edge with ID = %d is not found", id));
        }
        return edgeGraph;
    }

    @Override
    public Set<EdgeGraph> getAll() {
        return GraphUtil.edgesToEdgeGraphs(edgeRepository.findAll());
    }
}
