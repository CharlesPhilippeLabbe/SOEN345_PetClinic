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
package org.springframework.samples.petclinic.vet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.xml.ws.ResponseWrapper;

/**
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
@Controller
class VetController {

    private final VetRepository vets;
    private final NewVetRepository newVets;

    @Autowired
    public VetController(VetRepository clinicService, NewVetRepository clinicService) {
        this.vets = clinicService;
        this.newVets = clinicService;
    }

    @GetMapping("/vets.html")
    public String showVetList(Map<String, Object> model) {
        // Here we are returning an object of type 'Vets' rather than a collection of Vet
        // objects so it is simpler for Object-Xml mapping
    	
    	//triggering forklift
    	this.forklift();
    	
        Vets vets = new Vets();
        vets.getVetList().addAll(this.vets.findAll());
        model.put("vets", vets);
        return "vets/vetList";
    }

    @GetMapping({ "/vets.json", "/vets.xml" })
    public @ResponseWrapper Vets showResourcesVetList() {
        // Here we are returning an object of type 'Vets' rather than a collection of Vet
        // objects so it is simpler for JSon/Object mapping
        Vets vets = new Vets();
        vets.getVetList().addAll(this.vets.findAll());
        return vets;
    }
    
    
    public void forklift(){
    	if(VetToggles.newDB && VetToggles.oldDB){
    		CompletableFuture.supplyAsync(()->{
    			
    			Collection<Vet> results = vets.findAll();
    			
    			if(results.size() > 0){
    				for(Vet vet: results){    
    				System.out.println("Lifting " + vet.getLastName());
    				newVets.addNewVet(new NewVet(vet));
    				}
    			}
   
    			return true;
    		});
    	}
    }
    
    
//    private int checkConsistency(Collection<Vet> results){
//        int count = 0;
//        if(VetToggles.newDB && VetToggles.oldDB &VetToggles.forklifted){
//            for(Vet vet : results){
//            	
//                System.out.println(vet.toString());
//                Vet actual = newVets.findById(vets.getId());
//                
//                if(!actual.equals(vet)){
//                    System.out.println("MIGRATION ERROR: " +
//                        "found: \n" + actual.toString() +
//                        "but was supposed to be: \n" + vet.toString());
//                        count++;
//                    }
//                }
//        }
//        return count;
//    }
//    
//    @GetMapping("/vets/ConsistencyCheck")
//    public ModelAndView getConsistencyCheck(){
//        Collection<Vet> results = this.vets.findByLastName("");
//        ModelAndView modAndView = new ModelAndView("vets/checkConsistency");
//        modAndView.addObject("message","Number of Inconsistencies: " + checkConsistency(results));
//        return modAndView;
//    }


}
