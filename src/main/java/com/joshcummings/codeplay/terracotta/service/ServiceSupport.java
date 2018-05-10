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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public abstract class ServiceSupport {
	private static final String DATABASE_URL = "jdbc:h2:mem:db";
	
	public <T> Set<T> runQuery(String query, Function<ResultSet, T> inflater) {
		return runQuery(query, ps -> ps, inflater);
	}
	
	public <T> Set<T> runQuery(String query, Preparer preparer, Function<ResultSet, T> inflater) {
		Set<T> results = new HashSet<T>();
		try ( Connection conn = DriverManager.getConnection(DATABASE_URL, "user", "password");
				PreparedStatement ps = conn.prepareStatement(query);
				ResultSet rs = preparer.prepare(ps).executeQuery(); ) {
			while ( rs.next() ) {
				results.add(inflater.apply(rs));
			}
		} catch ( SQLException e ) {
			throw new IllegalArgumentException(e);
		}
		return results;
	}

	public Integer count(String tableName) {
		return runQuery("SELECT count(*) FROM " + tableName,
				(rs) -> {
					try {
						return rs.getInt(1);
					} catch ( SQLException e ) {
						throw new IllegalStateException(e);
					}
				}).iterator().next();
	}

	public void runUpdate(String query) {
		try ( Connection conn = DriverManager.getConnection(DATABASE_URL, "user", "password");
				PreparedStatement ps = conn.prepareStatement(query); ){
			ps.executeUpdate();
		} catch ( SQLException e ) {
			throw new IllegalArgumentException(e);
		}
	}
	
	@FunctionalInterface
	public interface Preparer {
		PreparedStatement prepare(PreparedStatement ps) throws SQLException;
	}
}