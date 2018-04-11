package org.springframework.samples.petclinic.visit;

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
import org.springframework.samples.petclinic.visit.NewVisitRepository;
import org.springframework.samples.petclinic.visit.Visit;
import org.springframework.samples.petclinic.visit.VisitDatabaseChecker;
import org.springframework.samples.petclinic.visit.VisitRepository;
import org.springframework.samples.petclinic.owner.VisitToggles;

public class VisitDatabaseCheckerTest {
    private static final int TEST_VISIT_ID = 1;

    private VisitRepository visits;

    private NewVisitRepository newVisits;

    private Visit visit1;
    private Visit visit2;

    private VisitDatabaseChecker checker;

    @Before
    public void setup() {
        visit1 = new Visit();
        visit1.setId(TEST_VISIT_ID);
        visit1.setPetId(7);
        visit1.setDate(new Date(2010, 03, 04));
        visit1.setDescription("rabies shot");


        visit2 = new Visit();
        visit2.setId(TEST_VISIT_ID);
        visit2.setPetId(8);
        visit2.setDate(new Date(2010, 03, 04));
        visit2.setDescription("rabies shot");


        VisitToggles.newDB = true;
        VisitToggles.oldDB = true;
        VisitToggles.forklifted = true;
        VisitToggles.hashChecker = false;

        visits = mock(VisitRepository.class);
        newVisits = mock(NewVisitRepository.class);

        when(this.visits.getVisit(TEST_VISIT_ID)).thenReturn(visit1);

        checker = new VisitDatabaseChecker(newVisits);
    }

    @Test
    public void checkById() {
        when(newVisits.getVisit(TEST_VISIT_ID)).thenReturn(visit1);

        assertTrue(checker.check(TEST_VISIT_ID, visits.getVisit(TEST_VISIT_ID)));

        assertEquals(0, checker.getReadInconsistencies());

        assertFalse(checker.check(TEST_VISIT_ID, new Visit()));

        assertEquals(1, checker.getReadInconsistencies());



        when(newVisits.getVisit(TEST_VISIT_ID)).thenReturn(visit2);

        assertFalse(checker.check(TEST_VISIT_ID, visits.getVisit(TEST_VISIT_ID)));

        verify(newVisits).save(visit1);

        assertEquals(2.0/3, checker.getInconsistencyRatio(), 0.001);

    }

    @Test
    public void checkByVisit() {
        when(newVisits.getVisit(TEST_VISIT_ID)).thenReturn(visit1);
        assertTrue(checker.check(visits.getVisit(TEST_VISIT_ID)));

        assertFalse(checker.check(visit2));

        verify(newVisits).save(visit2);
    }


    @Test
    public void checkByResults() {
        Collection<Visit> newResults = new ArrayList<>();
        newResults.add(visit1);
        Collection<Visit> results = new ArrayList<>();
        results.add(visit2);

        when(newVisits.getAllVisits()).thenReturn(newResults);

        assertEquals(0, checker.check(newResults));

        assertEquals(1, checker.check(results));

        verify(newVisits).save(visit2);

        assertEquals(1, checker.getReadInconsistencies());
    }
}
