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
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

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
    @Autowired
    private final NewPetRepository newPets;
    private final NewOwnerRepository newOwners;

    @Autowired
    public PetController(PetRepository pets, OwnerRepository owners, NewPetRepository newPets, NewOwnerRepository newOwners) {
        this.pets = pets;
        this.owners = owners;
        this.newPets = newPets;
        this.newOwners = newOwners;
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
            return "redirect:/owners/{ownerId}";
        }
    }

    private void forklift(Collection<Pet> results) {
    	
    	if(PetToggles.newDB && PetToggles.oldDB && !PetToggles.forklifted) {
    		
    		//finding pets by owner
    		
    		System.out.println(results.size());
    		if(results.size() > 0) {
    			
    			for(Pet pet : results) {
    				System.out.println("Lifting" + pet.getOwner());
    				newPets.save(pet);
    				
    			}
    			
    			PetToggles.forklifted = true; //forklifting just one single time
    		}
    	}
    }
    
    private int checkConsistency(Collection<Pet> results){
        int count = 0;
        if(PetToggles.newDB && PetToggles.oldDB && PetToggles.forklifted){
            for(Pet pet : results){

                Pet actual = newPets.findById(pet.getId());
                if(!actual.equals(pet)){
                    System.out.println("MIGRATION ERROR: " +
                        "found: \n" + actual.toString() +
                        "but was supposed to be: \n" + pet.toString());
                        count++;
                    }
                }
        }
        return count;
    }
    
    @GetMapping("/pets/ConsistencyCheck")
    public ModelAndView getConsistencyCheck(){
        List<Pet> results = this.pets.findPetTypes(); // should work @return the {@link Pet} if found
        ModelAndView mav = new ModelAndView("pets/checkConsistency");
        mav.addObject("message","Number of Inconsistencies: " + checkConsistency(results));
        return mav;
    }

}


