package ru.resprojects.linkchecker.web.rest;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import ru.resprojects.linkchecker.services.GraphService;

import javax.validation.Valid;
import java.net.URI;
import java.util.Collection;
import java.util.Set;

import static ru.resprojects.linkchecker.dto.GraphDto.EdgeGraph;
import static ru.resprojects.linkchecker.web.rest.GraphRestController.REST_URL;

/**
 * REST controller for work with edges of the data graph
 */
@Validated
@RestController
@RequestMapping(value = GraphEdgeRestController.EDGE_REST_URL,
    produces = MediaType.APPLICATION_JSON_VALUE)
public class GraphEdgeRestController {

    static final String EDGE_REST_URL = REST_URL + "/edges";

    private GraphService graphService;

    @Autowired
    public GraphEdgeRestController(final GraphService graphService) {
        this.graphService = graphService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EdgeGraph> create(@RequestBody @Valid EdgeGraph edge) {
        EdgeGraph created = this.graphService.getEdges().create(edge);
        URI uri = MvcUriComponentsBuilder.fromController(getClass())
            .path(EDGE_REST_URL)
            .buildAndExpand()
            .toUri();
        return ResponseEntity.created(uri).body(created);
    }

    //How to validate data in collections https://stackoverflow.com/a/54394177
    @PostMapping(value = "/byBatch", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<EdgeGraph>> createByBatch(@RequestBody @Valid Set<EdgeGraph> edges) {
        Set<EdgeGraph> created = this.graphService.getEdges().create(edges);
        URI uri = MvcUriComponentsBuilder.fromController(getClass())
            .path(EDGE_REST_URL)
            .buildAndExpand()
            .toUri();
        return ResponseEntity.created(uri).body(created);
    }

    @GetMapping
    public ResponseEntity<Collection<EdgeGraph>> get() {
        return ResponseEntity.ok(this.graphService.getEdges().getAll());
    }

    @GetMapping(value = "/byId/{id}")
    public ResponseEntity<EdgeGraph> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(graphService.getEdges().getById(id));
    }

    @GetMapping(value = "/byName/{name}")
    public ResponseEntity<Collection<EdgeGraph>> getByName(@PathVariable String name) {
        return ResponseEntity.ok(graphService.getEdges().get(name));
    }

    @GetMapping(value = "/byName")
    @ResponseBody
    public ResponseEntity<EdgeGraph> getByNames(@RequestParam("nodeOne") String nameNodeOne, @RequestParam("nodeTwo") String nameNodeTwo) {
        return ResponseEntity.ok(graphService.getEdges().get(nameNodeOne, nameNodeTwo));
    }

    @DeleteMapping
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void delete() {
        graphService.getEdges().deleteAll();
    }

    @DeleteMapping(value = "/byId/{id}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable Integer id) {
        graphService.getEdges().delete(id);
    }

    @DeleteMapping(value = "/byName/{name}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteByName(@PathVariable String name) {
        graphService.getEdges().delete(name);
    }

    @DeleteMapping(value = "/byName")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteByNames(@RequestParam("nodeOne") String nodeOne, @RequestParam("nodeTwo") String nodeTwo) {
        graphService.getEdges().delete(nodeOne, nodeTwo);
    }
}
