/*
 * Copyright 2013-2015 Erudika. http://erudika.com
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
 *
 * For issues and patches go to: https://github.com/erudika
 */
package com.erudika.para.security;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

/**
 * A permission describes the HTTP methods allowed to be executed on a specific resource/type. For example; the 'books'
 * type can have a permission '{ "*" : ["GET"] }' which means "give read-only permissions to everyone".
 * It is backed by a map of resource names (object types) to a set of allowed HTTP methods.
 * @author Alex Bogdanovski [alex@erudika.com]
 */
public final class ResourcePermissions {

	public static enum Value {

		/**
		 * Allows all HTTP methods (full access)
		 */
		READ_WRITE,
		/**
		 * Allows GET method only
		 */
		GET,
		/**
		 * Allows POST method only
		 */
		POST,
		/**
		 * Allows PUT method only
		 */
		PUT,
		/**
		 * ALlows PATCH method only
		 */
		PATCH,
		/**
		 * Allows DELETE method only
		 */
		DELETE,
		/**
		 * Allows read methods: GET, same as {@link #HTTP_GET}
		 */
		READ_ONLY,
		/**
		 * Allows write methods: POST, PUT, PATCH and DELETE
		 */
		WRITE_ONLY;

		public static final EnumSet<Value> ALL_VALUES = EnumSet.of(GET, POST, PUT, PATCH, DELETE);

		@Override
		@JsonValue
		public String toString() {
			switch(this) {
				case READ_WRITE: return ALLOW_ALL;
				case READ_ONLY: return GET.name();
				case WRITE_ONLY: return "w";
				default: return this.name();
			}
		}
	}

	public static String ALLOW_ALL = "*";
	private Map<String, Set<String>> resourcePermissions;

	/**
	 * No-args constructor
	 */
	public ResourcePermissions() {
		resourcePermissions = new HashMap<String, Set<String>>();
//		this(ALLOW_ALL, EnumSet.of(Value.READ_WRITE));
	}

//	public ResourcePermissions(String resource, EnumSet<Value> permissions) {
//		grantPermission(resource, permissions);
//	}

	/**
	 * Grants a permission for a given resource/type.
	 * @param resourceName the type of object / resource name
	 * @param permissions the HTTP methods allowed
	 */
	@JsonAnySetter
	public void grantPermission(String resourceName, EnumSet<Value> permissions) {
		Set<String> methodsAllowed;
		if (permissions == null || permissions.isEmpty()
				|| permissions.containsAll(Value.ALL_VALUES)
				|| permissions.contains(Value.READ_WRITE)
				|| (permissions.contains(Value.READ_ONLY) && permissions.contains(Value.WRITE_ONLY))
				|| (permissions.contains(Value.GET) && permissions.contains(Value.WRITE_ONLY))) {
			methodsAllowed = Collections.singleton(ALLOW_ALL);
		} else {
			if (permissions.contains(Value.WRITE_ONLY)) {
				methodsAllowed = new HashSet<String>() {{
					add(Value.POST.name());
					add(Value.PUT.name());
					add(Value.PATCH.name());
					add(Value.DELETE.name());
				}};
			} else if(permissions.contains(Value.READ_ONLY)) {
				methodsAllowed = Collections.singleton(Value.GET.toString());
			} else {
				methodsAllowed = new HashSet<String>(permissions.size());
				for (Value permission : permissions) {
					methodsAllowed.add(permission.toString());
				}
			}
		}
		if (!StringUtils.isBlank(resourceName)) {
			resourcePermissions.put(resourceName, methodsAllowed);
		}
	}

	public void revokePermission(String resource) {
		if (!StringUtils.isBlank(resource)) {
			resourcePermissions.remove(resource);
		}
	}

	/**
	 * Get the value of resourcePermissions
	 *
	 * @return the value of resourcePermissions
	 */
	@JsonAnyGetter
	public Map<String, Set<String>> get() {
		return resourcePermissions;
	}

}
