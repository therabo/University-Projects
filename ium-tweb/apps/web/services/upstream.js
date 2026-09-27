const axios = require('axios');
const {
  analyticsApiUrl,
  catalogApiUrl,
  matchApiUrl,
  requestTimeoutMs
} = require('../config/services');

const createClient = (baseURL) => axios.create({
  baseURL,
  timeout: requestTimeoutMs
});

const clients = {
  analytics: createClient(analyticsApiUrl),
  catalog: createClient(catalogApiUrl),
  match: createClient(matchApiUrl)
};

function sendUpstreamError(res, error) {
  const status = error.response?.status || 502;
  const upstreamMessage = error.response?.data?.error;

  console.error('Upstream request failed:', error.message);
  res.status(status).json({
    error: upstreamMessage || 'Upstream service unavailable'
  });
}

module.exports = { clients, sendUpstreamError };
