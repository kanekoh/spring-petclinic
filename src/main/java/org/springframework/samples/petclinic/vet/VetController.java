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
package org.springframework.samples.petclinic.vet;

import java.util.List;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

/**
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
@Path("/")
public class VetController {

	@CheckedTemplate(requireTypeSafeExpressions = false)
	public static class Templates {

		public static native TemplateInstance vetList(List<Vet> listVets, int currentPage, int totalPages,
				long totalItems);

	}

	private final VetRepository vetRepository;

	@Inject
	public VetController(VetRepository vetRepository) {
		this.vetRepository = vetRepository;
	}

	@GET
	@Path("vets.html")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance showVetList(@QueryParam("page") @DefaultValue("1") int page) {
		int pageSize = 5;
		List<Vet> allVets = this.vetRepository.findAllVets();
		long total = allVets.size();
		int totalPages = (int) Math.ceil((double) total / pageSize);
		List<Vet> listVets = allVets.stream().skip((long) (page - 1) * pageSize).limit(pageSize).toList();
		return Templates.vetList(listVets, page, totalPages, total);
	}

	@GET
	@Path("vets")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Vets showResourcesVetList() {
		Vets vets = new Vets();
		vets.getVetList().addAll(this.vetRepository.findAllVets());
		return vets;
	}

}
