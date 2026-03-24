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
import java.util.List;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Wick Dynex
 */
@Path("/owners")
public class OwnerController {

	@CheckedTemplate(requireTypeSafeExpressions = false)
	public static class Templates {

		public static native TemplateInstance createOrUpdateOwnerForm(Owner owner, List<String> errors);

		public static native TemplateInstance findOwners(Owner owner, List<String> errors);

		public static native TemplateInstance ownersList(List<Owner> listOwners, int currentPage, int totalPages,
				long totalItems);

		public static native TemplateInstance ownerDetails(Owner owner);

	}

	private final OwnerRepository owners;

	private final Validator validator;

	@Inject
	public OwnerController(OwnerRepository owners, Validator validator) {
		this.owners = owners;
		this.validator = validator;
	}

	@GET
	@Path("/new")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance initCreationForm() {
		Owner owner = new Owner();
		return Templates.createOrUpdateOwnerForm(owner, List.of());
	}

	@POST
	@Path("/new")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_HTML)
	public Response processCreationForm(@BeanParam OwnerForm form) {
		Owner owner = new Owner();
		owner.setFirstName(form.firstName);
		owner.setLastName(form.lastName);
		owner.setAddress(form.address);
		owner.setCity(form.city);
		owner.setTelephone(form.telephone);

		var violations = validator.validate(owner);
		if (!violations.isEmpty()) {
			List<String> errors = violations.stream().map(ConstraintViolation::getMessage).toList();
			return Response.ok(Templates.createOrUpdateOwnerForm(owner, errors)).build();
		}

		this.owners.save(owner);
		return Response.seeOther(URI.create("/owners/" + owner.getId())).build();
	}

	@GET
	@Path("/find")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance initFindForm() {
		Owner owner = new Owner();
		return Templates.findOwners(owner, List.of());
	}

	@GET
	@Path("")
	@Produces(MediaType.TEXT_HTML)
	public Response processFindForm(@QueryParam("page") @DefaultValue("1") int page,
			@QueryParam("lastName") String lastName) {
		if (lastName == null) {
			lastName = "";
		}

		int pageSize = 5;
		long total = this.owners.countByLastNameStartingWith(lastName);

		if (total == 0) {
			Owner owner = new Owner();
			owner.setLastName(lastName);
			return Response.ok(Templates.findOwners(owner, List.of("not found"))).build();
		}

		if (total == 1) {
			List<Owner> found = this.owners.findByLastNameStartingWithUnpaged(lastName);
			Owner owner = found.get(0);
			return Response.seeOther(URI.create("/owners/" + owner.getId())).build();
		}

		List<Owner> listOwners = this.owners.findByLastNameStartingWith(lastName, page - 1, pageSize);
		int totalPages = (int) Math.ceil((double) total / pageSize);
		return Response.ok(Templates.ownersList(listOwners, page, totalPages, total)).build();
	}

	@GET
	@Path("/{ownerId: [0-9]+}/edit")
	@Produces(MediaType.TEXT_HTML)
	public Response initUpdateOwnerForm(@PathParam("ownerId") int ownerId) {
		Owner owner = this.owners.findById(ownerId)
			.orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId));
		return Response.ok(Templates.createOrUpdateOwnerForm(owner, List.of())).build();
	}

	@POST
	@Path("/{ownerId: [0-9]+}/edit")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_HTML)
	public Response processUpdateOwnerForm(@PathParam("ownerId") int ownerId, @BeanParam OwnerForm form) {
		Owner owner = this.owners.findById(ownerId)
			.orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId));

		owner.setFirstName(form.firstName);
		owner.setLastName(form.lastName);
		owner.setAddress(form.address);
		owner.setCity(form.city);
		owner.setTelephone(form.telephone);

		var violations = validator.validate(owner);
		if (!violations.isEmpty()) {
			List<String> errors = violations.stream().map(ConstraintViolation::getMessage).toList();
			return Response.ok(Templates.createOrUpdateOwnerForm(owner, errors)).build();
		}

		owner.setId(ownerId);
		this.owners.save(owner);
		return Response.seeOther(URI.create("/owners/" + ownerId)).build();
	}

	@GET
	@Path("/{ownerId: [0-9]+}")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance showOwner(@PathParam("ownerId") int ownerId) {
		Owner owner = this.owners.findById(ownerId)
			.orElseThrow(() -> new IllegalArgumentException(
					"Owner not found with id: " + ownerId + ". Please ensure the ID is correct "));
		return Templates.ownerDetails(owner);
	}

	/**
	 * Form bean for owner form submissions.
	 */
	public static class OwnerForm {

		@FormParam("firstName")
		public String firstName;

		@FormParam("lastName")
		public String lastName;

		@FormParam("address")
		public String address;

		@FormParam("city")
		public String city;

		@FormParam("telephone")
		public String telephone;

	}

}
