# Hango API Gateway Portal
[![Hango gateway CI](https://github.com/hango-io/portal/actions/workflows/maven.yml/badge.svg)](https://github.com/hango-io/portal/actions/workflows/maven.yml)

## About
Hango gateway is a high-performance, cloud-native, next-generation API Gateway. Hango portal is the backend of Hango UI.
Hango portal, responsible for org.hango.cloud.dashboard.meta data manaagement, service,route,plugin configuration, etc...

Hango portal provides open api, you can call open api to create service, route, and configuration.

## Project structure
```shell script
.
├── gateway-api
├── common-infra
└── envoy-infra
    ├── Dockerfile
    ├── src
    └── pom.xml 
├── LICENSE
├── README.md
└── pom.xml
```
gateway-portal directory is used to provide api to hango ui module.

## Develop
Using maven
```shell script
$ git clone git@github.com:hango-io/portal.git
$ cd portal
$ mvn clean package
```

## Contributing
If you wish to contribute to Hango API Gateway, please read the root projects' contributing files.

## License
[Apache-2.0](https://choosealicense.com/licenses/apache-2.0/)