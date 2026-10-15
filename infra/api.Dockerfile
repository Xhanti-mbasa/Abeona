FROM node:22-alpine AS dependencies
WORKDIR /app
COPY package.json package-lock.json ./
COPY services/api/package.json services/api/package.json
COPY packages/provider-connectors/package.json packages/provider-connectors/package.json
COPY packages/contracts/package.json packages/contracts/package.json
RUN npm ci --omit=dev --ignore-scripts
FROM node:22-alpine
RUN apk add --no-cache dumb-init
WORKDIR /app
COPY --from=dependencies /app/node_modules ./node_modules
COPY package.json ./
COPY services/api ./services/api
COPY packages/provider-connectors ./packages/provider-connectors
COPY packages/contracts ./packages/contracts
RUN mkdir -p services/api/.timetable-snapshots && chown -R node:node /app
USER node
EXPOSE 3000
HEALTHCHECK --interval=30s --timeout=5s CMD wget -qO- http://localhost:3000/health || exit 1
ENTRYPOINT ["dumb-init", "--"]
CMD ["node", "services/api/src/index.js"]
