# bt-dev-prod-data-collector

This application is deployed in Heroku with app id [`btdevprod-exporter`](https://dashboard.heroku.com/apps/btdevprod-exporter).

The application has a PostgreSQL database attached to the dyno as a [Heroku add-on](https://dashboard.heroku.com/apps/btdevprod-exporter/resources). In order to monitor it, you need "operate" permissions on Heroku for this app.

You can find more information on the application using the Heroku CLI. Run `heroku info`.

## Health

This is a Spring Boot application, instrumented with Health actuators. Find the health state by visiting the endpoint `/actuator/health`.

## Build Scan Link Unfurler for Slack

This application serves as backend for a Slack App installed within the Gradle Slack workspace that relies on the [Slack Events API](https://api.slack.com/apis/connections/events-api) to then unfurl Develocity Build Scan links and expand them into [Block Kit](https://api.slack.com/block-kit) based messages.

Find the source code for the events API handling and build scan data expansion and rendering under the [application module](./application/src/main/kotlin/org/gradle/devprod/collector/).

## Deploying changes

After merging changes to the repository, follow the Heroku guide to [deploy them via its git integration](devcenter.heroku.com/articles/git) (note: you will need an account in heroku.com to generate a personal access token).

```shell
git push heroku HEAD:main
```

It's worth noting that Heroku doesn't care which branch you're pushing to, i.e. `git push heroku HEAD:whatever-branch` will trigger deploying. This can be useful if you want to deploy your personal branch for testing.
