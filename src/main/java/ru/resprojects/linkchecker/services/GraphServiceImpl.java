package ru.resprojects.linkchecker.services;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.resprojects.linkchecker.AppProperties;
import ru.resprojects.linkchecker.dto.GraphDto;
import ru.resprojects.linkchecker.model.AbstractNamedEntity;
import ru.resprojects.linkchecker.model.Edge;
import ru.resprojects.linkchecker.model.Node;
import ru.resprojects.linkchecker.util.exeptions.ApplicationException;
import ru.resprojects.linkchecker.util.exeptions.ErrorPlaceType;
import ru.resprojects.linkchecker.util.exeptions.ErrorType;
import ru.resprojects.linkchecker.util.exeptions.NotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.resprojects.linkchecker.dto.GraphDto.NodeGraph;
import static ru.resprojects.linkchecker.dto.GraphDto.EdgeGraph;
import static ru.resprojects.linkchecker.util.GraphUtil.*;

@Service
public class GraphServiceImpl implements GraphService {

    private static Logger LOG = LoggerFactory.getLogger(GraphServiceImpl.class);

    private final GraphEdgeService edges;
    private final GraphNodeService nodes;
    private final AppProperties properties;

    @Autowired
    public GraphServiceImpl(final GraphEdgeService edges, final GraphNodeService nodes, final AppProperties properties) {
        this.edges = edges;
        this.nodes = nodes;
        this.properties = properties;
    }

    @Override
    public GraphDto create(final GraphDto graphTo) throws ApplicationException {
        LOG.debug("Starting create new graph.");
        if (Objects.isNull(graphTo)) {
            LOG.debug("Stopping creating a new graph because method received" +
                " null argument");
            throw new ApplicationException(
                ErrorType.DATA_ERROR,
                ErrorPlaceType.GRAPH,
                HttpStatus.UNPROCESSABLE_ENTITY,
                properties.getAppMsg().get("MSG_ARGUMENT_NULL")
            );
        }
        if (graphTo.getNodes().isEmpty() && !graphTo.getEdges().isEmpty()) {
            LOG.debug("Stopping creating a new graph because the collection of" +
                " nodes is empty, while the collection of edges is not empty");
            throw new ApplicationException(
                ErrorType.DATA_ERROR,
                ErrorPlaceType.GRAPH,
                HttpStatus.UNPROCESSABLE_ENTITY,
                "NODES: " + properties.getAppMsg().get("MSG_COLLECTION_EMPTY")
            );
        }
        LOG.debug("Removing old graph from DB");
        clear();
        LOG.debug("Checking graph for cycles. If cycles is found, they will be removed.");
        GraphDto graph = graphToGraphDto(
            removeCyclesFromGraph(graphBuilder(graphTo.getNodes(), graphTo.getEdges())));
        LOG.debug("Saving graph to the DB");
        Set<NodeGraph> nodeGraphSet = nodes.create(graph.getNodes());
        Set<EdgeGraph> edgeGraphSet = edges.create(graph.getEdges());
        return new GraphDto(nodeGraphSet, edgeGraphSet);
    }

    @Override
    public GraphDto get() {
        return removeGraphCycles(new GraphDto(nodes.getAll(), edges.getAll()));
    }

    @Override
    public void clear() {
        LOG.debug("Removing all nodes and edges from the graph");
        nodes.deleteAll();
    }

