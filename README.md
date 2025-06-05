# Digital.ai Release Auth Crowd Plugin

[![License: MIT][xlr-auth-crowd-plugin-license-image]][xlr-auth-crowd-plugin-license-url]
![Github All Releases][xlr-auth-crowd-plugin-downloads-image]

This project is a plugin for [Digital.ai Release](https://digital.ai/products/release) that enables authentication and user management via Atlassian Crowd. It provides integration with Crowd for user authentication within Digital.ai Release.

## How to Build

This project uses Gradle for building. To build the plugin, run:

```
./gradlew clean build
```

The built JAR file will be located in `build/libs/`.

## Tested Digital.ai Release Version

This plugin was tested against **Digital.ai Release 24.3**. Compatibility with other versions is not guaranteed.

## Cautions

- Ensure your Crowd server is properly configured and accessible from the Digital.ai Release server.
- This plugin may not be compatible with future or earlier versions of Digital.ai Release.
- Always test in a non-production environment before deploying to production.
- Review and configure security settings according to your organization's policies.

## Installation

### Step 1 - Configure Atlassian Crowd to communicate with the Release Application

To configure Atlassian Crowd to receive authentication requests from Release:

1. [Add Release application](https://confluence.atlassian.com/crowd/adding-an-application-18579591.html#AddinganApplication-add) to Atlassian Crowd.
2. [Add](https://confluence.atlassian.com/crowd/adding-a-directory-18579549.html) and [configure](https://confluence.atlassian.com/crowd/mapping-a-directory-to-an-application-18579599.html) the directories that are visible to Release.
3. [Add](https://confluence.atlassian.com/crowd/adding-a-group-20807693.html) and [map](https://confluence.atlassian.com/crowd/specifying-which-groups-can-access-an-application-25788430.html) the groups which that will authenticate with Release.

For more information, see [Adding an Application](https://confluence.atlassian.com/crowd/adding-an-application-18579591.html).

### Step 2 - Configure Release to use an Atlassian Crowd

1. Download the latest JAR file from the [Releases](https://github.com/xebialabs-community/xlr-auth-crowd-plugin/releases) page.
2. Copy the JAR file into the following directory on your Digital.ai Release server:
   ```
   DAI_RELEASE_SERVER/plugins/__local__
   ```
3. To configure Release to use an Atlassian Crowd, modify the `xl-release-security.xml` security configuration file. This following is an example `xl-release-security.xml` file:
```xml
<?xml version="1.0" encoding="UTF-8"?>

<beans xmlns="http://www.springframework.org/schema/beans" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:security="http://www.springframework.org/schema/security"
       xsi:schemaLocation="
        http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/security http://www.springframework.org/schema/security/spring-security.xsd
    ">
    <import resource="xl-crowd-security.xml"/>

    <bean id="crowdUserDetailsService" class="com.xebialabs.xlrelease.plugins.security.crowd.userdetails.CrowdUserDetailsServiceImpl">
        <property name="crowdClient" ref="crowdClient"/>
        <property name="authorityPrefix" value=""/>
    </bean>

    <bean id="crowdAuthenticationProvider" class="com.xebialabs.xlrelease.plugins.security.crowd.authentication.XLCrowdAuthenticationProvider">
        <constructor-arg ref="crowdClient"/>
        <constructor-arg ref="crowdUserDetailsService"/>
    </bean>

    <bean id="rememberMeAuthenticationProvider" class="com.xebialabs.deployit.security.authentication.RememberMeAuthenticationProvider"/>

    <security:authentication-manager alias="authenticationManager">
        <security:authentication-provider ref="rememberMeAuthenticationProvider" />
        <security:authentication-provider ref="xlAuthenticationProvider"/>
        <security:authentication-provider ref="crowdAuthenticationProvider"/>
    </security:authentication-manager>

</beans>
```

### Step 3 - Add the cache configuration file to your Release directory

Copy the following file into your `XL_RELEASE_SERVER_HOME/conf` directory:

| Copy From                          | Copy To                                      |
|-------------------------------------|----------------------------------------------|
| `CROWD/client/conf/crowd-ehcache.xml` | `XL_RELEASE_SERVER_HOME/conf/crowd-ehcache.xml` |

This file can be adjusted to change the cache behavior.

### Step 4 - Configure the Atlassian Crowd Spring Security connector properties

The Atlassian Crowd Spring Security connector must be configured with the details of the Atlassian Crowd server.

1. Copy the default `crowd.properties` file into your `XL_RELEASE_SERVER_HOME/conf` directory:

| Copy From                          | Copy To                                      |
|-------------------------------------|----------------------------------------------|
| `CROWD/client/conf/crowd.properties` | `XL_RELEASE_SERVER_HOME/conf/crowd.properties` |

2. Edit `crowd.properties` and populate the following fields appropriately:

| Key                     | Value                                                                                                                                |
|-------------------------|--------------------------------------------------------------------------------------------------------------------------------------|
| application.name        | Use the same application name that you used when adding the application to Atlassian Crowd.                                          |
| application.password    | Use the same application password that you used when adding the application to Atlassian Crowd.                                      |
| crowd.server.url        | URL to use when connecting with the integration libraries that are used to communicate with the Atlassian Crowd server, e.g. http://localhost:8095/crowd/services/. |
| session.validationinterval | Time interval (in minutes) between requests to validate whether the user is logged in or out of Atlassian Crowd. Set to 0 for validation on each request. Setting to 1 or higher increases performance. |

For more information, see [crowd.properties](https://confluence.atlassian.com/crowd/the-crowd-properties-file-98665664.html).

Restart the Digital.ai Release server after all changes.

## Example team security setup
You can setup a Atlassian Crowd group called `devs` to be used by the members of a team in Release. Assign this group to a role in Release called Developers. At folder or release level, you can add permissions for a team called Dev Team that contains the Release role Developers. This role contains the created Atlassian Crowd group called devs.

When you log in as a user into the devs group using Atlassian Crowd, you will have the permissions for the Developers role at folder or release level.

## License

This project is licensed under the [MIT License](LICENSE).

---

[xlr-auth-crowd-plugin-license-image]: https://img.shields.io/badge/License-MIT-yellow.svg
[xlr-auth-crowd-plugin-license-url]: https://opensource.org/licenses/MIT
[xlr-auth-crowd-plugin-downloads-image]: https://img.shields.io/github/downloads/xebialabs-community/xlr-auth-crowd-plugin/total?label=Downloads&color=blue
