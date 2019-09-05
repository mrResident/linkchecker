package ru.resprojects.linkchecker.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.resprojects.linkchecker.model.Node;

import java.util.List;

@Transactional(readOnly = true)
public interface NodeRepository extends JpaRepository<Node, Integer> {

    @Override
    @Transactional
    Node save(Node node);

    @Override
    @Transactional
    <S extends Node> List<S> saveAll(Iterable<S> entities);

    @Transactional
    int deleteById(int id);

    @Transactional
    int deleteByName(String name);

    @Transactional
    void deleteAll();

    Node getByName(String name);

    @Override
    List<Node> findAll();


}
