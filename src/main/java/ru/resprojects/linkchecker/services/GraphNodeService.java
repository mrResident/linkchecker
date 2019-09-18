package ru.resprojects.linkchecker.services;

import ru.resprojects.linkchecker.util.exeptions.NotFoundException;

import java.util.Set;

import static ru.resprojects.linkchecker.dto.GraphDto.NodeGraph;

/**
 * GraphNodeService - the interface for work with graph nodes.
 */
public interface GraphNodeService {

    /**
     * Create node of the graph.
     * @param nodeGraph graph node {@link NodeGraph}
     * @return added graph node.
     */
    NodeGraph create(final NodeGraph nodeGraph);

    /**
     * Batch creation nodes of the graph
     * @param nodeGraphs set of graph nodes {@link NodeGraph}
     * @return added graph nodes.
     */
    Set<NodeGraph> create(final Set<NodeGraph> nodeGraphs);

    /**
     * Update node of the graph.
     * @param nodeGraph  graph node {@link NodeGraph}
     * @throws NotFoundException if node not updated
     */
    void update(final NodeGraph nodeGraph) throws NotFoundException;

    /**
     * Search graph node by id and delete from graph.
     * @param id of node of the graph.
     * @throws NotFoundException if node not found in the graph.
     */
    void delete(final Integer id) throws NotFoundException;

    /**
     * Search graph node by unique name and delete from graph.
     * @param name unique name of graph node.
     * @throws NotFoundException if node not found in the graph.
     */
    void delete(final String name) throws NotFoundException;

    /**
     * Search graph node by object {@link NodeGraph} and delete from graph.
     * @param nodeGraph object {@link NodeGraph}.
     * @throws NotFoundException if node not found in the graph.
     */
    void delete(final NodeGraph nodeGraph) throws NotFoundException;

    /**
     * Will be deleted all nodes and edges associated with
     * these nodes in the graph.
     */
    void deleteAll();

    /**
     * Search and return graph node by unique name.
     * @param name unique name of graph node.
     * @return node of the graph.
     * @throws NotFoundException if node not found in the graph.
     */
    NodeGraph get(final String name) throws NotFoundException;

    /**
     * Search and return graph node by id.
     * @param id of node of the graph.
     * @return node of the graph.
     * @throws NotFoundException if node not found in the graph.
     */
    NodeGraph getById(final Integer id) throws NotFoundException;

    /**
     * Get all graph nodes.
     * @return set of graph nodes.
     */
    Set<NodeGraph> getAll();

}

