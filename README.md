ManyWho Identity Service
========================

> This service is currently in development, and not yet recommended for use in production environments

[![Build Status](https://travis-ci.org/manywho/service-identity.svg)](https://travis-ci.org/manywho/service-identity)

This service offers a very basic identity provider for your flows, giving support for authentication and a
tenant-specific user and group directory.

## Usage

If you need to, it's easy to spin up your own instance of the service if you follow these instructions:

### Database

You will find the required schemas for the supported databases in the [`src/main/sql`](src/main/sql) folder.

### Configuring

The available configuration settings for the application are:

* **DATABASE_URL:** A JDBC connection string, e.g. `jdbc:postgresql://localhost/service-identity`
* **DATABASE_USERNAME**
* **DATABASE_PASSWORD**

#### Environment Variables

You will have to configure the application at runtime by using environment variables, so you will need to run the
application like this:

```bash
$ DATABASE_URL=jdbc:postgresql://localhost/service-identity DATABASE_USERNAME=postgres DATABASE_PASSWORD=password java -jar target/identity-*.jar
```

### Building

To build the application, you will need to have Maven 3 and a Java 8 implementation installed (OpenJDK and Oracle Java SE
are both supported).

Now you can build the runnable shaded JAR:

```bash
$ mvn clean package
```

### Running

The application is a RestEASY JAX-RS application, that by default is run under the Jetty server on port 8080 (if you
use the packaged JAR).

#### Defaults

Running the following command will start the service listening on `0.0.0.0:8080`:

```bash
$ java -jar target/identity-*.jar
```

#### Heroku

The service is compatible with Heroku, and can be deployed by clicking the button below:

[![Deploy to Heroku](https://www.herokucdn.com/deploy/button.svg)](https://heroku.com/deploy?template=https://github.com/manywho/service-identity)

## Contributing

Contributions are welcome to the project - whether they are feature requests, improvements or bug fixes! Refer to 
[CONTRIBUTING.md](CONTRIBUTING.md) for our contribution requirements.

## License

This service is released under the [MIT License](https://opensource.org/licenses/MIT).