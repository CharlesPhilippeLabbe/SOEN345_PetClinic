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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 */
@Controller
class OwnerController {

    private static final String VIEWS_OWNER_CREATE_OR_UPDATE_FORM = "owners/createOrUpdateOwnerForm";
    private final OwnerRepository owners;
    private static int readInconsistencies = 0;
    private static int totalReads = 1;//set to one, for ratio's sake
    private NewOwnerRepository newOwners;

    private ConsistencyChecker<Owner> checker;



    @Autowired
    public OwnerController(OwnerRepository clinicService, NewOwnerRepository newOwners) {
        this.owners = clinicService;
        this.newOwners = newOwners;
         checker = new OwnerDatabaseChecker(newOwners);

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
                }catch(InterruptedException e){
                }
            }
            System.out.println("D a t a b a s e   s w a p p e d !!!!");
            //once the conditions are met swap databases
            OwnerToggles.oldDB = false;
            //switch hash checker on
            OwnerToggles.hashChecker = true;
            this.checker = new OwnerHashChecker(newOwners, new ViolationRepositoryImpl(), true);

            return true;
        });
    }

    @InitBinder
    public void setAllowedFields(WebDataBinder dataBinder) {
        dataBinder.setDisallowedFields("id");
    }

    @GetMapping("/owners/new")
    public String initCreationForm(Map<String, Object> model) {
        Owner owner = new Owner();
        model.put("owner", owner);
        return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
    }

    @PostMapping("/owners/new")
    public String processCreationForm(@Valid Owner owner, BindingResult result) {
        if (result.hasErrors())
        {
            return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
        }

        else
        {
	        	if (OwnerToggles.newDB){
	        		this.newOwners.save(owner);
	        	}

	        	if (OwnerToggles.oldDB && OwnerToggles.forklifted) {
	                this.owners.save(owner);
	        	}

	        	//toggles are handledby the current instance of the checker
                //saving every new owners in new database
                this.checker.update(owner);

	        	return "redirect:/owners/" + owner.getId();
        }
    }

    @GetMapping("/owners/find")
    public String initFindForm(Map<String, Object> model) {
        model.put("owner", new Owner());
        return "owners/findOwners";
    }

    @GetMapping("/owners")
    public String processFindForm(Owner owner, BindingResult result, Map<String, Object> model) {

        // allow parameterless GET request for /owners to return all records
        if (owner.getLastName() == null) {
            owner.setLastName(""); // empty string signifies broadest possible search
        }

        Collection<Owner> results;

        // find owners by last name
        if(OwnerToggles.oldDB){
            results = this.owners.findByLastName(owner.getLastName());

        }else{//only new db setup
            results = this.newOwners.findByLastName(owner.getLastName());
            //no hash checks for whole set of owners because it is handled by the initAsync() once db is swapped
        }

        //Read inconsistency checker
        if( OwnerToggles.forklifted){
            final String L = owner.getLastName();
            //will check with hash or old/new db
            //hash check will check only if the results.size() == 1, the rest is handled by async checker
            CompletableFuture.supplyAsync(()-> readByLastNameInconsistency(L, results));
        }


        if (results.isEmpty()) {
            // no owners found
            result.rejectValue("lastName", "notFound", "not found");
            return "owners/findOwners";
        } else if (results.size() == 1) {
            // 1 owner found
            owner = results.iterator().next();
            return "redirect:/owners/" + owner.getId();
        } else {
            // multiple owners found
            model.put("selections", results);
            return "owners/ownersList";
        }
    }

    @GetMapping("/owners/{ownerId}/edit")
    public String initUpdateOwnerForm(@PathVariable("ownerId") int ownerId, Model model) {
        Owner owner;
        if(OwnerToggles.oldDB){
            owner = this.owners.findById(ownerId);

        }else{
            owner = this.newOwners.findById(ownerId);
        }
        //Read inconsistency checker hash or old/new db
        if(OwnerToggles.forklifted){
            CompletableFuture.supplyAsync(() -> readByIdInconsistency(ownerId, owner));
        }
        model.addAttribute(owner);
        return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
    }

    @PostMapping("/owners/{ownerId}/edit")
    public String processUpdateOwnerForm(@Valid Owner owner, BindingResult result, @PathVariable("ownerId") int ownerId) {
        if (result.hasErrors()) {
            return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
        }

        else
        {
            owner.setId(ownerId);

            if(OwnerToggles.oldDB )
            {
            		this.owners.save(owner);
            }

            if(OwnerToggles.newDB && OwnerToggles.forklifted)
            {
            		this.newOwners.save(owner);
            }

            return "redirect:/owners/{ownerId}";
        }
    }

    /**
     * Custom handler for displaying an owner.
     *
     * @param ownerId the ID of the owner to display
     * @return a ModelMap with the model attributes for the view
     */
    @GetMapping("/owners/{ownerId}")
    public ModelAndView showOwner(@PathVariable("ownerId") final int ownerId) {
        ModelAndView mav = new ModelAndView("owners/ownerDetails");

        Owner owner;

        if(OwnerToggles.oldDB){
            owner = this.owners.findById(ownerId);
        }else{
            owner = this.newOwners.findById(ownerId);
        }
        //will check hash or new/old db
        if(OwnerToggles.forklifted){
            CompletableFuture.supplyAsync(() -> readByIdInconsistency(ownerId, owner));
        }

        mav.addObject(owner);
        return mav;
    }


    private void forklift(){

        if(OwnerToggles.newDB && OwnerToggles.oldDB && !OwnerToggles.forklifted){
            Iterator<Owner> results = this.owners.findByLastName("").iterator();
            // find owners by last name
                while(results.hasNext()){
                    Owner owner = results.next();
                    System.out.println("Lifting: " + owner.getLastName());
                    newOwners.save(owner);

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                OwnerToggles.forklifted = true;//forklifting only once

        }
    }



    private int checkConsistency(Collection<Owner> results){

        return checker.check(results);
    }

    @GetMapping("/owners/ConsistencyCheck")
    public ModelAndView getConsistencyCheck(){
        Collection<Owner> results = this.owners.findByLastName("");
        ModelAndView mav = new ModelAndView("owners/checkConsistency");
        mav.addObject("message","Number of Inconsistencies: " + checkConsistency(results));
        return mav;
    }

    @GetMapping("/owners/ReadConsistencyCheck")
    public ModelAndView getReadInconsistencies(){
        Collection<Owner> results = this.owners.findByLastName("");

        ModelAndView mav = new ModelAndView("owners/checkConsistency");
        mav.addObject("message","Number of Read Inconsistencies: " + readByLastNameInconsistency("", results));
        return mav;
    }

    public int readByLastNameInconsistency(String lastname, Collection<Owner> results){

        readInconsistencies += this.checker.check(lastname, results);

        return readInconsistencies;
    }


    @GetMapping("/owners/ReadConsistencyCheck/{ownerId}")
    public ModelAndView getReadInconsistencies(@PathVariable("ownerId") final int ownerId){
        Owner owner = this.owners.findById(ownerId);

        ModelAndView mav = new ModelAndView("owners/checkConsistency");
        mav.addObject("message","Number of Read Inconsistencies: " + readByIdInconsistency(ownerId, owner));
        return mav;
    }

    public int readByIdInconsistency(int id, Owner expected){

        checker.check(id, expected);

        return checker.getReadInconsistencies();
    }

}
