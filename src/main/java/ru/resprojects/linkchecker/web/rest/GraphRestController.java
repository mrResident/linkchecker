package ru.resprojects.linkchecker.web.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import ru.resprojects.linkchecker.dto.GraphDto;
import ru.resprojects.linkchecker.services.GraphService;

import javax.validation.Valid;
import java.net.URI;
import java.util.Set;

/**
 * REST controller for work with data graph in generally
 */
@Validated
@RestController
@RequestMapping(value = GraphRestController.REST_URL, produces = MediaType.APPLICATION_JSON_VALUE)
public class GraphRestController {

    private static final Logger LOG = LoggerFactory.getLogger(GraphRestController.class);

    public static final String REST_URL = "/rest/v1/graph";

    private GraphService graphService;

    @Autowired
    public GraphRestController(final GraphService graphService) {
        this.graphService = graphService;
    }

    @RequestMapping(method = RequestMethod.OPTIONS)
    public ResponseEntity<?> options() {
        return ResponseEntity
            .ok()
            .allow(HttpMethod.GET, HttpMethod.POST, HttpMethod.DELETE, HttpMethod.OPTIONS)
            .build();
    }

    @GetMapping
    public ResponseEntity<GraphDto> get() {
        LOG.info("Getting graph");
        return ResponseEntity.ok(this.graphService.get());
    }

    @GetMapping(value = "/export", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> exportToGraphViz() {
        LOG.info("Export graph to GraphViz format");
        return ResponseEntity.ok(this.graphService.exportToGraphViz());
    }

    @PostMapping(value = "/checkroute", produces = MediaType.TEXT_HTML_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> checkRoute(@RequestBody Set<String> nodeNames) {
        LOG.info("Checking route for nodes " + nodeNames.toString());
        return ResponseEntity.ok(this.graphService.checkRoute(nodeNames));
    }

    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GraphDto> create(@RequestBody @Valid GraphDto graph) {
        LOG.info("Creating new graph");
        GraphDto created = graphService.create(graph);
        URI uri = MvcUriComponentsBuilder.fromController(getClass())
            .path(REST_URL)
            .buildAndExpand()
            .toUri();
        return ResponseEntity.created(uri).body(created);
    }

    @DeleteMapping
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void delete() {
        LOG.info("Removing graph");
        graphService.clear();
    }

}
