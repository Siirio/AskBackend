import http from "k6/http";
import { check } from "k6";

const baseUrl = __ENV.BASE_URL;
const scenario = __ENV.SCENARIO || "healthy_search";
const virtualUsers = Number(__ENV.VUS || 10);
const duration = __ENV.DURATION || "30s";
const requestEndpoint = __ENV.REQUEST_ENDPOINT;
const requestPayload = __ENV.REQUEST_PAYLOAD;

if (!baseUrl) {
  throw new Error("BASE_URL is required");
}

export const options = {
  vus: virtualUsers,
  duration,
  thresholds: {
    http_req_failed: [__ENV.MAX_FAILURE_RATE || "rate<0.01"],
    http_req_duration: [__ENV.P95_THRESHOLD || "p(95)<500"],
  },
};

const searchPayload = JSON.stringify({
  raw_query: __ENV.SEARCH_QUERY || "trail running shoes",
  scope: __ENV.SEARCH_SCOPE || "product",
  sort: "intent_match",
  page: 0,
  page_size: Number(__ENV.PAGE_SIZE || 20),
});

export default function () {
  const isSearchScenario = ["healthy_search", "ai_timeout", "meilisearch_outage", "rebuild_reconciliation"].includes(scenario);
  const endpoint = isSearchScenario ? "/api/v1/search" : requestEndpoint;
  const payload = isSearchScenario ? searchPayload : requestPayload;
  if (!endpoint || !payload) {
    throw new Error(`REQUEST_ENDPOINT and REQUEST_PAYLOAD are required for ${scenario}`);
  }
  const response = http.post(`${baseUrl.replace(/\/$/, "")}${endpoint}`, payload, {
    headers: {
      "Content-Type": "application/json",
      ...( __ENV.AUTH_TOKEN ? { Authorization: `Bearer ${__ENV.AUTH_TOKEN}` } : {}),
    },
    tags: { scenario },
  });
  check(response, {
    "request succeeded": result => result.status >= 200 && result.status < 300,
    "search response retained query": result => !isSearchScenario || result.json("raw_query") === (__ENV.SEARCH_QUERY || "trail running shoes"),
  });
}
