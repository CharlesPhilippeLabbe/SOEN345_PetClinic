package org.springframework.samples.petclinic.visit;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.data.repository.Repository;
import org.springframework.samples.petclinic.model.BaseEntity;
import org.springframework.samples.petclinic.visit.newdata.CustomVisitRepository;

public interface NewVisitRepository extends  Repository<Visit, Integer>, CustomVisitRepository {

	/**
     * Save a <code>Visit</code> to the data store, either inserting or updating it.
     *
     * @param visit the <code>Visit</code> to save
     * @see BaseEntity#isNew
     */
    void save(Visit visit) throws DataAccessException;

    List<Visit> findByPetId(Integer petId);

}
