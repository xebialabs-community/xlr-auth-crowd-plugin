/**
 * Copyright 2025 DIGITAL.AI
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.xebialabs.xlrelease.plugins.security.crowd.userdetails;

import java.util.*;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.model.user.UserWithAttributes;
import com.atlassian.crowd.service.client.CrowdClient;
import com.atlassian.crowd.user.UserAuthoritiesProvider;

import com.xebialabs.xlrelease.plugins.security.crowd.exception.CrowdDataAccessException;

public class CrowdUserDetailsServiceImpl implements CrowdUserDetailsService {
    private CrowdClient crowdClient;
    private String authorityPrefix = "";
    private Iterable<Map.Entry<String, String>> groupToAuthorityMappings;
    private String adminAuthority = "ROLE_SYS_ADMIN";
    private UserAuthoritiesProvider userAuthoritiesProvider;

    public CrowdUserDetailsServiceImpl() {
    }

    public CrowdUserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
        try {
            UserWithAttributes principal = this.crowdClient.getUserWithAttributes(username);
            return new CrowdUserDetails(principal, this.getAuthorities(principal.getName()));
        } catch (UserNotFoundException e) {
            throw new UsernameNotFoundException("Could not find principal in Crowd with username: " + username, e);
        } catch (OperationFailedException | InvalidAuthenticationException | ApplicationPermissionException e) {
            throw new CrowdDataAccessException(e);
        }
    }

    Collection<GrantedAuthority> getAuthorities(String username) throws UserNotFoundException, OperationFailedException, InvalidAuthenticationException, ApplicationPermissionException {
        if (this.userAuthoritiesProvider == null) {
            if (this.groupToAuthorityMappings == null) {
                List<String> userGroups = this.crowdClient.getNamesOfGroupsForNestedUser(username, 0, -1);
                return this.generateAuthoritiesFromGroupNames(userGroups);
            } else {
                return this.generateAuthorityFromMap(username);
            }
        } else {
            List<GrantedAuthority> authorities = new ArrayList();

            for(String authority : this.userAuthoritiesProvider.getAuthorityNames(username)) {
                authorities.add(new SimpleGrantedAuthority(authority));
            }

            return authorities;
        }
    }

    private Set<GrantedAuthority> generateAuthorityFromMap(String username) throws OperationFailedException, ApplicationPermissionException, InvalidAuthenticationException {
        Set<GrantedAuthority> authorities = new HashSet<>();

        for(Map.Entry<String, String> groupToAuthorityMapEntry : this.groupToAuthorityMappings) {
            if (this.crowdClient.isUserNestedGroupMember(username, (String)groupToAuthorityMapEntry.getKey())) {
                authorities.add(new SimpleGrantedAuthority((String)groupToAuthorityMapEntry.getValue()));
            }
        }

        return authorities;
    }

    private List<GrantedAuthority> generateAuthoritiesFromGroupNames(List<String> userGroups) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        for (final String userGroup : userGroups) {
            String var10003 = this.getAuthorityPrefix();
            authorities.add(new SimpleGrantedAuthority(var10003 + userGroup));
        }

        return authorities;
    }

    public String getAuthorityPrefix() {
        return this.authorityPrefix;
    }

    public void setAuthorityPrefix(String authorityPrefix) {
        this.authorityPrefix = authorityPrefix;
    }

    public Iterable<Map.Entry<String, String>> getGroupToAuthorityMappings() {
        return this.groupToAuthorityMappings;
    }

    public void setGroupToAuthorityMappings(Iterable<Map.Entry<String, String>> groupToAuthorityMappings) {
        this.groupToAuthorityMappings = groupToAuthorityMappings;
    }

    public void setUserAuthoritiesProvider(UserAuthoritiesProvider userAuthoritiesProvider) {
        this.userAuthoritiesProvider = userAuthoritiesProvider;
    }

    public String getAdminAuthority() {
        return this.adminAuthority;
    }

    public void setAdminAuthority(String adminAuthority) {
        this.adminAuthority = adminAuthority;
    }

    public void setCrowdClient(CrowdClient crowdClient) {
        this.crowdClient = crowdClient;
    }
}
