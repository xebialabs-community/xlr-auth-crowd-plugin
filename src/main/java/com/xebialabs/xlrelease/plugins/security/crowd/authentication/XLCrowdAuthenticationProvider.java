/**
 * Copyright 2025 DIGITAL.AI
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.xebialabs.xlrelease.plugins.security.crowd.authentication;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.model.authentication.UserAuthenticationContext;
import com.atlassian.crowd.model.authentication.ValidationFactor;
import com.atlassian.crowd.service.client.CrowdClient;

import com.xebialabs.xlrelease.plugins.security.crowd.userdetails.CrowdUserDetails;
import com.xebialabs.xlrelease.plugins.security.crowd.userdetails.CrowdUserDetailsService;

public class XLCrowdAuthenticationProvider extends CrowdAuthenticationProvider {
    private final CrowdClient authenticationManager;
    private final CrowdUserDetailsService userDetailsService;

    public XLCrowdAuthenticationProvider(CrowdClient authenticationManager, CrowdUserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (this.supports(authentication.getClass()) && authentication instanceof UsernamePasswordAuthenticationToken) {
            return this.authenticateUsernamePassword((UsernamePasswordAuthenticationToken) authentication);
        }
        return null;
    }

    @Override
    protected String authenticate(final String username, final String password) throws Exception {
        ValidationFactor[] validationFactors = new ValidationFactor[0];
        UserAuthenticationContext userAuthenticationContext = new UserAuthenticationContext(username, PasswordCredential.unencrypted(password), validationFactors, null);
        return this.authenticationManager.authenticateSSOUser(userAuthenticationContext);
    }

    @Override
    protected CrowdUserDetails loadUserByUsername(final String username) throws Exception {
        return this.userDetailsService.loadUserByUsername(username);
    }
}
