<p align="center">
  <img width="200px" src="images/ytsms-logo.png" title="YTSMS">
</p>
<p align="center">
  <img src="https://img.shields.io/github/checks-status/MDeLuise/ytsms/main?style=for-the-badge&label=build&color=%23DC143C" />
<img src="https://img.shields.io/github/v/release/MDeLuise/ytsms?style=for-the-badge&color=%23DC143C" />
</p>

<p align="center">YTSMS is a <b>self-hosted youtube subscription management service.</b><br>Useful to keep track of new uploaded video of the followed channels, even without having a Google account.</p>

<p align="center"><a href="https://github.com/MDeLuise/ytsms/#why">Why?</a> • <a href="https://github.com/MDeLuise/ytsms/#features-highlight">Features highlights</a> • <a href="https://github.com/MDeLuise/ytsms/#getting-started">Getting started</a> • <a href="https://github.com/MDeLuise/ytsms/#configuration">Configuration</a> • <a href="https://github.com/MDeLuise/ytsms/#faq">FAQ</a></p>

<p align="center">
  <img src="/images/screenshot-desktop.png" width="45%" />
  <img src="/images/screenshot-mobile.png" width="45%" /> 
</p>

## Why?
I've always enjoyed spending some time viewing YouTube videos. The problem is that YouTube keeps track of a lot of data about you.

Ytsms takes care of this problem allowing you to get updated about new videos from followed channels without having a Google account or having it but using the official API (that is still better then using directly the subscription system of the Google account, since you can view the video logged out).

## Features highlight
* Add channels to followed list
* Get notified about every new video published by the followed channels
* Filter videos by channel
* Chose between YouTube backend or an [Invidious](https://invidious.io/) instance
* Dark/Light mode
* 🔜 Create playlists of videos

## Getting started
YTSMS provides multiple ways of installing it on your server.
* [Setup with Docker](https://www.ytsms.org/docs/v1/setup/setup-with-docker/) (_recommended_)
* [Setup without Docker](https://www.ytsms.org/docs/v1/setup/setup-without-docker/)

### Setup with docker
Working with Docker is pretty straight forward. To make things easier, a [docker compose file](#) is provided in the repository which contain all needed services, configured to just run the application right away.

There are two different images for the service:
* `msdeluise/ytsms-backend`
* `msdeluise/ytsms-frontend`

This images can be use indipendently, or they can be use in a docker-compose file.
For the sake of simplicity, the provided docker-compose.yml file is reported here:
```
version: "3"
name: ytsms
services:
  backend:
    image: msdeluise/ytsms-backend:latest
    env_file: backend.env
    depends_on:
      - db
    restart: unless-stopped

  db:
    image: mysql:8.0
    restart: always
    env_file: backend.env

  frontend:
    image: msdeluise/ytsms-frontend:latest
    env_file: frontend.env
    links:
      - backend

  reverse-proxy:
    image: nginx:stable-alpine
    ports:
      - "8080:80"
    volumes:
      - ./default.conf:/etc/nginx/conf.d/default.conf
    links:
      - backend
      - frontend
```

Run the docker compose file (`docker compose -f <file> up -d`), then the service will be available at `localhost:8080`, while the REST API will be available at `localhost:8080/api` (`localhost:8080/api/swagger-ui/index.html` for the documentation of them).

<details>

  <summary>Run on a remote host</summary>

  Please notice that running the `docker-compose` file from another machine change the way to connect to the server. For example, if you run the `docker-compose` on the machine with the local IP `192.168.1.100` then you have to change the backend url in the [API_URL](#configurations) variable to `http://192.168.1.100:8080/api`. In this case, the frontend of the system will be available at `http://192.168.1.100:8080`, and the backend will be available at `http://192.168.1.100:8080/api`.
</details>

### Setup without docker
The application was developed with being used with Docker in mind, thus this method is not preferred.

#### Requirements
* [JDK 19+](https://openjdk.org/)
* [MySQL](https://www.mysql.com/)
* [React](https://reactjs.org/)

#### Run
1. Be sure to have the `mysql` database up and running
1. Run the following command in the terminal inside the `backend` folder
  `./mvnw spring-boot:run`
1. Run the following command in the terminal inside the `frontend` folder
  `npm start`

Then, the frontend of the system will be available at `http://localhost:3000`, and the backend at `http://localhost:8085/api`.


## Configuration

There are 2 configuration file available:
* `deployment/backend.env`: file containing the configuration for the backend. An example of content is the following:
  ```
  MYSQL_HOST=db
  MYSQL_PORT=3306
  MYSQL_USERNAME=root
  MYSQL_PSW=root
  JWT_SECRET=putTheSecretHere
  JWT_EXP=1
  MYSQL_ROOT_PASSWORD=root
  MYSQL_DATABASE=bootdb
  USERS_LIMIT=-1 # including the admin account, so <= 0 if undefined, >= 2 if defined
  YOUTUBE_KEY=
  VIDEO_REFRESH=0 0 * * * *
  ```
  Change the properties values according to your system.

* `deployment/frontend.env`: file containing the configuration for the frontend. An example of content is the following:
  ```
  API_URL=http://localhost:8080/api
  BROWSER=none
  PAGE_SIZE=25
  ```
  Change the properties values according to your system.

### Fetching mode
The service offers two video retrieval options:
* `scraping`: videos are retrieved without the need of a YouTube API key, although there are [some restrictions](#FAQ)
* `official YouTube api`: video are retrieved using a YouTube API key, which means [more metadata can be fetched but less anonymization is provided](#FAQ) 

In order to choose between one of the retrieval option, the `YOUTUBE_KEY` property in the [configuration file](#Configuration) must be filled or left empty. 

## FAQ
* How can I get the `channel id` of a youtube channel?
  
  You can do it in two ways:
  * go to the channel homepage and run in the javascript console `ytInitialData.metadata.channelMetadataRenderer.externalId`
  * go to the channel homepage, view the page source and search for the value `browse_id`

* What distinguishes `official youtube api` and `scraping` modes?
  
  The `official youtube api` mode offers the following advantages:
  * retrieve the video duration
  * retrieve the channels image thumbnails
  * retrieve more old video
  
  The `scraping` mode offers the following advantages:
  * no need to use any YouTube API key
  * no [quota restrictions](https://developers.google.com/youtube/v3/getting-started#quota)
  * increased anonymization

* How can I create a YouTube API key?

  You can create a key following the [official guide](https://developers.google.com/youtube/v3/getting-started).

* Why sometime the `Channel Name` method for adding a subscription does not works?

  This is a pretty known problem ([1](https://stackoverflow.com/questions/71062188/youtube-listchannels-with-usernameforusername-is-not-working), [2](https://stackoverflow.com/questions/35051882/youtube-api-v3-channels-list-method-doesnt-work-for-some-channels-names), [3](https://stackoverflow.com/questions/39378768/youtube-api-3-channels-by-username-and-id-inconsistant), [4](https://stackoverflow.com/questions/64299967/how-to-find-the-forusername-parameter-for-a-specific-channel)). It's caused by the inconsistence of some YouTube channel's `custom URL` and `username` parameters.
