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
package com.joshcummings.codeplay.terracotta.app;

import com.joshcummings.codeplay.terracotta.model.Account;
import com.joshcummings.codeplay.terracotta.model.User;
import com.joshcummings.codeplay.terracotta.service.AccountService;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Set;

/**
 * @author Josh Cummings
 */
public class UserFilter implements Filter {

	private AccountService accountService;

	public UserFilter(AccountService accountService) {
		this.accountService = accountService;
	}

	@Override
	public void init(FilterConfig filterConfig) { }

	@Override
	public void doFilter(
					ServletRequest req,
					ServletResponse resp,
					FilterChain chain)
			throws IOException, ServletException {

		try {
			if ( req instanceof HttpServletRequest ) {
				HttpServletRequest request = (HttpServletRequest) req;

				User user = (User) request.getSession().getAttribute("authenticatedUser");
				if ( user != null ) {
					Set<Account> accounts = this.accountService.findByUsername(user.getUsername());
					request.setAttribute("authenticatedAccounts", accounts);
				}
			}
		} finally {
			chain.doFilter(req, resp);
		}
	}

	@Override
	public void destroy() { }

}