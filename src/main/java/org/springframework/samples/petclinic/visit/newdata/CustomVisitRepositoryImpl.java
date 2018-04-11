package org.springframework.samples.petclinic.visit.newdata;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;

import org.springframework.samples.petclinic.visit.Visit;

public class CustomVisitRepositoryImpl implements CustomVisitRepository {

	@PersistenceContext
	EntityManager entityManager;
	
	@Transactional
	@Override
	public void save(Visit visit) {
        Query query = entityManager.createNativeQuery("INSERT INTO new_visits(id, pet_id, visit_date, description)" +
                " VALUES(:id,:pet_id,:visit_date,:description)" +
                "ON DUPLICATE KEY UPDATE pet_id = :pet_id, visit_date =:visit_date, description = :description");
            query.setParameter("id", visit.getId());
            query.setParameter("pet_id", visit.getPetId());
            query.setParameter("visit_date", visit.getDate());
            query.setParameter("description", visit.getDescription());
            
            query.executeUpdate();
	}

	@Transactional
	@Override
	public List<Visit> findByPetId(Integer petId) {
        Query query = entityManager.createNativeQuery("SELECT * FROM new_visits WHERE pet_id = :pet_id", Visit.class);
        query.setParameter("pet_id", petId + "%");

        List<Visit> visits = query.getResultList();
        System.out.println(visits.size());
        return visits;
	}
}
