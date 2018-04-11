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
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
@Controller
@RequestMapping("/owners/{ownerId}")
class PetController {

    private static final String VIEWS_PETS_CREATE_OR_UPDATE_FORM = "pets/createOrUpdatePetForm";
    private final PetRepository pets;
    private final OwnerRepository owners;
    private static int readInconsistencies = 0;
    private static int totalReads = 1;
    private NewPetRepository newPets;
    
    private ConsistencyChecker<Pet> checker;

    @Autowired
    public PetController(PetRepository pets, OwnerRepository owners, NewPetRepository newPets) {
        this.pets = pets;
        this.owners = owners;
        this.newPets = newPets;
        checker = new PetDatabaseChecker(newPets);
        
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
            PetToggles.oldDB = false;
            //switch hash checker on
            PetToggles.hashChecker = true;
            //this.checker = new PetHashChecker(newPets, new ViolationRepositoryImpl(), true);

            return true;
        });
    }

    @ModelAttribute("types")
    public Collection<PetType> populatePetTypes() {
        return this.pets.findPetTypes();
    }

    @ModelAttribute("owner")
    public Owner findOwner(@PathVariable("ownerId") int ownerId) {
        return this.owners.findById(ownerId);
    }

    @InitBinder("owner")
    public void initOwnerBinder(WebDataBinder dataBinder) {
        dataBinder.setDisallowedFields("id");
    }

    @InitBinder("pet")
    public void initPetBinder(WebDataBinder dataBinder) {
        dataBinder.setValidator(new PetValidator());
    }

    @GetMapping("/pets/new")
    public String initCreationForm(Owner owner, ModelMap model) {
        Pet pet = new Pet();
        owner.addPet(pet);
        model.put("pet", pet);
        return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
    }

    @PostMapping("/pets/new")
    public String processCreationForm(Owner owner, @Valid Pet pet, BindingResult result, ModelMap model) {
        if (StringUtils.hasLength(pet.getName()) && pet.isNew() && owner.getPet(pet.getName(), true) != null){
            result.rejectValue("name", "duplicate", "already exists");
        }
        owner.addPet(pet);
        if (result.hasErrors()) {
            model.put("pet", pet);
            return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
        } else {
            this.pets.save(pet);
            return "redirect:/owners/{ownerId}";
        }
    }

    @GetMapping("/pets/{petId}/edit")
    public String initUpdateForm(@PathVariable("petId") int petId, ModelMap model) {
        Pet pet = this.pets.findById(petId);
        model.put("pet", pet);
        return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
    }
    


    @PostMapping("/pets/{petId}/edit")
    public String processUpdateForm(@Valid Pet pet, BindingResult result, Owner owner, ModelMap model) {
        if (result.hasErrors()) {
            pet.setOwner(owner);
            model.put("pet", pet);
            return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
        } else {
            owner.addPet(pet);
            this.pets.save(pet);
            
            if(PetToggles.oldDB && PetToggles.forklifted) {
            	this.pets.save(pet);
            }
            if(PetToggles.newDB) {
            	this.newPets.save(pet);
            }
                 
            return "redirect:/owners/{ownerId}";
        }
    }
    
    private void forklift(){

        if(PetToggles.newDB && PetToggles.oldDB && !PetToggles.forklifted){
            Iterator<Pet> results = this.pets.findByName("").iterator();
            // find pets by name
                while(results.hasNext()){
                    Pet pet = results.next();
                    System.out.println("Lifting: " + pet.getName());
                    newPets.save(pet);

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                PetToggles.forklifted = true;//forklifting only once

        }
    }

}
