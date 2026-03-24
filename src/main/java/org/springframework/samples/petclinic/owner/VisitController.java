/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.owner;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Dave Syer
 * @author Wick Dynex
 */
@Path("/owners/{ownerId: [0-9]+}/pets/{petId: [0-9]+}/visits")
public class VisitController {

	@CheckedTemplate
	public static class Templates {

		public static native TemplateInstance createOrUpdateVisitForm(Owner owner, Pet pet, Visit visit,
				List<String> errors);

	}

	private final OwnerRepository owners;

	@Inject
	public VisitController(OwnerRepository owners) {
		this.owners = owners;
	}

	@GET
	@Path("/new")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance initNewVisitForm(@PathParam("ownerId") int ownerId, @PathParam("petId") int petId) {
		Owner owner = getOwner(ownerId);
		Pet pet = getPet(owner, petId);
		Visit visit = new Visit();
		return Templates.createOrUpdateVisitForm(owner, pet, visit, List.of());
	}

	@POST
	@Path("/new")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_HTML)
	public Response processNewVisitForm(@PathParam("ownerId") int ownerId, @PathParam("petId") int petId,
			@BeanParam VisitForm form) {
		Owner owner = getOwner(ownerId);
		Pet pet = getPet(owner, petId);

		Visit visit = new Visit();
		if (form.date != null && !form.date.isBlank()) {
			try {
				visit.setDate(LocalDate.parse(form.date));
			}
			catch (Exception e) {
				// keep default
			}
		}
		visit.setDescription(form.description);

		// Validate
		if (form.description == null || form.description.isBlank()) {
			return Response.ok(Templates.createOrUpdateVisitForm(owner, pet, visit, List.of("Description is required")))
				.build();
		}

		pet.addVisit(visit);
		this.owners.save(owner);
		return Response.seeOther(URI.create("/owners/" + ownerId)).build();
	}

	private Owner getOwner(int ownerId) {
		return this.owners.findById(ownerId)
			.orElseThrow(() -> new IllegalArgumentException(
					"Owner not found with id: " + ownerId + ". Please ensure the ID is correct "));
	}

	private Pet getPet(Owner owner, int petId) {
		Pet pet = owner.getPet(petId);
		if (pet == null) {
			throw new IllegalArgumentException(
					"Pet with id " + petId + " not found for owner with id " + owner.getId() + ".");
		}
		return pet;
	}

	/**
	 * Form bean for visit form submissions.
	 */
	public static class VisitForm {

		@FormParam("date")
		public String date;

		@FormParam("description")
		public String description;

	}

}
