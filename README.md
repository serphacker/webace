# WebAce java HTTP client 

[![build status](https://gitlab.com/serphacker/webace/badges/master/pipeline.svg)](https://gitlab.com/serphacker/webace/commits/master) 
[![coverage report](https://gitlab.com/serphacker/webace/badges/master/coverage.svg)](https://gitlab.com/serphacker/webace/commits/master)
[![code quality](https://api.codacy.com/project/badge/Grade/a1b564ef434c41e08af674bbec05b186)](https://www.codacy.com/app/noguespi/webace?utm_source=gitlab.com&amp;utm_medium=referral&amp;utm_content=serphacker/webace&amp;utm_campaign=Badge_Grade)
[![Maven Central](https://img.shields.io/maven-central/v/com.serphacker.webace/webace.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.serphacker.webace%22%20AND%20a:%22webace%22)

WebAce is a java HTTP client for web scraping with great proxy support based on [Apache HttpComponents](https://hc.apache.org/index.html). 

Homepage :  https://gitlab.com/serphacker/webace

Issues and bug report : https://gitlab.com/serphacker/webace/issues

Features : 

* Designed for web scraping, form submission and web automation (bots, link building, etc.)
* Support for HTTP and Socks proxy with authentication
* Support multiple sources IP (BindProxy)
* Proxy rotation
* Cookie management
* Easy bypass of SSL/TLS verification (works well with debugging proxy like [burp](https://portswigger.net/burp) and [ZAP](https://www.owasp.org/index.php/OWASP_Zed_Attack_Proxy_Project))
* Great form submission and charsets handling
* HTTP/2
* Java 11
 
 ## Install
 
 [![Maven Central](https://img.shields.io/maven-central/v/com.serphacker.webace/webace.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.serphacker.webace%22%20AND%20a:%22webace%22)
 (require java minimal version 11 )

 ```xml
<dependency>
  <groupId>com.serphacker.webace</groupId>
  <artifactId>webace</artifactId>
  <version>LATEST</version>
</dependency>
```
 
 ## Usage
 
 TODO
 
 ## Build

### Building jar

`mvn clean package`

### Running integration tests

1. Copy `docker/webace.env.template` to `docker/webace_it.env`
2. In `docker` directory, run `./docker-compose.sh --env webace_it.env up`. It will starts all the required container
(httpbin server, squid proxies, socks proxies, etc.).
3. Back in base directory, run the integration tests with the following command : 
`mvn -P integration-test -DhttpBinDomain=172.29.1.1 -Dtest.service-backend=docker-compose clean verify`

## License

The MIT License (MIT)