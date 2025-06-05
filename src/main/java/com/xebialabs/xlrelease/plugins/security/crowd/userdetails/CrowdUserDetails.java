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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.atlassian.crowd.embedded.api.Attributes;
import com.atlassian.crowd.embedded.impl.ImmutableAttributes;
import com.atlassian.crowd.model.user.ImmutableUser;
import com.atlassian.crowd.model.user.User;
import com.google.common.base.MoreObjects;

public class CrowdUserDetails implements UserDetails {
    private static final Attributes EMPTY_ATTRIBUTES = new EmptyAttributes();
    private final User principal;
    private final Attributes attributes;
    private final Collection<GrantedAuthority> authorities;

    public CrowdUserDetails(User principal, Collection<GrantedAuthority> authorities) {
        this(principal, principal instanceof Attributes ? (Attributes) principal : EMPTY_ATTRIBUTES, authorities);
    }

    private CrowdUserDetails(User principal, Attributes attributes, Collection<GrantedAuthority> authorities) {
        this.principal = ImmutableUser.from(principal);
        this.attributes = new ImmutableAttributes(attributes);
        this.authorities = MoreObjects.firstNonNull(authorities, Collections.emptySet());
    }

    public Collection<GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    public User getRemotePrincipal() {
        return this.principal;
    }

    public String getPassword() {
        throw new UnsupportedOperationException("Not giving you the password");
    }

    public String getUsername() {
        return this.principal.getName();
    }

    public boolean isAccountNonExpired() {
        return true;
    }

    public boolean isAccountNonLocked() {
        return true;
    }

    public boolean isCredentialsNonExpired() {
        return true;
    }

    public boolean isEnabled() {
        return this.principal.isActive();
    }

    public String getFirstName() {
        return this.principal.getFirstName();
    }

    public String getLastName() {
        return this.principal.getLastName();
    }

    public String getEmail() {
        return this.principal.getEmailAddress();
    }

    public String getAttribute(String attributeName) {
        return this.attributes.getValue(attributeName);
    }

    public String getFullName() {
        return this.principal.getDisplayName();
    }

    public boolean hasAuthority(Predicate<GrantedAuthority> authorityPredicate) {
        return this.authorities.stream().anyMatch(authorityPredicate);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            CrowdUserDetails that = (CrowdUserDetails) o;
            return Objects.equals(this.principal, that.principal) && Objects.equals(this.attributes, that.attributes) && Objects.equals(this.authorities, that.authorities);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.principal, this.attributes, this.authorities});
    }

    private static class EmptyAttributes implements Attributes, Serializable {
        private EmptyAttributes() {
        }

        public Set<String> getKeys() {
            return Collections.emptySet();
        }

        public String getValue(String arg0) {
            return null;
        }

        public Set<String> getValues(String arg0) {
            return Collections.emptySet();
        }

        public boolean isEmpty() {
            return true;
        }
    }
}
