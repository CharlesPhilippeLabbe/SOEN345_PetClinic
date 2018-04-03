package org.springframework.samples.petclinic.owner.newdata;

import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetType;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
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

        Query query = entityManager.createNativeQuery("INSERT INTO new_pets (id, name, birth_date, type_id, owner_id) " +
            "VALUES (:id, :name, :birth_date, :type_id, :owner_id) " +
            "ON DUPLICATE KEY UPDATE name = :name, birth_date = :birth_date, type_id = :type_id, owner_id = :owner_id");
        query.setParameter("id", pet.getId());
        query.setParameter("name", pet.getName());
        query.setParameter("birth_date", pet.getBirthDate());
        query.setParameter("type_id", pet.getType().getId());
        query.setParameter("owner_id", pet.getOwner().getId());

        query.executeUpdate();

    }


}
