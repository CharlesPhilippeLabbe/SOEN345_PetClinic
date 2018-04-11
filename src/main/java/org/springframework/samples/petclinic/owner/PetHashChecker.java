package org.springframework.samples.petclinic.owner;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import org.springframework.samples.petclinic.model.HashConsistencyChecker;
import org.springframework.samples.petclinic.model.ViolationRepository;

public class PetHashChecker extends HashConsistencyChecker<Pet>{
	
	NewPetRepository repository;
	NewOwnerRepository newOwnerRepo;

	public PetHashChecker(NewPetRepository repo, NewOwnerRepository newOwnerRepo, ViolationRepository violations) {
		super(violations);
		this.repository = repo;
		this.newOwnerRepo = newOwnerRepo;
	}
	
	public PetHashChecker(ViolationRepository violations){
        super(violations);
    }

    public PetHashChecker(NewPetRepository repository, NewOwnerRepository newOwnerRepo, ViolationRepository violations, boolean initAsync){
        this(repository, newOwnerRepo, violations);

        if(initAsync){
            //populating hash first
            CompletableFuture.supplyAsync(this::populate)
            .thenAccept(this::initiateAsync);
        }
    }

	@Override
	public int check(String name, Collection<Pet> pets) {
		int intial = this.getReadInconsistencies();

        for(Pet pet: pets){
            //ignores empty search with "", this is handled in the continuous checker
            if (pet.getName().equals(name)){
                this.check(pet.getId(), pet);//inconsistency found
            }
        }

        return this.getReadInconsistencies() - intial;
	}
	
	private boolean populate(){
        return true;
    }
	
	public void setRepository(NewPetRepository repo, NewOwnerRepository newOwnerRepo){
        this.repository = repo;
        this.newOwnerRepo = newOwnerRepo;
    }

    protected void initiateAsync(NewPetRepository repo, NewOwnerRepository newOwnerRepo, boolean async){
        this.repository = repo;
        this.newOwnerRepo = newOwnerRepo;
        this.initiateAsync(async);
    }

    protected void initiateAsync(boolean async){

        if(this.repository == null){
            return;
        }
        if(async){
            CompletableFuture.supplyAsync(this::continuousChecker);
        }
        else{
            this.continuousChecker();
        }
    }

    private boolean continuousChecker(){
        while(PetToggles.hashChecker){
            Collection<Pet> pets = repository.findByName("");//getting all the pets

            for(Pet pet: pets){
                this.check(pet);//checking every instances of pet in database for inconsistencies
            }

            try {
                Thread.sleep(10*60000);//wait 10 mins until next check
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

}
