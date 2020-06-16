package org.c19x.server.session;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * System user.
 *
 * @author user
 */
public class User {
	public final String name;
	public String hashOfPassword;
	public final Set<String> permissions = new HashSet<>(1);

	public User(final String name, final String hashOfPassword) {
		super();
		this.name = name;
		this.hashOfPassword = hashOfPassword;
	}

	@Override
	public String toString() {
		return "User [name=" + name + ", hashOfPassword=" + hashOfPassword + ", permissions="
				+ permissions.stream().sorted().collect(Collectors.toList()) + "]";
	}
}
