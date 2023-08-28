FROM node:18

# Create app directory
WORKDIR /usr/src/app

# Install app dependencies
COPY package*.json ./

RUN npm install
# If you are building your code production
# RUN npm ci --omit=dev

EXPOSE 3000

CMD [ "node", "index.js" ]