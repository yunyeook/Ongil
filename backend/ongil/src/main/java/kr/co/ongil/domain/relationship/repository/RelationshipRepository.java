package kr.co.ongil.domain.relationship.repository;

import kr.co.ongil.domain.relationship.entity.Relationship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RelationshipRepository extends JpaRepository<Relationship,Integer> {

}