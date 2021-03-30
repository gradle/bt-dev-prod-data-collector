plugins {
    `java-library`
    id("nu.studer.jooq") version "5.2"
}

group = "org.gradle.boot-test"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

dependencies {
    jooqGenerator("org.postgresql:postgresql:42.2.19")
}

jooq {
    configurations {
        create("main") {
            generateSchemaSourceOnCompilation.set(false)
            jooqConfiguration.apply {
                jdbc.apply {
                    driver = "org.postgresql.Driver"
                    url = "jdbc:postgresql://localhost:5432/btdevprod"
                    user = "btdevprod"
                    password = "btdevprod"
                }
                generator.apply {
                    name = "org.jooq.codegen.DefaultGenerator"
                    database.apply {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        inputSchema = "public"
                        excludes = "flyway_schema_history"
                    }
                    target.apply {
                        packageName = "org.gradle.devprod.enterprise.export.generated.jooq"
                        directory = "src/main/java"
                    }
                    strategy.name = "org.jooq.codegen.DefaultGeneratorStrategy"
                }
            }
        }
    }
}