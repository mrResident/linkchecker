package ru.resprojects.linkchecker.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.resprojects.linkchecker.model.Node;

import java.util.List;

@Transactional(readOnly = true)
public interface NodeRepository extends JpaRepository<Node, Integer> {

    @Transactional
    <S extends Node> S save(S node);

    @Transactional
    <S extends Node> List<S> saveAll(Iterable<S> entities);

    @Transactional
    void deleteById(Integer id);

    @Transactional
    void deleteByName(String name);

    @Transactional
    void deleteAllInBatch();

    Node getByName(String name);

    boolean existsByName(String name);
}
