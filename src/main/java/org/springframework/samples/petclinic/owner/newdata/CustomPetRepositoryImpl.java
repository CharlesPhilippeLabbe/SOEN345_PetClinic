package org.springframework.samples.petclinic.owner.newdata;

import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetType;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.QueryTimeoutException;
import javax.persistence.TransactionRequiredException;

import javax.transaction.Transactional;

import java.util.List;

public class CustomPetRepositoryImpl implements CustomPetRepository {

    @PersistenceContext
    EntityManager entityManager;

    /**
     * @Query("SELECT ptype FROM PetType ptype ORDER BY ptype.name")
     */
    @Transactional
    @Override
    public List<PetType> findPetTypes(){
        Query query = entityManager.createNativeQuery("SELECT * FROM new_types ORDER BY name", PetType.class);
        return query.getResultList();
    }

    @Transactional
    @Override
    public Pet findById(Integer id) {
        Query query = entityManager.createNativeQuery("SELECT * FROM new_pets WHERE id = ?", Pet.class);
        query.setParameter(1, id);
        return (Pet)query.getSingleResult();
    }

    @Transactional
    @Override
    public void save(Pet pet) {


        Query query = entityManager.createNativeQuery("INSERT IGNORE INTO new_pets VALUE ?");
        
        /**
         * Create an instance of <code>Query</code> for executing
         * a native SQL query.
         * @param sqlString a native SQL query string
         * @param resultClass the class of the resulting instance(s)
         * @return the new query instance
         */
        
        
        Query query = entityManager.createNativeQuery("INSERT INTO new_pets (id, name, birth_date, type_id, owner_id) " +
            "VALUES (:id, :name, :birth_date, :type_id, :owner_id) " +
            "ON DUPLICATE KEY UPDATE name = :name, birth_date = :birth_date, type_id = :type_id, owner_id = :owner_id");
        query.setParameter("id", pet.getId());
        query.setParameter("name", pet.getName());
        query.setParameter("birth_date", pet.getBirthDate());
        query.setParameter("type_id", pet.getType().getId());
        query.setParameter("owner_id", pet.getOwner().getId());

        query.executeUpdate();

    	/**
    	 * Execute an update or delete statement.
    	 *
    	 * @return the number of entities updated or deleted
    	 *
    	 * @throws IllegalStateException if called for a Java
    	 * Persistence query language SELECT statement or for
    	 * a criteria query
    	 * @throws TransactionRequiredException if there is
    	 * no transaction
    	 * @throws QueryTimeoutException if the statement execution
    	 * exceeds the query timeout value set and only
    	 * the statement is rolled back
    	 * @throws PersistenceException if the query execution exceeds
    	 * the query timeout value set and the transaction
    	 * is rolled back
    	 */
    }


}
