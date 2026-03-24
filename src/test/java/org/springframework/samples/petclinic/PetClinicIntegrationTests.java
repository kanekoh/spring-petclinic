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

package org.springframework.samples.petclinic;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.vet.VetRepository;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
public class PetClinicIntegrationTests {

	@Inject
	VetRepository vets;

	@Test
	void findAll() {
		vets.findAllVets();
		vets.findAllVets(); // served from cache
	}

	@Test
	void ownerDetails() {
		given().when().get("/owners/1").then().statusCode(200);
	}

	@Test
	void ownerList() {
		given().queryParam("lastName", "").when().get("/owners").then().statusCode(200);
	}

	@Test
	void staticCss() {
		given().when().get("/resources/css/petclinic.css").then().statusCode(200);
	}

	@Test
	void staticFavicon() {
		given().when().get("/resources/images/favicon.png").then().statusCode(200);
	}

	@Test
	void staticSpringLogo() {
		given().when().get("/resources/images/spring-logo.svg").then().statusCode(200);
	}

	@Test
	void webjarBootstrap() {
		given().when().get("/webjars/bootstrap/5.3.8/dist/js/bootstrap.bundle.min.js").then().statusCode(200);
	}

	@Test
	void webjarFontAwesome() {
		given().when().get("/webjars/font-awesome/4.7.0/css/font-awesome.min.css").then().statusCode(200);
	}

	@Test
	void ownerDetailsPageReferencesStaticResources() {
		String body = given().when().get("/owners/1").then().statusCode(200).extract().body().asString();
		assertThat(body).contains("/resources/css/petclinic.css");
		assertThat(body).contains("/webjars/bootstrap/");
	}

}
