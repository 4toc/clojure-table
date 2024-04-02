FROM clojure:tools-deps

RUN apt-get update && \
    apt-get install -y curl && \
    curl -sL https://deb.nodesource.com/setup_21.x | bash && \
    apt-get install -y nodejs

COPY . /usr/src/app
WORKDIR /usr/src/app

RUN npm install --global yarn
RUN npm install --global shadow-cljs 

RUN yarn install

RUN shadow-cljs release frontend

RUN clj -T:build uber

CMD [ "java", "-jar", "target/app-standalone.jar" ]