/*
 * Copyright 2015-2018 Josh Cummings
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.joshcummings.codeplay.terracotta.service;

import com.joshcummings.codeplay.terracotta.model.User;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;

/**
 * This class makes Terracotta Bank vulnerable to SQL injection
 * attacks because it concatenates queries instead of using
 * bind variables.
 *
 * @author Josh Cummings
 */
@Service
public class UserService extends ServiceSupport {
	public void addUser(User user) {
		String password = user.getPassword();
		String hashed = BCrypt.hashpw(password, BCrypt.gensalt());

		runUpdate("INSERT INTO users (id, username, password, name, email)"
				+ " VALUES ('" + user.getId() + "','" + user.getUsername() + 
				"','" + hashed + "','" + user.getName() + "','" + user.getEmail() + "')");
	}

	public User findByUsername(String username) {
		Set<User> users = runQuery("SELECT * FROM users WHERE username = '" + username + "'",
			(rs) -> {
				try {
					return new User(rs.getString(1), rs.getString(4), rs.getString(5), rs.getString(2), rs.getString(3));
				} catch ( SQLException e ) {
					throw new IllegalStateException(e);
				}
			});
		return users.isEmpty() ? null : users.iterator().next();
	}

	private transient byte[] random = BCrypt.hashpw(UUID.randomUUID().toString(), BCrypt.gensalt()).getBytes();

	public boolean findByUsernameAndPassword(String username, String password) {
		Set<String> users = runQuery("SELECT password FROM users WHERE username = '" + username + "'",
				(rs) -> {
					try {
						return rs.getString(1);
					} catch ( SQLException e ) {
						throw new IllegalStateException(e);
					}
				});

		return BCrypt.checkpw(password, users.isEmpty() ?
				new String(random) :
				users.iterator().next()) && !users.isEmpty();
	}

	public Integer count() {
		return super.count("users");
	}
}
