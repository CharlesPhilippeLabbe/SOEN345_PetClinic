package org.springframework.samples.petclinic.owner;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

public class OwnerDatabaseCheckerTest {

    private static final int TEST_OWNER_ID = 1;

    private OwnerRepository owners;

    private NewOwnerRepository newOwners;

    private Owner george;
    private Owner paul;

    private OwnerDatabaseChecker checker;

    @Before
    public void setup() {
        george = new Owner();
        george.setId(TEST_OWNER_ID);
        george.setFirstName("George");
        george.setLastName("Franklin");
        george.setAddress("110 W. Liberty St.");
        george.setCity("Madison");
        george.setTelephone("6085551023");


        paul = new Owner();
        paul.setId(TEST_OWNER_ID);
        paul.setFirstName("Paul");
        paul.setLastName("Franklin");
        paul.setAddress("110 W. Liberty St.");
        paul.setCity("Madison");
        paul.setTelephone("6085551023");


        OwnerToggles.newDB = true;
        OwnerToggles.oldDB = true;
        OwnerToggles.forklifted = true;
        OwnerToggles.hashChecker = false;

        owners = mock(OwnerRepository.class);
        newOwners = mock(NewOwnerRepository.class);

        when(this.owners.findById(TEST_OWNER_ID)).thenReturn(george);

        checker = new OwnerDatabaseChecker(newOwners);
    }

    @Test
    public void checkById() {
        when(newOwners.findById(TEST_OWNER_ID)).thenReturn(george);

        assertTrue(checker.check(TEST_OWNER_ID, owners.findById(TEST_OWNER_ID)));

        assertEquals(0, checker.getReadInconsistencies());

        assertFalse(checker.check(TEST_OWNER_ID, new Owner()));

        assertEquals(1, checker.getReadInconsistencies());



        when(newOwners.findById(TEST_OWNER_ID)).thenReturn(paul);

        assertFalse(checker.check(TEST_OWNER_ID, owners.findById(TEST_OWNER_ID)));

        verify(newOwners).save(george);

        assertEquals(2.0/3, checker.getInconsistencyRatio(), 0.001);

    }

    @Test
    public void checkByOwner() {
        when(newOwners.findById(TEST_OWNER_ID)).thenReturn(george);
        assertTrue(checker.check(owners.findById(TEST_OWNER_ID)));

        assertFalse(checker.check(paul));

        verify(newOwners).save(paul);
    }


    @Test
    public void checkByResults() {
        Collection<Owner> newResults = new ArrayList<>();
        newResults.add(george);
        Collection<Owner> results = new ArrayList<>();
        results.add(paul);

        when(newOwners.findByLastName("")).thenReturn(newResults);

        assertEquals(0, checker.check(newResults));

        assertEquals(1, checker.check(results));

        verify(newOwners).save(paul);

        assertEquals(1, checker.getReadInconsistencies());
    }
}
