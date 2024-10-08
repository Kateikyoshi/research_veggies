# Why
A small project to:
1) Solidify Spring Security knowledge about how to create users using OAuth2 and secure endpoints.
2) Solidify frontend thymeleaf + bootstrap skills

# What is this
In short, this service allows to use omnipresent admin user or register a new user to save and check vegetables in DB.
Users have lower scope than admin. Every user gets JWT. Can't use roles with reactive thymeleaf, though.
So, I don't use roles or JWT for anything. Too lazy.

# How to use
## To start locally
If you start locally embedded h2 will be used.
To start go to VeggiesApplication.kt and launch with Intellij.
You can just go to localhost:8080 in your browser and use the service.
## To start with docker
Docker version uses postgres container.
Backend and DB start using docker compose.
To start use 'docker compose up'.
To stop use 'docker compose stop'.
Since "host" network mode is used, you can just directly
access your running docker containers from host.
It means you just go to the browser with localhost:8080 and
you will be able to access the service.
### If you f'ed up with docker
If you messed up and want to recreate everything, then
rebuild with gradle 'gradle build'. Then forcefully recreate containers
using 'docker compose up --build --force-recreate'. If DB wasn't initialized
correctly, then you will also have to kill its volume before all that.