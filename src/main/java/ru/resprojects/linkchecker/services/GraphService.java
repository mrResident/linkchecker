package ru.resprojects.linkchecker.services;

import ru.resprojects.linkchecker.dto.GraphDto;
import ru.resprojects.linkchecker.util.exeptions.ApplicationException;
import ru.resprojects.linkchecker.util.exeptions.NotFoundException;

import java.util.Set;

/**
 * GraphService - the interface for work with <a href = https://en.wikipedia.org/wiki/Graph_(discrete_mathematics)#Graph>undirected graph</a>.
 */
public interface GraphService {

    /**
     * Checking input graph data, removing cycles from input graph and saving it
     * to the DB.
     * @param graphTo graph {@link GraphDto}
     * @return graph.
     * @throws ApplicationException if found errors.
     */
    GraphDto create(final GraphDto graphTo) throws ApplicationException;

    /**
     * Get graph.
     * @return graph {@link GraphDto}
     */
    GraphDto get();

    /**
     * Remove graph.
     */
    void clear();

    /**
     * Checking route between nodes.
     * @param nodeNameSet collection of the unique nodes name.
     * @return check result in JSON format.
     * @throws NotFoundException if route is not found
     */
    String checkRoute(final Set<String> nodeNameSet) throws NotFoundException;

    /**
     * Exporting graph to <a href = https://www.graphviz.org/about/>graphviz</a> format.
     * @return string data in graphviz format.
     */
    String exportToGraphViz();

    /**
     * Get access to nodes of the graph.
     * @return {@link GraphNodeService}
     */
    GraphNodeService getNodes();

    /**
     * Get access to edges of the graph.
     * @return {@link GraphEdgeService}
     */
    GraphEdgeService getEdges();
}

