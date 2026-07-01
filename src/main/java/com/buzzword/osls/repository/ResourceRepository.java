package com.buzzword.osls.repository;

import com.buzzword.osls.model.Resource;
import com.buzzword.osls.model.enums.ResourceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {

    List<Resource> findByCategory(ResourceCategory category);

    @Query("SELECT r FROM Resource r WHERE " +
           "LOWER(r.title) LIKE LOWER(CONCAT('%',:query,'%')) OR " +
           "LOWER(r.description) LIKE LOWER(CONCAT('%',:query,'%')) OR " +
           "LOWER(r.addedBy.username) LIKE LOWER(CONCAT('%',:query,'%'))")
    List<Resource> searchResources(@Param("query") String query);

    @Query("SELECT r FROM Resource r WHERE " +
           "(LOWER(r.title) LIKE LOWER(CONCAT('%',:query,'%')) OR " +
           "LOWER(r.description) LIKE LOWER(CONCAT('%',:query,'%')) OR " +
           "LOWER(r.addedBy.username) LIKE LOWER(CONCAT('%',:query,'%'))) " +
           "AND r.category=:category")
    List<Resource> searchResourcesByQueryAndCategory(
        @Param("query") String query,
        @Param("category") ResourceCategory category
    );

    List<Resource> findByAddedById(Long userId);
}
