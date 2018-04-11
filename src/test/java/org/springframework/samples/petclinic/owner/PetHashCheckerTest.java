package org.springframework.samples.petclinic.owner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.springframework.samples.petclinic.model.ViolationRepository;

public class PetHashCheckerTest {
	private Pet pet1;
    private String pet1Hashed = "G5W6C2T7C1Y6SB5N71738D62719H6247";
    private PetHashChecker checker;
    private ViolationRepository violations;
    
    private Owner george;
    private PetType petType; 

    @Before
    public void setup(){
        george = new Owner();
        george.setId(1);
        george.setFirstName("George");
        george.setLastName("Franklin");
        george.setAddress("110 W. Liberty St.");
        george.setCity("Madison");
        george.setTelephone("6085551023");
        
        petType = new PetType();
		petType.setId(1);
		
		pet1 = new Pet();
		pet1.setId(1);
		pet1.setName("Tommy");
		pet1.setBirthDate(new Date(2010, 10, 10));
		pet1.setType(petType);
		pet1.setOwner(george);

        violations = mock(ViolationRepository.class);
        checker = new PetHashChecker(violations);
    }


    @Test
    public void check() {
        checker.update(pet1);
        assertTrue(checker.check(pet1));
        assertEquals(0, checker.getReadInconsistencies());

        pet1.setName("tommy");
        assertFalse(checker.check(pet1));
        assertEquals(1, checker.getReadInconsistencies());
        verify(violations).add(this.pet1Hashed,checker.getChecksum(pet1));

        checker.update(pet1);
        checker.update(pet1);assertTrue(checker.check(pet1));

        assertEquals(1.0/3, checker.getInconsistencyRatio(), 0.001);
    }

    @Test
    public void checkByName(){
        Collection<Pet> results = new ArrayList<>();
        results.add(pet1);
        checker.update(pet1);
        assertEquals(0, checker.check("Tommy", results));
        assertEquals(0, checker.check("tommy", results));

        pet1.setName("Rex");
        checker.update(pet1);

        assertEquals(1, checker.check("Tommy", results));
    }
}
