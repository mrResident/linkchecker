package ru.resprojects.linkchecker.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import ru.resprojects.linkchecker.repositories.NodeRepository;
import ru.resprojects.linkchecker.util.GraphUtil;
import ru.resprojects.linkchecker.util.exeptions.NotFoundException;

import java.util.Set;

import static ru.resprojects.linkchecker.dto.GraphDto.NodeGraph;
import static ru.resprojects.linkchecker.util.ValidationUtil.checkNotFoundWithId;

@Service
public class GraphNodeServiceImpl implements GraphNodeService {

    private final NodeRepository nodeRepository;

    @Autowired
    public GraphNodeServiceImpl(NodeRepository nodeRepository) {
        this.nodeRepository = nodeRepository;
    }

    @Override
    public NodeGraph create(final NodeGraph nodeGraph) {
        Assert.notNull(nodeGraph, "Node must not be null");
        return GraphUtil.nodeToNodeGraph(nodeRepository.save(GraphUtil.nodeGraphToNode(nodeGraph)));
    }

    @Override
    public void update(final NodeGraph nodeGraph) throws NotFoundException {
        Assert.notNull(nodeGraph, "Node must not be null");
        checkNotFoundWithId(nodeRepository.save(
            GraphUtil.nodeGraphToNode(nodeGraph)),
            nodeGraph.getId()
        );
    }

    @Override
    public void delete(final Integer id) throws NotFoundException {
        if (nodeRepository.existsById(id)) {
            nodeRepository.deleteById(id);
        } else {
            throw new NotFoundException(String.format("Node with ID = %d is not found", id));
        }
    }

    @Override
    public void delete(final String name) throws NotFoundException {
        if (nodeRepository.existsByName(name)) {
            nodeRepository.deleteByName(name);
        } else {
            throw new NotFoundException(String.format("Node with NAME = %s is not found", name));
        }
    }

    @Override
    public void delete(final NodeGraph nodeGraph) throws NotFoundException {
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
    public Set<NodeGraph> getAll() {
        return GraphUtil.nodesToNodeGraphs(nodeRepository.findAll());
    }

    @Override
    public NodeGraph get(final String name) throws NotFoundException {
        NodeGraph nodeGraph = GraphUtil.nodeToNodeGraph(nodeRepository.getByName(name));
        if (nodeGraph == null) {
            throw new NotFoundException(String.format("Node with name %s is not found", name));
        }
        return nodeGraph;
    }

    @Override
    public NodeGraph getById(final Integer id) throws NotFoundException {
        NodeGraph nodeGraph = GraphUtil.nodeToNodeGraph(nodeRepository.findById(id).orElse(null));
        if (nodeGraph == null) {
            throw new NotFoundException(String.format("Node with ID = %d is not found", id));
        }
        return nodeGraph;
    }

}
