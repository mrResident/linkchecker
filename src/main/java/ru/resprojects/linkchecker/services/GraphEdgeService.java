package ru.resprojects.linkchecker.services;

import ru.resprojects.linkchecker.util.exeptions.NotFoundException;
import ru.resprojects.linkchecker.dto.GraphDto;

import java.util.Set;

import static ru.resprojects.linkchecker.dto.GraphDto.EdgeGraph;

/**
 * GraphEdgeService - the interface for work with graph edges.
 */
public interface GraphEdgeService {

    /**
     * Create graph edge. Nodes that linked by the edge, must be
     * exist in graph, else throw exception.
     * @param edgeGraph Graph edge that link two nodes.
     * @return added graph edge.
     */
    EdgeGraph create(final EdgeGraph edgeGraph);

    /**
     * Search graph edge by id and delete from graph.
     * @param id of edge of the graph.
     * @throws NotFoundException edge is not found in the graph.
     */
    void delete(final Integer id) throws NotFoundException;

    /**
     * Search graph edge by object {@link EdgeGraph} and delete from graph.
     * @param edgeGraph object {@link EdgeGraph}
     * @throws NotFoundException edge is not found in the graph.
     */
    void delete(final EdgeGraph edgeGraph) throws NotFoundException;

    /**
     * Get edge of the graph, that link two nodes {@link GraphDto.NodeGraph}
     * in exist graph.
     * @param nodeNameOne unique name of the first graph node.
     * @param nodeNameTwo unique name of the second graph node.
     * @return graph edge {@link EdgeGraph}.
     * @throws NotFoundException if edge is not found in the graph.
     */
    EdgeGraph get(final String nodeNameOne, final String nodeNameTwo) throws NotFoundException;

    /**
     * Get edge of the graph by edge id.
     * @param id of edge graph.
     * @return graph edge {@link EdgeGraph}.
     * @throws NotFoundException if edge is not found in the graph.
     */
    EdgeGraph getById(final Integer id) throws NotFoundException;

    /**
     * Get all edges from graph.
     * @return set of graph edges.
     */
    Set<EdgeGraph> getAll();
}

