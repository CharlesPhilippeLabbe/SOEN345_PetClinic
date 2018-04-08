package org.springframework.samples.petclinic.owner;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class OwnerHashCheckerTest {

    private Owner george;
    private String georgeHashed = "A1E0C1E1C4C6EA7513295C43635E4581";
    OwnerHashChecker checker;

    @Before
    public void setup(){
        george = new Owner();
        george.setId(1);
        george.setFirstName("George");
        george.setLastName("Franklin");
        george.setAddress("110 W. Liberty St.");
        george.setCity("Madison");
        george.setTelephone("6085551023");

        checker = new OwnerHashChecker();
    }



    @Test
    public void check() {
        checker.update(george);
        assertTrue(checker.check(george));
        assertEquals(0, checker.getInconsistencies());

        george.setLastName("franklin");
        assertFalse(checker.check(george));
        assertEquals(1, checker.getInconsistencies());

        checker.update(george);
        checker.update(george);assertTrue(checker.check(george));

        assertEquals(3, checker.getNumberOfChecks());
    }


}
