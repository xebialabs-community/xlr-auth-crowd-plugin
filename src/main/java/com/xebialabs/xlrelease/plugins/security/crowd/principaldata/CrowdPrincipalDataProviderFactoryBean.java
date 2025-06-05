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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.xebialabs.xlrelease.plugins.security.crowd.authentication.XLCrowdAuthenticationProvider;
import com.xebialabs.xlrelease.plugins.security.crowd.userdetails.CrowdUserDetailsServiceImpl;
import com.xebialabs.xlrelease.principaldata.DefaultUserDataProvider;
import com.xebialabs.xlrelease.principaldata.PrincipalDataProvider;
import com.xebialabs.xlrelease.principaldata.PrincipalDataProviderImpl;
import com.xebialabs.xlrelease.principaldata.UserDataProvider;

public class CrowdPrincipalDataProviderFactoryBean implements FactoryBean<PrincipalDataProvider>, ApplicationContextAware, InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(CrowdPrincipalDataProviderFactoryBean.class);

    private ApplicationContext context;

    private PrincipalDataProvider provider;

    @Override
    public void afterPropertiesSet() throws Exception {
        PrincipalDataProviderImpl principalDataProvider = new PrincipalDataProviderImpl();

        DefaultUserDataProvider defaultProvider = new DefaultUserDataProvider();
        // 1. Local user data provider has highest priority
        principalDataProvider.addUserProvider(defaultProvider);

        // 2. Crowd user data provider
        List<UserDataProvider> crowdUserDataProviderList = tryInitUserDataFromSpringCrowdProvider();
        if (!crowdUserDataProviderList.isEmpty()) {
            principalDataProvider.addUserProvider(crowdUserDataProviderList);
        }

        this.provider = principalDataProvider;
    }

    @Override
    public PrincipalDataProvider getObject() throws Exception {
        return provider;
    }

    @Override
    public Class<?> getObjectType() {
        return PrincipalDataProvider.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }


    /**
     * Tries to obtain an Crowd connection from a "standard" Spring Security
     * configuration
     * <p>
     * Due to the Spring implementation, this requires some reflection.
     */
    private List<UserDataProvider> tryInitUserDataFromSpringCrowdProvider() {
        Map<String, CrowdUserDetailsServiceImpl> crowdUserDetailServiceImpls = context
                .getBeansOfType(CrowdUserDetailsServiceImpl.class);
        List<UserDataProvider> crowdUserDataProviderList = new ArrayList<>();
        if (!crowdUserDetailServiceImpls.isEmpty()) {
            try {
                logger.info("Found {} in spring context, initializing from Crowd",
                        CrowdUserDetailsServiceImpl.class.getSimpleName());
                crowdUserDetailServiceImpls.forEach((name, crowdUserDetailServiceImpl) -> crowdUserDataProviderList
                        .add(new CrowdUserDataProvider(crowdUserDetailServiceImpl)));
            } catch (Exception e) {
                logger.warn("Error initializing from {}", XLCrowdAuthenticationProvider.class.getSimpleName(), e);
            }
        }
        return crowdUserDataProviderList;
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }
}
