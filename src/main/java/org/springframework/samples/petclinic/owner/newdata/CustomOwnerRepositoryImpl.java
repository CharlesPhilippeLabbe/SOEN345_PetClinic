package org.springframework.samples.petclinic.owner.newdata;

import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.Pet;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.Collection;

public class CustomOwnerRepositoryImpl implements CustomOwnerRepository{

    @PersistenceContext
    EntityManager entityManager;


    /**
     * @Query("SELECT DISTINCT owner FROM new_owner left join fetch owner.pets WHERE owner.lastName LIKE :lastName%")
     * @param lastName
     * @return
     */
    @Transactional
    @Override
    public Collection<Owner> findByLastName(String lastName) {
        Query query = entityManager.createNativeQuery("SELECT DISTINCT id FROM new_owners WHERE last_name LIKE ?");
        query.setParameter(1, lastName);
        return query.getResultList();
    }

    /**
     * @Query("SELECT owner FROM new_owner left join fetch owner.pets WHERE owner.id =:id")
     * @param id
     * @return
     */
    @Transactional
    @Override
    public Owner findById(Integer id) {
        Query query = entityManager.createNativeQuery("SELECT 1 FROM new_owners  WHERE id = :id");
        query.setParameter("id", id);
        Owner owner = (Owner)query.getSingleResult();

        query = entityManager.createNativeQuery("SELECT * FROM new_pets WHERE owner = ?");
        query.setParameter(1, id);
        Collection<Pet> pets = query.getResultList();
        for(Pet pet : pets){
            owner.addPet(pet);
        }
        return owner;
    }

    /**
     * INSERT IGNORE INTO owners VALUES (1, 'George', 'Franklin', '110 W. Liberty St.', 'Madison', '6085551023')
     * @param owner
     */
    @Transactional
    @Override
    public void save(Owner owner) {


        Query query = entityManager.createNativeQuery("INSERT INTO new_owners(id, first_name, last_name, address, city, telephone)" +
            " VALUES(:id,:first_name,:last_name,:address,:city,:telephone)");
        query.setParameter("id", owner.getId());
        query.setParameter("first_name", owner.getFirstName());
        query.setParameter("last_name", owner.getLastName());
        query.setParameter("address", owner.getAddress());
        query.setParameter("city", owner.getCity());
        query.setParameter("telephone", owner.getTelephone());
        System.out.println("Executing custom query");
        query.executeUpdate();
        System.out.println("Executed custom query");

    }
}
