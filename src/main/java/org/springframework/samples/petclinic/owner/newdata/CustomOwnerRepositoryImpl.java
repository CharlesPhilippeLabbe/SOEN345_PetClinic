package org.springframework.samples.petclinic.owner.newdata;

import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.Pet;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Collection;

public class CustomOwnerRepositoryImpl implements CustomOwnerRepository{

    @PersistenceContext
    EntityManager entityManager;

    /**
     * @Query("SELECT DISTINCT owner FROM new_owner left join fetch owner.pets WHERE owner.lastName LIKE :lastName%")
     * @param lastName
     * @return
     */
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
    @Override
    public Owner findById(Integer id) {
        Query query = entityManager.createNativeQuery("SELECT 1 FROM new_owners  WHERE id = ?");
        query.setParameter(1, id);
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
    @Override
    public void save(Owner owner) {
        System.out.println("Executing custom query");
       /* Query query = entityManager.createNativeQuery("INSERT IGNORE INTO new_owners VALUES ?");
        query.setParameter(1, owner.getId());
        query.setParameter(2, owner.getFirstName());
        query.setParameter(3, owner.getLastName());
        query.setParameter(4, owner.getAddress());
        query.setParameter(5, owner.getCity());
        query.setParameter(6, owner.getTelephone());

        query.getSingleResult();*/
    }
}
