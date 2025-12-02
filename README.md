Full Startup Starter (FSS)
---

This is a collection of projects to bootstrap a startup ( or a project ) by providing complete and productive
development stack paired with methodologies like
 API first and Executable Specifications, a.k.a ATDD (Acceptance Test Driven Development) 

![overview](full-stack-dev/docs/Full-Startup-Starter.png)

This Medium article:[ Full Startup Starter technology stack](https://medium.com/@kgignatyev/full-startup-starter-on-jvm-stack-c83a4f0a28ad) explains the reasons
and ideas for the project


[GETTING STARTED](GETTING_STARTED.md)

## Projects
- [fss-api](fss-api/README.md) - API definitions
- [fss-api-server-stubs](fss-api-server-stubs/README.md) - builds library of server sides stubs based on API definitions
- [fss-sboot-service](fss-sboot-service/README.md) - very simple service that implements the API
- [fss-ui](fss-ui/README.md) - Angular based UI that uses script to generate client side code for the API
- [fss-acceptance-tests](fss-acceptance-tests/README.md) - acceptance tests that java client to test the service


## Overview



All the parts of this stack are productive and working well together, however, they are replaceable.
If one hates Angular then React can be used (but use of commercial UI libraries is the must for productivity!).

![overview](full-stack-dev/docs/fss-overview.png)

## Sample screens
Jobs
![jobs](full-stack-dev/docs/jobs-list.png)
Company response
![company response](full-stack-dev/docs/company-response.png)
Login with Auth0
<br/>
![login](full-stack-dev/docs/login.png)



