package org.springframework.samples.petclinic.owner.newdata;

import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetType;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

public class CustomPetRepositoryImpl implements CustomPetRepository {

    @PersistenceContext
    EntityManager entityManager;

    /**
     * @Query("SELECT ptype FROM PetType ptype ORDER BY ptype.name")
     */
    @Override
    public List<PetType> findPetTypes(){
        Query query = entityManager.createNativeQuery("SELECT * FROM new_types ORDER BY name");
        return query.getResultList();
    }

    @Override
    public Pet findById(Integer id) {
        Query query = entityManager.createNativeQuery("SELECT * FROM new_pets WHERE id = ?");
        query.setParameter(1, id);
        return (Pet)query.getSingleResult();
    }

    @Override
    public void save(Pet pet) {
/*
        Query query = entityManager.createNativeQuery("INSERT IGNORE INTO new_pets VALUE ?");
        query.setParameter(1, pet.getId());
        query.setParameter(1, pet.getName());
        query.setParameter(1, pet.getBirthDate());
        query.setParameter(1, pet.getType().getId());
        query.setParameter(1, pet.getOwner().getId());

        query.getSingleResult();*/

    }


}
