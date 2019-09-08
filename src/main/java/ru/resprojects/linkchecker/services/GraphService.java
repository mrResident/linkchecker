package ru.resprojects.linkchecker.services;

import ru.resprojects.linkchecker.dto.GraphDto;
import ru.resprojects.linkchecker.util.exeptions.NotFoundException;

import java.util.Set;

import static ru.resprojects.linkchecker.dto.GraphDto.NodeGraph;

/**
 * GraphService - the interface for work with graph. Graph may be represented
 * how one big linked graph with subgraphs and may be represented how set of
 * unlinked graphs.
 */
public interface GraphService extends GraphNodeService, GraphEdgeService {

    /**
     * Checking input graph data, removing cycles from input graph and saving
     * to DB.
     * @param graphTo graph {@link GraphDto}
     * @return graph.
     */
    GraphDto create(GraphDto graphTo);

    /**
     * Get graph.
     * @return graph {@link GraphDto}
     * @throws NotFoundException if graph not found in database or corrupted.
     */
    GraphDto get() throws NotFoundException;

    /**
     * Remove all graph.
     */
    void clear();

    /**
     * Remove subgraph from graph. If subgraph is not found, throws exception.
     * @param graphDto subgraph.
     * @throws NotFoundException if graph not found in database or corrupted.
     */
    void delete(GraphDto graphDto) throws NotFoundException;

    /**
     * Checking route between nodes.
     * @param nodes set of graph nodes. {@link GraphDto.NodeGraph}
     * @return check result in JSON format.
     */
    String checkRoute(Set<NodeGraph> nodes);
}

