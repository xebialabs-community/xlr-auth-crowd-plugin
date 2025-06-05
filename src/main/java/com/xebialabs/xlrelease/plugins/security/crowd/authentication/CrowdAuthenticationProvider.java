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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.StringUtils;
import com.atlassian.crowd.exception.ExpiredCredentialException;
import com.atlassian.crowd.exception.InactiveAccountException;

import com.xebialabs.xlrelease.plugins.security.crowd.userdetails.CrowdUserDetails;

public abstract class CrowdAuthenticationProvider implements AuthenticationProvider {
    private static final Logger logger = LoggerFactory.getLogger(CrowdAuthenticationProvider.class);

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        logger.debug("Authenticating using CrowdAuthenticationProvider");

        if (authentication instanceof UsernamePasswordAuthenticationToken token) {
            return authenticateUsernamePassword(token);
        }
        return null;
    }

    protected Authentication authenticateUsernamePassword(UsernamePasswordAuthenticationToken token) {
        String username = token.getPrincipal() != null ? token.getPrincipal().toString() : null;
        String password = token.getCredentials() != null ? token.getCredentials().toString() : null;

        if (!StringUtils.hasText(username)) {
            throw new BadCredentialsException("Username is empty");
        }
        if (!StringUtils.hasText(password)) {
            throw new BadCredentialsException("Password is empty");
        }

        try {
            authenticate(username, password);
            CrowdUserDetails userDetails = loadUserByUsername(username);
            return new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());
        } catch (Exception e) {
            throw translateException(e);
        }
    }

    protected abstract String authenticate(String username, String password) throws Exception;

    protected abstract CrowdUserDetails loadUserByUsername(String username) throws Exception;

    private AuthenticationException translateException(Exception e) {
        if (e instanceof AuthenticationException authEx) {
            return authEx;
        }
        if (e instanceof ExpiredCredentialException) {
            return new CredentialsExpiredException(e.getMessage());
        }
        if (e instanceof InactiveAccountException) {
            return new DisabledException(e.getMessage(), e);
        }
        return new BadCredentialsException(e.getMessage(), e);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
