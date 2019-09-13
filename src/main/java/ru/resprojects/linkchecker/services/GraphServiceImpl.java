package ru.resprojects.linkchecker.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.resprojects.linkchecker.dto.GraphDto;
import ru.resprojects.linkchecker.util.exeptions.NotFoundException;

import java.util.Set;

import static ru.resprojects.linkchecker.dto.GraphDto.NodeGraph;

@Service
public class GraphServiceImpl implements GraphService {

    private static Logger LOG = LoggerFactory.getLogger(GraphServiceImpl.class);

    private final GraphEdgeService edge;
    private final GraphNodeService node;

    @Autowired
    public GraphServiceImpl(GraphEdgeService edge, GraphNodeService node) {
        this.edge = edge;
        this.node = node;
    }

    @Override
    public GraphDto create(final GraphDto graphTo) {
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
    public void delete(final GraphDto graphDto) throws NotFoundException {

    }

    @Override
    public String checkRoute(Set<NodeGraph> nodes) {
        return null;
    }

    public GraphEdgeService getEdge() {
        return edge;
    }

    public GraphNodeService getNode() {
        return node;
    }
}
