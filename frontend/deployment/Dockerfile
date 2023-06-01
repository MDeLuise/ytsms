FROM node:alpine

WORKDIR /app
COPY package.json .
COPY package-lock.json .
RUN npm install && \
    npm run build
COPY . .

ENTRYPOINT ["serve","-s","build"]
