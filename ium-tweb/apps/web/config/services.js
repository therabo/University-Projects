const normalizeUrl = (value) => value.replace(/\/$/, '');

module.exports = {
  analyticsApiUrl: normalizeUrl(process.env.ANALYTICS_API_URL || 'http://127.0.0.1:5055'),
  catalogApiUrl: normalizeUrl(process.env.CATALOG_API_URL || 'http://127.0.0.1:8081'),
  matchApiUrl: normalizeUrl(process.env.MATCH_API_URL || 'http://127.0.0.1:3001'),
  requestTimeoutMs: Number.parseInt(process.env.UPSTREAM_TIMEOUT_MS || '10000', 10)
};
