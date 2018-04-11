package org.springframework.samples.petclinic.visit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.sql.Date;

import org.junit.Before;
import org.junit.Test;
import org.springframework.samples.petclinic.model.ViolationRepository;

public class VisitHashCheckerTest {
    private Visit visit1;
    // this is the incorrect hash, didnt have time to complete this test class
    private String visit1Hashed = "A1E0C1E1C4C6EA7513295C43635E4581";
    private VisitHashChecker checker;
    private ViolationRepository violations;

    @Before
    public void setup(){
        visit1 = new Visit();
        visit1.setId(1);
        visit1.setPetId(7);
        visit1.setDate(new Date(2010, 03, 04));
        visit1.setDescription("rabies shot");

        violations = mock(ViolationRepository.class);
        checker = new VisitHashChecker(violations);
    }



    @Test
    public void check() {
        checker.update(visit1);
        assertTrue(checker.check(visit1));
        assertEquals(0, checker.getReadInconsistencies());

        visit1.setPetId(8);
        assertFalse(checker.check(visit1));
        assertEquals(1, checker.getReadInconsistencies());
        verify(violations).add(this.visit1Hashed,checker.getChecksum(visit1));

        checker.update(visit1);
        checker.update(visit1);assertTrue(checker.check(visit1));

        assertEquals(1.0/3, checker.getInconsistencyRatio(), 0.001);
    }
}
