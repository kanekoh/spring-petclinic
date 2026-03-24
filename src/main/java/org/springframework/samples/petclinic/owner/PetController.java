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
import java.text.ParseException;
import java.time.LocalDate;
import java.util.Collection;
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
 * @author Wick Dynex
 */
@Path("/owners/{ownerId: [0-9]+}/pets")
public class PetController {

	@CheckedTemplate
	public static class Templates {

		public static native TemplateInstance createOrUpdatePetForm(Owner owner, Pet pet,
				Collection<PetType> types, List<String> errors);

	}

	private final OwnerRepository owners;

	private final PetTypeRepository types;

	private final PetTypeFormatter petTypeFormatter;

	@Inject
	public PetController(OwnerRepository owners, PetTypeRepository types, PetTypeFormatter petTypeFormatter) {
		this.owners = owners;
		this.types = types;
		this.petTypeFormatter = petTypeFormatter;
	}

	@GET
	@Path("/new")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance initCreationForm(@PathParam("ownerId") int ownerId) {
		Owner owner = getOwner(ownerId);
		Pet pet = new Pet();
		owner.addPet(pet);
		Collection<PetType> petTypes = this.types.findPetTypes();
		return Templates.createOrUpdatePetForm(owner, pet, petTypes, List.of());
	}

	@POST
	@Path("/new")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_HTML)
	public Response processCreationForm(@PathParam("ownerId") int ownerId, @BeanParam PetForm form) {
		Owner owner = getOwner(ownerId);
		Collection<PetType> petTypes = this.types.findPetTypes();

		Pet pet = new Pet();
		pet.setName(form.name);
		pet.setBirthDate(parseDate(form.birthDate));
		if (form.type != null && !form.type.isBlank()) {
			try {
				pet.setType(this.petTypeFormatter.parse(form.type));
			}
			catch (ParseException e) {
				// type not found, leave null
			}
		}

		PetValidator validator = new PetValidator();
		List<PetValidator.PetFieldError> errors = validator.validate(pet);

		// Check for duplicate name
		if (pet.getName() != null && !pet.getName().isBlank() && pet.isNew()
				&& owner.getPet(pet.getName(), true) != null) {
			errors = new java.util.ArrayList<>(errors);
			errors.add(new PetValidator.PetFieldError("name", "duplicate", "already exists"));
		}

		// Validate birth date not in the future
		if (pet.getBirthDate() != null && pet.getBirthDate().isAfter(LocalDate.now())) {
			errors = new java.util.ArrayList<>(errors);
			errors.add(new PetValidator.PetFieldError("birthDate", "typeMismatch.birthDate",
					"Birth date cannot be in the future"));
		}

		if (!errors.isEmpty()) {
			List<String> errorMessages = errors.stream().map(PetValidator.PetFieldError::message).toList();
			return Response.ok(Templates.createOrUpdatePetForm(owner, pet, petTypes, errorMessages)).build();
		}

		owner.addPet(pet);
		this.owners.save(owner);
		return Response.seeOther(URI.create("/owners/" + ownerId)).build();
	}

	@GET
	@Path("/{petId: [0-9]+}/edit")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance initUpdateForm(@PathParam("ownerId") int ownerId, @PathParam("petId") int petId) {
		Owner owner = getOwner(ownerId);
		Pet pet = owner.getPet(petId);
		Collection<PetType> petTypes = this.types.findPetTypes();
		return Templates.createOrUpdatePetForm(owner, pet, petTypes, List.of());
	}

	@POST
	@Path("/{petId: [0-9]+}/edit")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_HTML)
	public Response processUpdateForm(@PathParam("ownerId") int ownerId, @PathParam("petId") int petId,
			@BeanParam PetForm form) {
		Owner owner = getOwner(ownerId);
		Collection<PetType> petTypes = this.types.findPetTypes();

		Pet pet = owner.getPet(petId);
		if (pet == null) {
			pet = new Pet();
			pet.setId(petId);
		}
		pet.setName(form.name);
		pet.setBirthDate(parseDate(form.birthDate));
		if (form.type != null && !form.type.isBlank()) {
			try {
				pet.setType(this.petTypeFormatter.parse(form.type));
			}
			catch (ParseException e) {
				// type not found
			}
		}

		PetValidator validator = new PetValidator();
		List<PetValidator.PetFieldError> errors = validator.validate(pet);

		// Check for duplicate name
		if (pet.getName() != null && !pet.getName().isBlank()) {
			Pet existingPet = owner.getPet(pet.getName(), false);
			if (existingPet != null && !existingPet.getId().equals(pet.getId())) {
				errors = new java.util.ArrayList<>(errors);
				errors.add(new PetValidator.PetFieldError("name", "duplicate", "already exists"));
			}
		}

		// Validate birth date not in the future
		if (pet.getBirthDate() != null && pet.getBirthDate().isAfter(LocalDate.now())) {
			errors = new java.util.ArrayList<>(errors);
			errors.add(new PetValidator.PetFieldError("birthDate", "typeMismatch.birthDate",
					"Birth date cannot be in the future"));
		}

		if (!errors.isEmpty()) {
			List<String> errorMessages = errors.stream().map(PetValidator.PetFieldError::message).toList();
			return Response.ok(Templates.createOrUpdatePetForm(owner, pet, petTypes, errorMessages)).build();
		}

		updatePetDetails(owner, pet);
		return Response.seeOther(URI.create("/owners/" + ownerId)).build();
	}

	private Owner getOwner(int ownerId) {
		return this.owners.findById(ownerId)
			.orElseThrow(() -> new IllegalArgumentException(
					"Owner not found with id: " + ownerId + ". Please ensure the ID is correct "));
	}

	private LocalDate parseDate(String dateStr) {
		if (dateStr == null || dateStr.isBlank()) {
			return null;
		}
		try {
			return LocalDate.parse(dateStr);
		}
		catch (Exception e) {
			return null;
		}
	}

	private void updatePetDetails(Owner owner, Pet pet) {
		Integer id = pet.getId();
		if (id == null) {
			throw new IllegalStateException("'pet.getId()' must not be null");
		}
		Pet existingPet = owner.getPet(id);
		if (existingPet != null) {
			existingPet.setName(pet.getName());
			existingPet.setBirthDate(pet.getBirthDate());
			existingPet.setType(pet.getType());
		}
		else {
			owner.addPet(pet);
		}
		this.owners.save(owner);
	}

	/**
	 * Form bean for pet form submissions.
	 */
	public static class PetForm {

		@FormParam("name")
		public String name;

		@FormParam("birthDate")
		public String birthDate;

		@FormParam("type")
		public String type;

	}

}
