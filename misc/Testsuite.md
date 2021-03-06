Executing testsuite
===================

Browser
-------

The testsuite uses Sellenium. By default it uses the HtmlUnit WebDriver, but can also be executed with Chrome or Firefox.

To run the tests with Firefox add `-Dbrowser=firefox` or for Chrome add `-Dbrowser=chrome`

Database
--------

By default the testsuite uses an embedded H2 database to test with other databases see (Database Testing)[DatabaseTesting.md].

Test utils
==========

Keycloak server
---------------

To start a basic Keycloak server for testing run:

    mvn exec:java -Pkeycloak-server
    
or run org.keycloak.testsuite.KeycloakServer from your favourite IDE!
     
When starting the server it can also import a realm from a json file:

    mvn exec:java -Pkeycloak-server -Dimport=testrealm.json
    
### Live edit of html and styles

The Keycloak test server can load resources directly from the filesystem instead of the classpath. This allows editing html, styles and updating images without restarting the server. To make the server use resources from the filesystem start with:

    mvn exec:java -Pkeycloak-server -Dresources
    
You can also specify the theme directory used by the server with:

    mvn exec:java -Pkeycloak-server -Dkeycloak.theme.dir=<PATH TO THEMES DIR>
    
For example to use the example themes run the server with:

    mvn exec:java -Pkeycloak-server -Dkeycloak.theme.dir=examples/themes
    
**NOTE:** If `keycloak.theme.dir` is specified the default themes (base, rcue and keycloak) are loaded from the classpath

### Run server with Mongo model

To start a Keycloak server with identity model data persisted in Mongo database instead of default JPA/H2 you can run:

    mvn exec:java -Pkeycloak-server -Dkeycloak.realm.provider=mongo -Dkeycloak.user.provider=mongo -Dkeycloak.audit.provider=mongo

By default it's using database `keycloak` on localhost/27017 and it uses already existing data from this DB (no cleanup of existing data during bootstrap). Assumption is that you already have DB running on localhost/27017 . Use system properties to configure things differently:

    mvn exec:java -Pkeycloak-server -Dkeycloak.realm.provider=mongo -Dkeycloak.user.provider=mongo -Dkeycloak.eventStore.provider=mongo -Dkeycloak.connectionsMongo.host=localhost -Dkeycloak.connectionsMongo.port=27017 -Dkeycloak.connectionsMongo.db=keycloak -Dkeycloak.connectionsMongo.clearOnStartup=false

Note that if you are using Mongo model, it would mean that Mongo will be used for audit as well. You may need to use audit related properties for configuration of Mongo if you want to override default ones (For example keycloak.audit.mongo.host, keycloak.audit.mongo.port etc)

TOTP codes
----------

To generate totp codes without Google authenticator run:

    mvn exec:java -Ptotp
    
or run org.keycloak.testsuite.TotpGenerator from your favourite IDE!

Once started copy/paste the totp secret and press enter. To use a new secret just copy/paste and press enter again.

Mail server
-----------

To start a test mail server for testing email sending run:

    mvn exec:java -Pmail-server
    
or run org.keycloak.testsuite.MailServer from your favourite IDE!

To configure Keycloak to use the above server add the following system properties:

    keycloak.mail.smtp.from=auto@keycloak.org
    keycloak.mail.smtp.host=localhost
    keycloak.mail.smtp.port=3025
    
For example if using the test utils Keycloak server start it with:

    mvn exec:java -Pkeycloak-server -Dkeycloak.mail.smtp.from=auto@keycloak.org -Dkeycloak.mail.smtp.host=localhost -Dkeycloak.mail.smtp.port=3025
    
LDAP server
-----------

To start a ApacheDS based LDAP server for testing LDAP sending run:
    
    mvn exec:java -Pldap
    
There are additional system properties you can use to configure (See LDAPEmbeddedServer class for details). Once done, you can create LDAP Federation provider
in Keycloak admin console with the settings like:
* Vendor: Other
* Connection URL: ldap://localhost:10389
* User DN Suffix: ou=People,dc=keycloak,dc=org
* Bind DN: uid=admin,ou=system
* Bind credential: secret

Kerberos server
---------------

To start a ApacheDS based Kerberos server for testing Kerberos + LDAP sending run:
    
    mvn exec:java -Pkerberos
    
There are additional system properties you can use to configure (See LDAPEmbeddedServer and KerberosEmbeddedServer class for details) but for testing purposes default values should be good.
By default ApacheDS LDAP server will be running on localhost:10389 and Kerberos KDC on localhost:6088 . 

Once kerberos is running, you can create LDAP Federation provider in Keycloak admin console with same settings like mentioned in previous LDAP section. 
But additionally you can enable Kerberos authentication in LDAP provider with the settings like:

* Kerberos realm: KEYCLOAK.ORG
* Server Principal: HTTP/localhost@KEYCLOAK.ORG
* KeyTab: $KEYCLOAK_SOURCES/testsuite/integration/src/test/resources/kerberos/http.keytab (Replace $KEYCLOAK_SOURCES with correct absolute path of your sources)

Once you do this, you should also ensure that your Kerberos client configuration file is properly configured with KEYCLOAK.ORG domain. 
See [../testsuite/integration/src/test/resources/kerberos/test-krb5.conf](../testsuite/integration/src/test/resources/kerberos/test-krb5.conf) for inspiration. The location of Kerberos configuration file 
is platform dependent (In linux it's file `/etc/krb5.conf` )

Then you need to configure your browser to allow SPNEGO/Kerberos login from `localhost` .

Exact steps are again browser dependent. For Firefox see for example [http://www.microhowto.info/howto/configure_firefox_to_authenticate_using_spnego_and_kerberos.html](http://www.microhowto.info/howto/configure_firefox_to_authenticate_using_spnego_and_kerberos.html) . 
URI `localhost` must be allowed in `network.negotiate-auth.trusted-uris` config option. 

For Chrome, you just need to run the browser with command similar to this (more details in Chrome documentation):

```
/usr/bin/google-chrome-stable --auth-server-whitelist="localhost"
```


Finally test the integration by retrieve kerberos ticket. In many OS you can achieve this by running command from CMD like:
                                          
```
kinit hnelson@KEYCLOAK.ORG
```
                        
and provide password `secret`

Now when you access `http://localhost:8081/auth/realms/master/account` you should be logged in automatically as user `hnelson` . 


