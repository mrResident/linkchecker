package ru.resprojects.linkchecker.services;

import ru.resprojects.linkchecker.util.exeptions.NotFoundException;
import ru.resprojects.linkchecker.dto.GraphDto;

import java.util.Set;

import static ru.resprojects.linkchecker.dto.GraphDto.NodeGraph;

/**
 * GraphNodeService - the interface for work with graph nodes.
 */
public interface GraphNodeService {

    /**
     * Create node of the graph.
     * @param node graph node {@link GraphDto.NodeGraph}
     * @return added graph node.
     */
    NodeGraph createNode(NodeGraph node);

    /**
     * Update node of the graph.
     * @param node  graph node {@link GraphDto.NodeGraph}
     */
    void updateNode(NodeGraph node);

    /**
     * Search graph node by id and delete from graph.
     * @param id of node of the graph.
     * @throws NotFoundException if node not found int the graph.
     */
    void deleteNode(Integer id) throws NotFoundException;

    /**
     * Search graph node by unique name and delete from graph.
     * @param name unique name of graph node.
     * @throws NotFoundException if node not found int the graph.
     */
    void deleteNode(String name) throws NotFoundException;

    /**
     * Search graph node by object {@link GraphDto.NodeGraph} and delete from graph.
     * @param nodeGraph object {@link GraphDto.NodeGraph}.
     * @throws NotFoundException if node not found int the graph.
     */
    void deleteNode(NodeGraph nodeGraph) throws NotFoundException;

    /**
     * Search and return graph node by unique name.
     * @param name unique name of graph node.
     * @return node of the graph.
     * @throws NotFoundException if node not found in the graph.
     */
    NodeGraph getNode(String name) throws NotFoundException;

    /**
     * Search and return graph node by id.
     * @param id of node of the graph.
     * @return node of the graph.
     * @throws NotFoundException if node not found in the graph.
     */
    NodeGraph getNodeById(Integer id) throws NotFoundException;

    /**
     * Get all graph nodes.
     * @return set of graph nodes.
     */
    Set<NodeGraph> getAllNodes();

}

