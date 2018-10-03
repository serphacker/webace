# WebAxe java HTTP client [![build status](https://gitlab.com/serphacker/webaxe/badges/master/pipeline.svg)](https://gitlab.com/serphacker/webaxe/commits/master) [![coverage report](https://gitlab.com/serphacker/webaxe/badges/master/coverage.svg)](https://gitlab.com/serphacker/webaxe/commits/master)

[WebAxe is a java HTTP client](https://gitlab.com/serphacker/webaxe) for web scraping with great proxy support based on [Apache HttpComponents](https://hc.apache.org/index.html). 

Homepage :  https://gitlab.com/serphacker/webaxe

Issues and bug report : https://gitlab.com/serphacker/webaxe/issues

Features : 

* Designed for web scraping, link building and form submission
* Support for HTTP and Socks proxy with authentication
* Support multiple sources IP (BindProxy)
* Proxy rotation
* Cookie management
* Easy bypass of SSL/TLS verification (works well with debugging proxy like [burp](https://portswigger.net/burp) and [ZAP](https://www.owasp.org/index.php/OWASP_Zed_Attack_Proxy_Project))
* Great form submission and charsets handling
* HTTP/2
* Java 11
 
 ## Install
 
 Require java 11
 
 Maven: 
 
 ```xml
<dependency>
  <groupId>com.serphacker.webaxe</groupId>
  <artifactId>webaxe</artifactId>
  <version>LATEST</version>
</dependency>
```

Gradle:

```text
compile 'com.serphacker.webaxe:webaxe:LATEST'
```
 
 ## Usage
 
 TODO
 
 ## Build

TODO

## License

The MIT License (MIT)