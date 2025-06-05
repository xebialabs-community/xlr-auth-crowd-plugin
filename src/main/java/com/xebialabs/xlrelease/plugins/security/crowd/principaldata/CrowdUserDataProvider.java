/**
 * Copyright 2025 DIGITAL.AI
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.xebialabs.xlrelease.plugins.security.crowd.principaldata;

import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.xebialabs.xlrelease.plugins.security.crowd.userdetails.CrowdUserDetails;
import com.xebialabs.xlrelease.plugins.security.crowd.userdetails.CrowdUserDetailsServiceImpl;
import com.xebialabs.xlrelease.principaldata.UserData;
import com.xebialabs.xlrelease.principaldata.UserDataProvider;


/**
 * Retrieves email addresses and full names from an crowd directory, reusing the
 * spring security configuration.
 */
public class CrowdUserDataProvider implements UserDataProvider {

    private static final Logger logger = LoggerFactory.getLogger(CrowdUserDataProvider.class);
    private final CrowdUserDetailsServiceImpl crowdUserDetailsServiceImpl;

    public CrowdUserDataProvider(CrowdUserDetailsServiceImpl crowdUserDetailsService) {
        this.crowdUserDetailsServiceImpl = crowdUserDetailsService;
    }

    @Override
    public UserData getUserData(String username) {
        try {
            CrowdUserDetails crowdUserDetails = crowdUserDetailsServiceImpl.loadUserByUsername(username);
            if (crowdUserDetails == null) {
                return UserData.NOT_FOUND;
            }
            String email = crowdUserDetails.getEmail();
            String fullName = crowdUserDetails.getFullName();
            logger.info("Email: {} and displayName: {} have been read from Crowd", email, fullName);
            return new UserData(email, fullName);
        } catch (UsernameNotFoundException e) {
            logger.info("User with username: {} not found", username);
            return UserData.NOT_FOUND;
        } catch (DataAccessException e) {
            logger.warn("Could not get data from Crowd Server for User with username: {}", username);
            return UserData.NOT_FOUND;
        } catch (Exception e) {
            logger.warn("Error accessing Crowd server", e);
            return UserData.NOT_FOUND;
        }
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities(final String username) {
        try {
            CrowdUserDetails crowdUserDetails = crowdUserDetailsServiceImpl.loadUserByUsername(username);
            if (crowdUserDetails == null) {
                return AuthorityUtils.NO_AUTHORITIES;
            }
            return crowdUserDetails.getAuthorities();
        } catch (UsernameNotFoundException e) {
            logger.info("User with username: {} not found", username);
            return AuthorityUtils.NO_AUTHORITIES;
        } catch (DataAccessException e) {
            logger.warn("Could not get data from Crowd Server for User with username: {}", username);
            return AuthorityUtils.NO_AUTHORITIES;
        } catch (Exception e) {
            logger.warn("Error accessing Crowd server", e);
            return AuthorityUtils.NO_AUTHORITIES;
        }
    }
}
