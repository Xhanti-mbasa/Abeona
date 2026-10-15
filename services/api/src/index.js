require('dotenv').config();
const express = require('express');
const cors = require('cors');
const {providers,health}=require('@abeona/provider-connectors');

const linesRouter     = require('./routes/lines');
const stationsRouter  = require('./routes/stations');
const schedulesRouter = require('./routes/schedules');
const updatesRouter   = require('./routes/updates');
const { errorHandler, notFound, rateLimiter } = require('./middleware');
const { scrapeServiceUpdates } = require('./scrapers/cttrains');
const cache = require('./utils/cache');

function createApp() {
const app = express();
const PORT = process.env.PORT || 3000;
const startedAt = new Date().toISOString();

// ── Middleware ──────────────────────────────────────────────────────────────
app.use(cors());
app.disable('x-powered-by');
app.use(express.json({limit:'64kb'}));
app.use(rateLimiter);

// ── Root ────────────────────────────────────────────────────────────────────
app.get('/', (req, res) => {
  res.json({
    name: 'Abeona API',
    description: 'Unofficial REST API for Cape Town Metrorail timetables and service updates.',
    version: '1.0.0',
    disclaimer: 'This API is not affiliated with Metrorail or PRASA. Data sourced from cttrains.co.za — a free community service.',
    endpoints: {
      lines:     'GET /lines',
      line:      'GET /lines/:lineId',
      stations_for_line: 'GET /lines/:lineId/stations',
      stations:  'GET /stations?q=',
      station:   'GET /stations/:stationId',
      schedules: 'GET /schedules?from=&to=&date=&time=',
      line_timetable: 'GET /schedules/lines/:lineId?from=&to=&days=&search_by=&time=',
      updates:   'GET /updates?line=',
      health:    'GET /health',
    },
    source: 'https://github.com/Xhanti-mbasa/cttrains-api',
    data_source: 'https://cttrains.co.za',
  });
});

// ── Health ──────────────────────────────────────────────────────────────────
app.get('/health', async (req, res) => {
  const memUsage = process.memoryUsage();
  const cacheStats = await cache.stats();

  res.json({
    status: 'ok',
    providers: Object.values(providers).map(p=>({id:p.id,...health.get(p.id)})),
    uptime_seconds: Math.floor(process.uptime()),
    timestamp: new Date().toISOString(),
    started_at: startedAt,
    cache: cacheStats,
    system: {
      memory_usage_mb: Math.round(memUsage.rss / 1024 / 1024),
      cpu_cores: require('os').cpus().length,
      platform: process.platform,
    },
    source: 'https://github.com/Xhanti-mbasa/cttrains-api',
  });
});

// ── Routes ──────────────────────────────────────────────────────────────────
app.use('/lines',     linesRouter);
app.use('/stations',  stationsRouter);
app.use('/schedules', schedulesRouter);
app.use('/updates',   updatesRouter);
app.use('/mobile/v1', require('./routes/mobile'));
app.use('/mobile/v1', require('./routes/providers').createProviderRouter());

// ── 404 / Error ─────────────────────────────────────────────────────────────
app.use(notFound);
app.use(errorHandler);

return app;
}

// ── Start ───────────────────────────────────────────────────────────────────
async function start({port=Number(process.env.PORT || 3000),host="0.0.0.0",handleSignals=true}={}) {
  // Initialise Redis (no-op if REDIS_URL is not set)
  await cache.initRedis();

  const app=createApp();
  const server=app.listen(port,host, () => {
    console.log(`Abeona API running on http://localhost:${server.address().port}`);
    console.log('Disclaimer: Unofficial — not affiliated with Metrorail/PRASA');
  });
  const stop=()=>server.close(()=>{cache.close();process.exit(0);});
  if(handleSignals) { process.once('SIGTERM',stop);process.once('SIGINT',stop); }
  return server;
}

if(require.main===module) start().catch((err) => {
  console.error('Failed to start server:', err);
  process.exit(1);
});

module.exports = {createApp,start};
