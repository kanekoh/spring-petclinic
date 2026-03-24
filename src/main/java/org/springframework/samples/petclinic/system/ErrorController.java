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
package org.springframework.samples.petclinic.system;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

/**
 * Exception handler that renders the error template.
 */
@Provider
public class ErrorController implements ExceptionMapper<RuntimeException> {

	private static final Logger LOG = Logger.getLogger(ErrorController.class);

	@CheckedTemplate
	public static class Templates {

		public static native TemplateInstance error(int status, String message);

	}

	@Override
	public Response toResponse(RuntimeException exception) {
		LOG.errorf(exception, "Unhandled exception: %s", exception.getMessage());
		String message = exception.getMessage();
		return Response.serverError().entity(Templates.error(500, message)).build();
	}

}
