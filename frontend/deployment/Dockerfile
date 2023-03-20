FROM node:alpine

WORKDIR /app
COPY package.json .
COPY package-lock.json .
RUN yarn install
COPY . .
RUN yarn build

ENTRYPOINT ["npm","start"]
