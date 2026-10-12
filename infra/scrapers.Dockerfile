FROM gradle:8.11.1-jdk17 AS build
WORKDIR /source
COPY packages/transit-core packages/transit-core
COPY workers/scrapers workers/scrapers
RUN gradle -p workers/scrapers installDist --no-daemon
FROM eclipse-temurin:17-jre-jammy
RUN useradd --system --uid 10001 abeona && mkdir /data && chown abeona /data
WORKDIR /opt/abeona
COPY --from=build /source/workers/scrapers/build/install/scrapers/ ./
ENV SCRAPER_BIND=0.0.0.0 SCRAPER_DATA_DIR=/data
USER abeona
EXPOSE 8081
ENTRYPOINT ["bin/scrapers"]
