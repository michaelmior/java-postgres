## Getting started

To execute a simple test program against the `foodmart` dataset, this assumes you have a correctly configured [password file](https://www.postgresql.org/docs/current/static/libpq-pgpass.html) and a schema named `foodmart` which contains the data.
Simple run `./gradlew run` to execute a query against the data.
The only dependency required for your own programs is the PostgreSQL java driver.
If you choose, you can [download the JAR file](http://central.maven.org/maven2/org/postgresql/postgresql/42.2.5/) for use in your own projects, but you can also use a build system like [Gradle](https://gradle.org/) used by this project.
For generating test data, some programs also make use of the [JFairy](https://www.javadoc.io/doc/io.codearte.jfairy/jfairy/0.5.9) library.

## Other programs

The repository contains several different programs.
By default the program `SQLTest` is run.
To run another program, execute `./gradlew -PmainClass=PROGRAM_NAME`.

| Program   | Description                                      |
|-----------|--------------------------------------------------|
| `SQLFake` | Populate the `hospital` schema with sample data  |
| `SQLTest` | Run a simple query against the `foodmart` schema |
