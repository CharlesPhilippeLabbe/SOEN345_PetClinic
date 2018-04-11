package org.springframework.samples.petclinic.owner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

public class PetDatabaseCheckerTest {

	private static final int TEST_PET_ID = 1;
	private static final int TEST_OWNER_ID = 1;
	private static final int TEST_PET_TYPE_ID = 1;

    private PetRepository pets;

    private NewPetRepository newPets;

    private Pet pet1;
    private Pet pet2;
    private PetType petType;
    private Owner owner;
    
    private Date date = new Date(2010, 10, 10);

    private PetDatabaseChecker checker;

    @Before
    public void setup() {
    		petType = new PetType();
    		petType.setId(TEST_PET_TYPE_ID);
    		
    		owner = new Owner();
    		owner.setId(TEST_OWNER_ID);
    		owner.setFirstName("George");
    		owner.setLastName("Franklin");
    		owner.setAddress("110 W. Liberty St.");
    		owner.setCity("Madison");
    		owner.setTelephone("6085551023");
    		
    		pet1 = new Pet();
    		pet1.setId(TEST_PET_ID);
    		pet1.setName("Tommy");
    		pet1.setBirthDate(date);
    		pet1.setType(petType);
    		pet1.setOwner(owner);

    		pet2 = new Pet();
    		pet2.setId(TEST_PET_ID);
    		pet2.setName("Tommy");
    		pet2.setBirthDate(date);
    		pet2.setType(petType);
    		pet2.setOwner(owner);

        PetToggles.newDB = true;
        PetToggles.oldDB = true;
        PetToggles.forklifted = true;
        PetToggles.hashChecker = false;

        pets = mock(PetRepository.class);
        newPets = mock(NewPetRepository.class);

        when(this.pets.findById(TEST_PET_ID)).thenReturn(pet1);

        checker = new PetDatabaseChecker(newPets);
    }

    @Test
    public void checkById() {
        when(newPets.findById(TEST_PET_ID)).thenReturn(pet1);

        assertTrue(checker.check(TEST_PET_ID, pets.findById(TEST_PET_ID)));

        assertEquals(0, checker.getReadInconsistencies());

        assertFalse(checker.check(TEST_PET_ID, new Pet()));

        assertEquals(1, checker.getReadInconsistencies());



        when(newPets.findById(TEST_PET_ID)).thenReturn(pet2);

        assertFalse(checker.check(TEST_PET_ID, pets.findById(TEST_PET_ID)));

        verify(newPets).save(pet1);

        assertEquals(2.0/3, checker.getInconsistencyRatio(), 0.001);

    }

    @Test
    public void checkByPet() {
        when(newPets.findById(TEST_PET_ID)).thenReturn(pet1);
        assertTrue(checker.check(pets.findById(TEST_PET_ID)));

        assertFalse(checker.check(pet2));

        verify(newPets).save(pet2);
    }


    @Test
    public void checkByResults() {
        Collection<Pet> newResults = new ArrayList<>();
        newResults.add(pet1);
        Collection<Pet> results = new ArrayList<>();
        results.add(pet2);

        when(newPets.findByName("")).thenReturn(newResults);

        assertEquals(0, checker.check(newResults));

        assertEquals(1, checker.check(results));

        verify(newPets).save(pet2);

        assertEquals(1, checker.getReadInconsistencies());
    }

}
