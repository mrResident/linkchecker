package ru.resprojects.linkchecker.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.resprojects.linkchecker.AppProperties;
import ru.resprojects.linkchecker.repositories.NodeRepository;
import ru.resprojects.linkchecker.util.GraphUtil;
import ru.resprojects.linkchecker.util.exeptions.ApplicationException;
import ru.resprojects.linkchecker.util.exeptions.ErrorPlaceType;
import ru.resprojects.linkchecker.util.exeptions.ErrorType;
import ru.resprojects.linkchecker.util.exeptions.NotFoundException;

import java.util.Objects;
import java.util.Set;

import static ru.resprojects.linkchecker.dto.GraphDto.NodeGraph;
import static ru.resprojects.linkchecker.util.ValidationUtil.checkNotFound;

@Service
public class GraphNodeServiceImpl implements GraphNodeService {

    private final NodeRepository nodeRepository;
    private final AppProperties properties;

    @Autowired
    public GraphNodeServiceImpl(final NodeRepository nodeRepository, final AppProperties properties) {
        this.nodeRepository = nodeRepository;
        this.properties = properties;
    }

    @Override
    public NodeGraph create(final NodeGraph nodeGraph) {
        if (Objects.isNull(nodeGraph)) {
            throw new ApplicationException(
                ErrorType.DATA_ERROR,
                ErrorPlaceType.NODE,
                HttpStatus.UNPROCESSABLE_ENTITY,
                properties.getAppMsg().get("MSG_ARGUMENT_NULL")
            );
        }
        return GraphUtil.nodeToNodeGraph(nodeRepository.save(
            GraphUtil.nodeGraphToNode(nodeGraph)));
    }

    @Override
    public Set<NodeGraph> create(Set<NodeGraph> nodeGraphs) {
        if (Objects.isNull(nodeGraphs)) {
            throw new ApplicationException(
                ErrorType.DATA_ERROR,
                ErrorPlaceType.NODE,
                HttpStatus.UNPROCESSABLE_ENTITY,
                properties.getAppMsg().get("MSG_ARGUMENT_NULL")
            );
        }
        if (nodeGraphs.isEmpty()) {
            throw new ApplicationException(
                ErrorType.DATA_ERROR,
                ErrorPlaceType.NODE,
                HttpStatus.UNPROCESSABLE_ENTITY,
                properties.getAppMsg().get("MSG_COLLECTION_EMPTY")
            );
        }
        nodeGraphs.forEach(nodeGraph -> {
            if (Objects.isNull(nodeGraph)) {
                throw new ApplicationException(
                    ErrorType.DATA_ERROR,
                    ErrorPlaceType.NODE,
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    properties.getAppMsg().get("MSG_COLLECTION_CONTAIN_NULL")
                );
            }
        });
        return GraphUtil.nodesToNodeGraphs(nodeRepository.saveAll(
            GraphUtil.nodeGraphsToNodes(nodeGraphs)));
    }

    @Override
    public void update(final NodeGraph nodeGraph) throws NotFoundException {
        if (Objects.isNull(nodeGraph)) {
            throw new ApplicationException(
                ErrorType.DATA_ERROR,
                ErrorPlaceType.NODE,
                HttpStatus.UNPROCESSABLE_ENTITY,
                properties.getAppMsg().get("MSG_ARGUMENT_NULL")
            );
        }
        checkNotFound(nodeRepository.save(
            GraphUtil.nodeGraphToNode(nodeGraph)), properties.getNodeMsg().get("NODE_MSG_UPDATE_ERROR") + nodeGraph.getId(),
            ErrorPlaceType.NODE
        );
    }

    @Override
    public void delete(final Integer id) throws NotFoundException {
        if (nodeRepository.existsById(id)) {
            nodeRepository.deleteById(id);
        } else {
            throw new NotFoundException(String.format(properties.getAppMsg().get("MSG_BY_ID_ERROR"), ErrorPlaceType.NODE, id), ErrorPlaceType.NODE);
        }
    }

    @Override
    public void delete(final String name) throws NotFoundException {
        if (nodeRepository.existsByName(name)) {
            nodeRepository.deleteByName(name);
        } else {
            throw new NotFoundException(String.format(properties.getNodeMsg().get("NODE_MSG_BY_NAME_ERROR"), name), ErrorPlaceType.NODE);
        }
    }

    @Override
    public void delete(final NodeGraph nodeGraph) throws NotFoundException {
        if (Objects.isNull(nodeGraph)) {
            throw new ApplicationException(
                ErrorType.DATA_ERROR,
                ErrorPlaceType.NODE,
                HttpStatus.UNPROCESSABLE_ENTITY,
                properties.getAppMsg().get("MSG_ARGUMENT_NULL")
            );
        }
        NodeGraph nodeFromRepo = GraphUtil.nodeToNodeGraph(nodeRepository
            .findById(nodeGraph.getId()).orElse(null));
        if (nodeGraph.equals(nodeFromRepo)) {
            nodeRepository.deleteById(nodeGraph.getId());
        } else {
            throw new NotFoundException(String.format(
                properties.getNodeMsg().get("NODE_MSG_BY_OBJECT_ERROR"), nodeGraph.toString()), ErrorPlaceType.NODE
            );
        }
    }

    @Override
    public void deleteAll() {
        nodeRepository.deleteAllInBatch();
    }

    @Override
    public Set<NodeGraph> getAll() {
        return GraphUtil.nodesToNodeGraphs(nodeRepository.findAll());
    }

    @Override
    public NodeGraph get(final String name) throws NotFoundException {
        NodeGraph nodeGraph = GraphUtil.nodeToNodeGraph(nodeRepository.getByName(name));
        return checkNotFound(nodeGraph,
            String.format(properties.getNodeMsg().get("NODE_MSG_BY_NAME_ERROR"), name), ErrorPlaceType.NODE);
    }

    @Override
    public NodeGraph getById(final Integer id) throws NotFoundException {
        NodeGraph nodeGraph = GraphUtil.nodeToNodeGraph(nodeRepository
            .findById(id).orElse(null));
        return checkNotFound(nodeGraph,
            String.format(properties.getAppMsg().get("MSG_BY_ID_ERROR"), ErrorPlaceType.NODE, id), ErrorPlaceType.NODE);
    }

}
