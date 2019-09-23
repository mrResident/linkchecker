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
    <S extends Edge> S save(S edge);

    @Transactional
    <S extends Edge> List<S> saveAll(Iterable<S> entities);

    @Transactional
    void deleteById(int id);

    @Transactional
    void deleteAllInBatch();

    @Transactional
    void deleteInBatch(Iterable<Edge> entities);

    Optional<Edge> findEdgeByNodeOneAndNodeTwo(Node nodeOne, Node nodeTwo);

    List<Edge> findEdgesByNodeOneOrNodeTwo(Node nodeOne, Node nodeTwo);

    boolean existsById(int id);

}

