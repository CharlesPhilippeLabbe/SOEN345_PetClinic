package org.springframework.samples.petclinic.visit.newdata;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.samples.petclinic.visit.Visit;

public interface CustomVisitRepository {
    
	void save(Visit visit) throws DataAccessException;

    List<Visit> findByPetId(Integer petId);
}
