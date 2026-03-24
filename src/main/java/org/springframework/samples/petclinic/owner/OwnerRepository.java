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

import java.util.List;
import java.util.Optional;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

/**
 * Repository class for <code>Owner</code> domain objects.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 * @author Wick Dynex
 */
@ApplicationScoped
public class OwnerRepository implements PanacheRepository<Owner> {

	/**
	 * Retrieve {@link Owner}s from the data store by last name, returning all owners
	 * whose last name <i>starts</i> with the given name.
	 * @param lastName Value to search for
	 * @param page the page to retrieve
	 * @param pageSize the size of each page
	 * @return a List of matching {@link Owner}s
	 */
	public List<Owner> findByLastNameStartingWith(String lastName, int page, int pageSize) {
		return find("lastName like ?1", lastName + "%").page(Page.of(page, pageSize)).list();
	}

	/**
	 * Count owners whose last name starts with the given name.
	 * @param lastName Value to search for
	 * @return the count of matching owners
	 */
	public long countByLastNameStartingWith(String lastName) {
		return count("lastName like ?1", lastName + "%");
	}

	/**
	 * Retrieve all {@link Owner}s whose last name starts with the given name, unpaged.
	 * @param lastName Value to search for
	 * @return a List of matching {@link Owner}s
	 */
	public List<Owner> findByLastNameStartingWithUnpaged(String lastName) {
		return find("lastName like ?1", lastName + "%").list();
	}

	/**
	 * Retrieve an {@link Owner} from the data store by id.
	 * @param id the id to search for
	 * @return an {@link Optional} containing the {@link Owner} if found
	 */
	public Optional<Owner> findById(Integer id) {
		Owner owner = findById((long) id);
		return Optional.ofNullable(owner);
	}

	/**
	 * Save an owner.
	 * @param owner the owner to save
	 */
	@Transactional
	public void save(Owner owner) {
		if (owner.getId() == null) {
			persist(owner);
		}
		else {
			getEntityManager().merge(owner);
		}
	}

}
