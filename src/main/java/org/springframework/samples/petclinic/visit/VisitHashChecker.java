package org.springframework.samples.petclinic.visit;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import org.springframework.samples.petclinic.model.HashConsistencyChecker;
import org.springframework.samples.petclinic.model.ViolationRepository;
import org.springframework.samples.petclinic.owner.VisitToggles;

public class VisitHashChecker extends HashConsistencyChecker<Visit> {

    private NewVisitRepository repository;
	
	public VisitHashChecker(ViolationRepository violations) {
		super(violations);
	}

    public VisitHashChecker(NewVisitRepository repository, ViolationRepository violations){
        super(violations);
        this.repository = repository;
    }

    public VisitHashChecker(NewVisitRepository repository, ViolationRepository violations, boolean initAsync){
        this(repository, violations);

        if(initAsync){
            //populating hash first
            CompletableFuture.supplyAsync(this::populate)
            .thenAccept(this::initiateAsync);
        }
    }
    
	@Override
	public int check(String name, Collection<Visit> expected) {
		// Not applicable
		return 0;
	}

    private boolean populate() {
        return true;
    }

    public void setRepository(NewVisitRepository repo){
        this.repository = repo;
    }

    protected void initiateAsync(NewVisitRepository repo, boolean async){
        this.repository = repo;
        this.initiateAsync(async);
    }

    protected void initiateAsync(boolean async){

        if (this.repository == null) {
            return;
        }
        if(async) {
            CompletableFuture.supplyAsync(this::continuousChecker);
        }
        else {
            this.continuousChecker();
        }
    }

    private boolean continuousChecker() {
        while(VisitToggles.hashChecker) {
            Collection<Visit> visits = repository.getAllVisits();

            for (Visit visit: visits){
                this.check(visit);
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
