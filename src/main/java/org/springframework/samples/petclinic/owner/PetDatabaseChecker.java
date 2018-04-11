package org.springframework.samples.petclinic.owner;

import java.util.Collection;

import org.springframework.samples.petclinic.model.DatabaseChecker;

public class PetDatabaseChecker extends DatabaseChecker<Pet>{

	private NewPetRepository newPets;
	
	public PetDatabaseChecker(NewPetRepository newDB) {
		this.newPets = newDB;
	}

	@Override
	public boolean check(int id, Pet expected) {
		if(PetToggles.newDB && PetToggles.oldDB && PetToggles.forklifted) {
            Pet actual = this.newPets.findById(id);
            return this.check(actual, expected);
        }
        return false;
	}

	@Override
	public boolean check(Pet expected) {
		return check(expected.getId(), expected);
	}

	@Override
	public int check(String name, Collection<Pet> expected) {
		if(PetToggles.newDB && PetToggles.oldDB && PetToggles.forklifted) {
			return this.check(this.newPets.findByName(name), expected);
        }
        return 0;
	}

	@Override
	public int check(Collection<Pet> results) {
		return this.check("", results);
	}

	@Override
	public void update(Pet expected) {
		if(!PetToggles.testing){
            this.newPets.save(expected);
        }
	}
	
}