    @Override
    public String checkRoute(final Set<String> nodeNameSet) throws NotFoundException {
        LOG.debug("Starting check route");
        if (Objects.isNull(nodeNameSet)) {
            LOG.debug("Stopping check route because method received" +
                " null argument");
            throw new ApplicationException(
                ErrorType.DATA_ERROR,
                ErrorPlaceType.GRAPH,
                HttpStatus.UNPROCESSABLE_ENTITY,
                properties.getAppMsg().get("MSG_ARGUMENT_NULL")
            );
        }
        if (nodeNameSet.isEmpty()) {
            LOG.debug("Stopping check route because the collection of" +
                " nodes is empty");
            throw new ApplicationException(
                ErrorType.DATA_ERROR,
                ErrorPlaceType.GRAPH,
                HttpStatus.UNPROCESSABLE_ENTITY,
                properties.getAppMsg().get("MSG_COLLECTION_EMPTY")
            );
        }
        if (nodeNameSet.size() == 1) {
            LOG.debug("Stopping check route because the collection of" +
                " nodes have only one element");
            throw new ApplicationException(
                ErrorType.DATA_ERROR,
                ErrorPlaceType.GRAPH,
                HttpStatus.UNPROCESSABLE_ENTITY,
                properties.getAppMsg().get("MSG_COLLECTION_CONTAIN_ONE_ELEMENT")
            );
        }
        LOG.debug("Checking graph for cycles. If cycles is found, they will be removed.");
        GraphDto graphDto = removeGraphCycles(new GraphDto(nodes.getAll(),
            edges.getAll()));
        Map<String, Boolean> faultNodes = getRandomNodeFault(graphDto.getNodes());
        List<String> nodeNameList = new ArrayList<>(nodeNameSet);
        Graph<Node, DefaultEdge> graph = graphBuilder(graphDto.getNodes(),
            graphDto.getEdges());
        DijkstraShortestPath<Node, DefaultEdge> dAlg = new DijkstraShortestPath<>(graph);
        Node firstNode = nodeGraphToNode(graphDto.getNodes().stream()
            .filter(ng -> ng.getName().equalsIgnoreCase(nodeNameList.get(0)))
            .findFirst()
            .orElse(null));
        if (Objects.isNull(firstNode)) {
            throw new NotFoundException(
                String.format(properties.getNodeMsg().get("NODE_MSG_BY_NAME_ERROR"), nodeNameList.get(0)),
                ErrorPlaceType.GRAPH
            );
        }
        ShortestPathAlgorithm.SingleSourcePaths<Node, DefaultEdge> paths = dAlg.getPaths(firstNode);
        if (faultNodes.getOrDefault(firstNode.getName(), false)) {
            throw new NotFoundException(
                String.format(properties.getNodeMsg().get("NODE_MSG_IS_FAULT"), firstNode.getName()),
                ErrorPlaceType.GRAPH
            );
        }
        nodeNameList.stream().skip(1).forEach(name -> {
            Node nextNode = nodeGraphToNode(graphDto.getNodes().stream()
                .filter(ng -> ng.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null));
            if (Objects.isNull(nextNode)) {
                throw new NotFoundException(
                    String.format(properties.getNodeMsg().get("NODE_MSG_BY_NAME_ERROR"), name),
                    ErrorPlaceType.GRAPH
                );
            }
            if (faultNodes.getOrDefault(nextNode.getName(), false)) {
                throw new NotFoundException(
                    String.format(properties.getNodeMsg().get("NODE_MSG_IS_FAULT"), name),
                    ErrorPlaceType.GRAPH
                );
            }
            List<Node> findNodes = paths.getPath(nextNode).getVertexList();
            List<String> findNodesName = findNodes.stream()
                .map(AbstractNamedEntity::getName)
                .collect(Collectors.toList());
            if (!nodeNameList.containsAll(findNodesName)) {
                throw new NotFoundException(
                    String.format(properties.getNodeMsg().get("NODE_MSG_NOT_REACHABLE"), nodeNameList.get(0), name),
                    ErrorPlaceType.GRAPH
                );
            }
        });
        nodeNameList.forEach(name ->
            graphDto.getNodes().stream()
                .filter(ng -> ng.getName().equalsIgnoreCase(name))
                .findFirst()
                .ifPresent(ng -> {
                    ng.setCounter(ng.getCounter() + 1);
                    nodes.update(ng);
                }));
        return String.format("Route for nodes %s is found", nodeNameList.toString());
    }

    private GraphDto removeGraphCycles(final GraphDto graph) {
        if (Objects.isNull(graph) || graph.getEdges().isEmpty()) {
            return graph;
        }
        GraphDto optimizedGraph = graphToGraphDto(
            removeCyclesFromGraph(graphBuilder(graph.getNodes(), graph.getEdges())));
        //Checking, was removed edges or not from graph after optimizing
        if (graph.getEdges().size() == optimizedGraph.getEdges().size()) {
            return graph;
        }
        //Because ID's lost's in optimized graph, we need recover them.
        Set<EdgeGraph> optimizedEdges = graph.getEdges().stream()
            .filter(e -> optimizedGraph.getEdges().stream()
                .anyMatch(eg -> eg.getNodeOne().equalsIgnoreCase(e.getNodeOne())
                    && eg.getNodeTwo().equalsIgnoreCase(e.getNodeTwo())))
            .collect(Collectors.toSet());
        //Rewrite edge collection in optimized graph.
        optimizedGraph.setEdges(optimizedEdges);
        //Search all edges that was removed from graph and remove them from DB.
        Set<EdgeGraph> removedEdgesGraph = graph.getEdges().stream()
            .filter(eg -> !optimizedGraph.getEdges().contains(eg))
            .collect(Collectors.toSet());
        Set<Edge> removedEdges = getEdgesFromGraphDto(new GraphDto(
            optimizedGraph.getNodes(),
            removedEdgesGraph));
        if (!removedEdges.isEmpty()) {
            edges.delete(removedEdges);
            return optimizedGraph;
        }
        return graph;
    }

    public GraphEdgeService getEdges() {
        return edges;
    }

    public GraphNodeService getNodes() {
        return nodes;
    }
}
