package org.springframework.samples.petclinic.owner;
import static org.mockito.Mockito.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.samples.petclinic.model.ViolationRepository;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.*;

public class OwnerHashCheckerTest {

    private Owner george;
    private String georgeHashed = "A1E0C1E1C4C6EA7513295C43635E4581";
    private OwnerHashChecker checker;
    private ViolationRepository violations;

    @Before
    public void setup(){
        george = new Owner();
        george.setId(1);
        george.setFirstName("George");
        george.setLastName("Franklin");
        george.setAddress("110 W. Liberty St.");
        george.setCity("Madison");
        george.setTelephone("6085551023");

        violations = mock(ViolationRepository.class);
        checker = new OwnerHashChecker(violations);
    }



    @Test
    public void check() {
        checker.update(george);
        assertTrue(checker.check(george));
        assertEquals(0, checker.getReadInconsistencies());

        george.setLastName("franklin");
        assertFalse(checker.check(george));
        assertEquals(1, checker.getReadInconsistencies());
        verify(violations).add(this.georgeHashed,checker.getChecksum(george));

        checker.update(george);
        checker.update(george);assertTrue(checker.check(george));

        assertEquals(1.0/3, checker.getInconsistencyRatio(), 0.001);
    }

    @Test
    public void checkByLastName(){
        Collection<Owner> results = new ArrayList<>();
        results.add(george);
        checker.update(george);
        assertEquals(0, checker.check("Franklin", results));
        assertEquals(0, checker.check("franklin", results));

        george.setFirstName("Paul");
        checker.update(george);

        assertEquals(1, checker.check("Franklin", results));
    }

}
