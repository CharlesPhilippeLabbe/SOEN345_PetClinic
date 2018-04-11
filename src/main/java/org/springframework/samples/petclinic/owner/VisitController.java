/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.owner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.model.ConsistencyChecker;
import org.springframework.samples.petclinic.model.ViolationRepositoryImpl;
import org.springframework.samples.petclinic.visit.NewVisitRepository;
import org.springframework.samples.petclinic.visit.Visit;
import org.springframework.samples.petclinic.visit.VisitDatabaseChecker;
import org.springframework.samples.petclinic.visit.VisitHashChecker;
import org.springframework.samples.petclinic.visit.VisitRepository;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Dave Syer
 */
@Controller
class VisitController {

    private final VisitRepository visits;
    private final PetRepository pets;
    
    private static int readInconsistencies = 0;
    private static int totalReads = 1;//set to one, for ratio's sake
    private NewVisitRepository newVisits;

    private ConsistencyChecker<Visit> checker;


    @Autowired
    public VisitController(VisitRepository visits, PetRepository pets, NewVisitRepository newVisits) {
        this.visits = visits;
        this.pets = pets;
        this.newVisits = newVisits;
        
        checker = new VisitDatabaseChecker(newVisits);

        CompletableFuture.supplyAsync(() ->{

           forklift();

           //wait until those conditions are met
           while(totalReads < 100 && ((double)readInconsistencies/totalReads) < 0.01){
               try{
                   Thread.sleep(1000);
                   if(totalReads >0){
                       System.out.println("#################################################################");
                       System.out.println("Total Reads: " + totalReads);
                       System.out.println("Miss ratio: " + (readInconsistencies/totalReads));
                   }
               } catch(InterruptedException e){
               }
           }
           System.out.println("D a t a b a s e   s w a p p e d !!!!");
           //once the conditions are met swap databases
           VisitToggles.oldDB = false;
           //switch hash checker on
           VisitToggles.hashChecker = true;
           this.checker = new VisitHashChecker(newVisits, new ViolationRepositoryImpl(), true);

           return true;
       });
    }

    @InitBinder
    public void setAllowedFields(WebDataBinder dataBinder) {
        dataBinder.setDisallowedFields("id");
    }

    /**
     * Called before each and every @RequestMapping annotated method.
     * 2 goals:
     * - Make sure we always have fresh data
     * - Since we do not use the session scope, make sure that Pet object always has an id
     * (Even though id is not part of the form fields)
     *
     * @param petId
     * @return Pet
     */
    @ModelAttribute("visit")
    public Visit loadPetWithVisit(@PathVariable("petId") int petId, Map<String, Object> model) {
        Pet pet = this.pets.findById(petId);
        model.put("pet", pet);
        
        Visit visit = new Visit();
        pet.addVisit(visit);
        
        return visit;
    }

    // Spring MVC calls method loadPetWithVisit(...) before initNewVisitForm is called
    @GetMapping("/owners/*/pets/{petId}/visits/new")
    public String initNewVisitForm(@PathVariable("petId") int petId, Map<String, Object> model) {
        return "pets/createOrUpdateVisitForm";
    }

    // Spring MVC calls method loadPetWithVisit(...) before processNewVisitForm is called
    @PostMapping("/owners/{ownerId}/pets/{petId}/visits/new")
    public String processNewVisitForm(@Valid Visit visit, BindingResult result) {
        if (result.hasErrors()) 
        {
            return "pets/createOrUpdateVisitForm";
        } 
        else 
        {
        		if (VisitToggles.newDB)
        		{
        			this.newVisits.save(visit);
        		}
        		if (VisitToggles.oldDB && VisitToggles.forklifted)
        		{
        			this.visits.save(visit);
        		}
        		
            this.checker.update(visit);
            return "redirect:/owners/{ownerId}";
        }
    }
    
    private void forklift() {
        if(VisitToggles.newDB && VisitToggles.oldDB && !VisitToggles.forklifted){
            Iterator<Visit> results = this.visits.getAllVisits().iterator();
            
            while (results.hasNext()) {
            		Visit visit = results.next();
            		System.out.println("Lifting: " + visit.getId());
            		newVisits.save(visit);
            		
                 try {
                	 	Thread.sleep(500);
                 } catch (InterruptedException e) {
                	 	e.printStackTrace();
                 }
            }
            VisitToggles.forklifted = true;//forklifting only once
        }
    }
    
    private int checkConsistency(Collection<Visit> results) {
        return checker.check(results);
    }

    @GetMapping("/visits/ConsistencyCheck")
    public ModelAndView getConsistencyCheck() {
        Collection<Visit> results = this.visits.getAllVisits();
        
        ModelAndView mav = new ModelAndView("visits/checkConsistency");
        mav.addObject("message","Number of Inconsistencies: " + checkConsistency(results));
        
        return mav;
    }

    @GetMapping("/visits/ReadConsistencyCheck")
    public ModelAndView getReadInconsistencies() {
        Collection<Visit> results = this.visits.getAllVisits();

        ModelAndView mav = new ModelAndView("visits/checkConsistency");
        mav.addObject("message","Number of Read Inconsistencies: " + readInconsistency(results));
        return mav;
    }

    public int readInconsistency(Collection<Visit> results){
        readInconsistencies += this.checker.check(results);

        return readInconsistencies;
    }


    @GetMapping("/visits/ReadConsistencyCheck/{visitId}")
    public ModelAndView getReadInconsistencies(@PathVariable("visitId") final int visitId){
        Visit visit = this.visits.getVisit(visitId);

        ModelAndView mav = new ModelAndView("visits/checkConsistency");
        mav.addObject("message","Number of Read Inconsistencies: " + readByIdInconsistency(visitId, visit));
        
        return mav;
    }

    public int readByIdInconsistency(int id, Visit expected){

        checker.check(id, expected);

        return checker.getReadInconsistencies();
    }
}
