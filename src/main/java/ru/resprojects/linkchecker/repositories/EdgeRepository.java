package ru.resprojects.linkchecker.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.resprojects.linkchecker.model.Edge;
import ru.resprojects.linkchecker.model.Node;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface EdgeRepository extends JpaRepository<Edge, Integer> {

    @Transactional
    Edge save(Edge edge);

    @Transactional
    <S extends Edge> List<S> saveAll(Iterable<S> entities);

    @Transactional
    void deleteById(int id);

    @Transactional
    void deleteByNodeOneOrNodeTwo(Node nodeOne, Node nodeTwo);

    @Transactional
    void deleteByNodeOneAndNodeTwo(Node nodeOne, Node nodeTwo);

    @Transactional
    void deleteAllInBatch();

    Optional<Edge> findEdgeByNodeOneAndNodeTwo(Node nodeOne, Node nodeTwo);

    List<Edge> findEdgesByNodeOneOrNodeTwo(Node nodeOne, Node nodeTwo);

}

