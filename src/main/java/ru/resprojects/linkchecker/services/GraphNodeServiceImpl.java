package ru.resprojects.linkchecker.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import ru.resprojects.linkchecker.repositories.NodeRepository;
import ru.resprojects.linkchecker.util.GraphUtil;
import ru.resprojects.linkchecker.util.exeptions.NotFoundException;

import java.util.Set;

import static ru.resprojects.linkchecker.dto.GraphDto.NodeGraph;
import static ru.resprojects.linkchecker.util.ValidationUtil.checkNotFound;
import static ru.resprojects.linkchecker.util.Messages.*;

@Service
public class GraphNodeServiceImpl implements GraphNodeService {

    private final NodeRepository nodeRepository;
    private boolean isStateChanged;

    @Autowired
    public GraphNodeServiceImpl(NodeRepository nodeRepository) {
        this.nodeRepository = nodeRepository;
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
    public NodeGraph create(final NodeGraph nodeGraph) {
        Assert.notNull(nodeGraph, MSG_NOT_NULL);
        isStateChanged = true;
        return GraphUtil.nodeToNodeGraph(nodeRepository.save(
            GraphUtil.nodeGraphToNode(nodeGraph)));
    }

    @Override
    public Set<NodeGraph> create(Set<NodeGraph> nodeGraphs) {
        Assert.notNull(nodeGraphs, MSG_NOT_NULL);
        Assert.notEmpty(nodeGraphs, MSG_COLLECTION_EMPTY);
        nodeGraphs.forEach(nodeGraph -> Assert.notNull(nodeGraph, MSG_COLLECTION_CONTAIN_NULL));
        isStateChanged = true;
        return GraphUtil.nodesToNodeGraphs(nodeRepository.saveAll(
            GraphUtil.nodeGraphsToNodes(nodeGraphs)));
    }

    @Override
    public void update(final NodeGraph nodeGraph) throws NotFoundException {
        Assert.notNull(nodeGraph, MSG_NOT_NULL);
        checkNotFound(nodeRepository.save(
            GraphUtil.nodeGraphToNode(nodeGraph)), NODE_MSG_UPDATE_ERROR + nodeGraph.getId()
        );
        isStateChanged = true;
    }

    @Override
    public void delete(final Integer id) throws NotFoundException {
        if (nodeRepository.existsById(id)) {
            nodeRepository.deleteById(id);
            isStateChanged = true;
        } else {
            throw new NotFoundException(String.format(MSG_BY_ID_ERROR, "Node", id));
        }
    }

    @Override
    public void delete(final String name) throws NotFoundException {
        if (nodeRepository.existsByName(name)) {
            nodeRepository.deleteByName(name);
            isStateChanged = true;
        } else {
            throw new NotFoundException(String.format(NODE_MSG_BY_NAME_ERROR, name));
        }
    }

    @Override
    public void delete(final NodeGraph nodeGraph) throws NotFoundException {
        Assert.notNull(nodeGraph, MSG_NOT_NULL);
        NodeGraph nodeFromRepo = GraphUtil.nodeToNodeGraph(nodeRepository
            .findById(nodeGraph.getId()).orElse(null));
        if (nodeGraph.equals(nodeFromRepo)) {
            nodeRepository.deleteById(nodeGraph.getId());
            isStateChanged = true;
        } else {
            throw new NotFoundException(String.format(
                NODE_MSG_BY_OBJECT_ERROR, nodeGraph.toString())
            );
        }
    }

    @Override
    public void deleteAll() {
        nodeRepository.deleteAllInBatch();
        isStateChanged = true;
    }

    @Override
    public Set<NodeGraph> getAll() {
        return GraphUtil.nodesToNodeGraphs(nodeRepository.findAll());
    }

    @Override
    public NodeGraph get(final String name) throws NotFoundException {
        NodeGraph nodeGraph = GraphUtil.nodeToNodeGraph(nodeRepository.getByName(name));
        return checkNotFound(nodeGraph,
            String.format(NODE_MSG_BY_NAME_ERROR, name));
    }

    @Override
    public NodeGraph getById(final Integer id) throws NotFoundException {
        NodeGraph nodeGraph = GraphUtil.nodeToNodeGraph(nodeRepository
            .findById(id).orElse(null));
        return checkNotFound(nodeGraph,
            String.format(MSG_BY_ID_ERROR, "Node", id));
    }

}
