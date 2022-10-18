plugins {
    `data-collector-common`
    `java-library`
    id("nu.studer.jooq")
    id("org.flywaydb.flyway")
}

dependencies {
    implementation(platform(project(":build-platform")))
    implementation("org.flywaydb:flyway-core")

    api("org.springframework.boot:spring-boot-starter-jooq")

    runtimeOnly("org.postgresql:postgresql")

    jooqGenerator(platform(project(":build-platform")))
    jooqGenerator("org.postgresql:postgresql")
}
// You need to run the image before running the flyway and jooq generation.
// Mind the port - make sure other postgres is not running on your host
// Run:
// docker run -d -p 5432:5432 -e POSTGRES_PASSWORD=btdevprod -e POSTGRES_USER=btdevprod -e POSTGRES_DB=btdevprod postgres
// ./gradlew persistence:flywayMigrate --info
// ./gradlew persistence:generateJooq --info
val localDbPort = 5432
val localDbUrl = "jdbc:postgresql://localhost:$localDbPort/btdevprod"
val localDbUsername = "btdevprod"
val localDbUserPassword = "btdevprod"

flyway {
    url = localDbUrl
    user = localDbUsername
    password = localDbUserPassword
}

jooq {
    configurations {
        create("main") {
            generateSchemaSourceOnCompilation.set(false)
            jooqConfiguration.apply {
                jdbc.apply {
                    driver = "org.postgresql.Driver"
                    url = localDbUrl
                    user = localDbUsername
                    password = localDbUserPassword
                }
                generator.apply {
                    name = "org.jooq.codegen.DefaultGenerator"
                    database.apply {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        inputSchema = "public"
                        excludes = "flyway_schema_history"
                    }
                    target.apply {
                        packageName = "org.gradle.devprod.collector.persistence.generated.jooq"
                        directory = "src/main/java"
                    }
                    strategy.name = "org.jooq.codegen.DefaultGeneratorStrategy"
                }
            }
        }
    }
}
