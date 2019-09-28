package ru.resprojects.linkchecker.services;

import ru.resprojects.linkchecker.util.exeptions.NotFoundException;

import java.util.Set;

import static ru.resprojects.linkchecker.dto.GraphDto.EdgeGraph;

/**
 * GraphEdgeService - the interface for work with graph edges.
 */
public interface GraphEdgeService extends ChangedState {

    /**
     * Create edge of the graph. Nodes that linked by the edge, must be
     * exist in graph, else throw exception.
     * @param edgeGraph edge {@link EdgeGraph} of the graph.
     * @return added graph edge.
     * @throws NotFoundException while creating edge
     */
    EdgeGraph create(final EdgeGraph edgeGraph) throws NotFoundException;

    /**
     * Batch creation edges of the graph. Nodes that linked by the edge, must be
     * exist in graph, else throw exception.
     * @param edgeGraphs set of graph edges {@link EdgeGraph}
     * @return added graph edges.
     * @throws NotFoundException while creating edges
     */
    Set<EdgeGraph> create(final Set<EdgeGraph> edgeGraphs) throws NotFoundException;

    /**
     * Search edge of the graph by id and delete from graph.
     * @param id of edge of the graph.
     * @throws NotFoundException if edge is not found in the graph.
     */
    void delete(final Integer id) throws NotFoundException;

    /**
     * Search edges by unique name of the graph node and delete them from graph.
     * Since the edge describes the connection of two nodes, the search occurs
     * on the node one or node two.
     * @param nodeName unique name of the graph node.
     * @throws NotFoundException if edge is not found in the graph.
     */
    void delete(final String nodeName) throws NotFoundException;

    /**
     * Search edge of the graph by node one and node two and if edge contain
     * both these nodes then delete edge from graph.
     * @param nodeNameOne unique name of the first graph node.
     * @param nodeNameTwo unique name of the second graph node.
     * @throws NotFoundException if edge is not found in the graph.
     */
    void delete(final String nodeNameOne, final String nodeNameTwo) throws NotFoundException;

    /**
     * Removing all edges from the graph.
     */
    void deleteAll();

    /**
     * Search edge of the graph by node one and node two and if edge contain
     * both these nodes then return edge else throw exception.
     * @param nodeNameOne unique name of the first graph node.
     * @param nodeNameTwo unique name of the second graph node.
     * @return graph edge {@link EdgeGraph}.
     * @throws NotFoundException if edge is not found in the graph.
     */
    EdgeGraph get(final String nodeNameOne, final String nodeNameTwo) throws NotFoundException;

    /**
     * Search edges by unique name of the graph node and return it.
     * Since the edge describes the connection of two nodes, the search occurs
     * on the node one or node two.
     * @param nodeName unique name of the graph node.
     * @return set of edges of the graph
     */
    Set<EdgeGraph> get(final String nodeName);

    /**
     * Get edge of the graph by id.
     * @param id of edge ot the graph.
     * @return edge {@link EdgeGraph} of the graph.
     * @throws NotFoundException if edge is not found in the graph.
     */
    EdgeGraph getById(final Integer id) throws NotFoundException;

    /**
     * Get all edges from graph.
     * @return set of edges ot the graph.
     */
    Set<EdgeGraph> getAll();
}

