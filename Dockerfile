FROM gcr.io/distroless/java17-debian11

ENV APP_HOME=/app

COPY ./Yiski6-Fat.jar $APP_HOME/

WORKDIR $APP_HOME

CMD ["Yiski6-Fat.jar"]
