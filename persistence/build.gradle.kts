plugins {
    `data-collector-common`
    `java-library`
    id("nu.studer.jooq")
}

dependencies {
    jooqGenerator(platform(project(":build-platform")))
    jooqGenerator("org.postgresql:postgresql")
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