package ru.resprojects.linkchecker.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import ru.resprojects.linkchecker.services.GraphService;

import javax.validation.Valid;
import java.net.URI;
import java.util.Collection;
import java.util.Set;

import static ru.resprojects.linkchecker.dto.GraphDto.NodeGraph;
import static ru.resprojects.linkchecker.web.GraphRestController.REST_URL;

/**
 * REST controller for work with nodes of the data graph
 */
@Validated
@RestController
@RequestMapping(value = GraphNodeRestController.NODES_REST_URL,
    produces = MediaType.APPLICATION_JSON_VALUE)
public class GraphNodeRestController {

    private static final Logger LOG = LoggerFactory.getLogger(GraphNodeRestController.class);

    static final String NODES_REST_URL = REST_URL + "/nodes";

    private GraphService graphService;

    @Autowired
    public GraphNodeRestController(final GraphService graphService) {
        this.graphService = graphService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<NodeGraph> create(@Valid @RequestBody NodeGraph node) {
        NodeGraph created = this.graphService.getNodes().create(node);
        URI uri = MvcUriComponentsBuilder.fromController(getClass())
            .path(NODES_REST_URL)
            .buildAndExpand()
            .toUri();
        return ResponseEntity.created(uri).body(created);
    }

    @PostMapping(value = "/byBatch", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<NodeGraph>> createByBatch(@RequestBody @Valid Set<NodeGraph> nodes) {
        Set<NodeGraph> created = this.graphService.getNodes().create(nodes);
        URI uri = MvcUriComponentsBuilder.fromController(getClass())
            .path(NODES_REST_URL)
            .buildAndExpand()
            .toUri();
        return ResponseEntity.created(uri).body(created);
    }

    @GetMapping
    public ResponseEntity<Collection<NodeGraph>> get() {
        return ResponseEntity.ok(this.graphService.getNodes().getAll());
    }

    @GetMapping(value = "/byId/{id}")
    public ResponseEntity<NodeGraph> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(graphService.getNodes().getById(id));
    }

    @GetMapping(value = "/byName/{name}")
    public ResponseEntity<NodeGraph> getByName(@PathVariable String name) {
        return ResponseEntity.ok(graphService.getNodes().get(name));
    }

    @DeleteMapping(value = "/byId/{id}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable Integer id) {
        graphService.getNodes().delete(id);
    }

    @DeleteMapping(value = "/byName/{name}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteByName(@PathVariable String name) {
        graphService.getNodes().delete(name);
    }

    @DeleteMapping(value = "/byObj")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteByObject(@RequestBody NodeGraph obj) {
        graphService.getNodes().delete(obj);
    }

    @DeleteMapping
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void delete() {
        graphService.getNodes().deleteAll();
    }
}
