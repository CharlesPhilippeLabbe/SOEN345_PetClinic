package org.springframework.samples.petclinic.visit;

import java.util.Collection;

import org.springframework.samples.petclinic.model.DatabaseChecker;
import org.springframework.samples.petclinic.owner.VisitToggles;

public class VisitDatabaseChecker extends DatabaseChecker<Visit> {

    private NewVisitRepository newVisits;

    public VisitDatabaseChecker(NewVisitRepository newDB){
        this.newVisits = newDB;
    }

	@Override
	public boolean check(int id, Visit expected) {
		if (VisitToggles.newDB && VisitToggles.oldDB && VisitToggles.forklifted) {
			Visit actual = this.newVisits.getVisit(id);
			return this.check(actual, expected);
		}
		return false;
	}

	@Override
	public boolean check(Visit expected) {
		return check(expected.getId(), expected);
	}

	@Override
	public int check(String name, Collection<Visit> expected) {
		// Not applicable
		return 0;
	}


	@Override
	public int check(Collection<Visit> results) {
		if (VisitToggles.newDB && VisitToggles.oldDB && VisitToggles.forklifted) {
			return this.check(this.newVisits.getAllVisits(), results);
		}
		return 0;
	}

	@Override
	public void update(Visit expected) {
        if(!VisitToggles.testing){
            this.newVisits.save(expected);
        }
	}
}
