package ru.resprojects.linkchecker.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ru.resprojects.linkchecker.model.Edge;
import ru.resprojects.linkchecker.model.Node;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface EdgeRepository extends JpaRepository<Edge, Integer> {

    @Override
    @Transactional
    Edge save(Edge edge);

    @Override
    @Transactional
    <S extends Edge> List<S> saveAll(Iterable<S> entities);

    /**
     * Delete entity from database uses id.
     * @param id is the identifier of specific entity (Edge).
     * @return number of deleted entities.
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM Edge e WHERE e.id=:id")
    int delete(@Param("id") int id);

    //ToDo need jdoc comment.
    @Transactional
    int deleteByNodeOneOrNodeTwo(Node nodeOne, Node nodeTwo);

    //ToDo need jdoc comment.
    @Transactional
    int deleteByNodeOneAndNodeTwo(Node nodeOne, Node nodeTwo);

    /**
     * Delete all entities from database.
     */
    @Transactional
    void deleteAll();

    @Override
    List<Edge> findAll();

    /**
     * Get specific edge of the graph. For find specific edge of the graph uses
     * identifier of specific entity (Edge) in database.
     * @param id identifier of specific entity (Edge) in database.
     * @return specific edge of the graph.
     */
    Optional<Edge> findEdgeById(int id);

    /**
     * Get specific edge of the graph. For find specific edge of the graph uses
     * two nodes.
     * @param nodeOne first node of the specific graph edge.
     * @param nodeTwo second node of the specific graph edge.
     * @return specific graph edge.
     */
    Optional<Edge> findEdgeByNodeOneAndNodeTwo(Node nodeOne, Node nodeTwo);

    /**
     * Get all possible edges of the graph. For find uses input nodes of the
     * graph. Possibly find all edges of the graph uses only nodeOne, or nodeTwo
     * or both nodeOne and nodeTwo.
     * @param nodeOne first node of the specific graph edge.
     * @param nodeTwo second node of the specific graph edge.
     * @return all possible edges of the graph.
     */
    List<Edge> findEdgesByNodeOneOrNodeTwo(Node nodeOne, Node nodeTwo);

}

