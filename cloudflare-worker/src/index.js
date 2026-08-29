const IGDB_API_BASE_URL = "https://api.igdb.com/v4";
const TWITCH_TOKEN_URL = "https://id.twitch.tv/oauth2/token";
const STEAM_API_BASE_URL = "https://api.steampowered.com";

const DEFAULT_PAGE_SIZE = 20;
const MAX_PAGE_SIZE = 50;
const TOP_GAMES_LIMIT = 100;
const TOP_MINIMUM_USER_RATINGS = 500;
const PRIORITY_USER_RATINGS = 50;
const TOKEN_REFRESH_MARGIN_SECONDS = 300;
const TOKEN_CACHE_KEY = new Request(
  "https://gameshelf-worker.internal/oauth/igdb-token"
);
const STEAM_SOURCE_CACHE_KEY = new Request(
  "https://gameshelf-worker.internal/igdb/steam-source"
);

const LIST_GAME_FIELDS = [
  "id",
  "name",
  "slug",
  "first_release_date",
  "rating",
  "rating_count",
  "aggregated_rating",
  "aggregated_rating_count",
  "total_rating",
  "total_rating_count",
  "cover.id",
  "cover.image_id",
  "genres.id",
  "genres.name",
  "genres.slug",
  "platforms.id",
  "platforms.name",
  "platforms.abbreviation"
].join(",");

const DETAIL_GAME_FIELDS = [
  LIST_GAME_FIELDS,
  "summary",
  "storyline",
  "url",
  "screenshots.id",
  "screenshots.image_id",
  "screenshots.width",
  "screenshots.height",
  "involved_companies.id",
  "involved_companies.developer",
  "involved_companies.publisher",
  "involved_companies.company.id",
  "involved_companies.company.name",
  "involved_companies.company.slug",
  "age_ratings.id",
  "age_ratings.organization.id",
  "age_ratings.organization.name",
  "age_ratings.rating_category.id",
  "age_ratings.rating_category.rating"
].join(",");

let tokenRequestInFlight = null;

export default {
  async fetch(request, env, context) {
    if (request.method === "OPTIONS") {
      return withCommonHeaders(new Response(null, { status: 204 }));
    }

    if (request.method !== "GET") {
      return errorResponse(405, "method_not_allowed", "Only GET is supported.");
    }

    const url = new URL(request.url);
    const path = normalizePath(url.pathname);

    try {
      if (path === "/health") {
        return jsonResponse({
          status: "ok",
          services: {
            igdb: hasIgdbConfiguration(env),
            steam: Boolean(env.STEAM_API_KEY)
          }
        });
      }

      if (path === "/games") {
        return await cachedPublicResponse(request, context, 300, () =>
          handleGames(url, env)
        );
      }

      const gameIdMatch = path.match(/^\/games\/(\d+)$/);
      if (gameIdMatch) {
        const gameId = parsePositiveInteger(gameIdMatch[1], "game id");
        return await cachedPublicResponse(request, context, 3600, () =>
          handleGameDetails(gameId, env)
        );
      }

      if (path === "/genres") {
        return await cachedPublicResponse(request, context, 86400, () =>
          handleGenres(env)
        );
      }

      if (path === "/platforms") {
        return await cachedPublicResponse(request, context, 86400, () =>
          handlePlatforms(env)
        );
      }

      if (path === "/age-rating-categories") {
        return await cachedPublicResponse(request, context, 86400, () =>
          handleAgeRatingCategories(env)
        );
      }

      if (path === "/steam/player-summaries") {
        return await handleSteamPlayerSummaries(url, env);
      }

      if (path === "/steam/game-match") {
        return await cachedPublicResponse(request, context, 86400, () =>
          handleSteamGameMatch(url, env)
        );
      }

      if (path === "/steam/owned-games") {
        return await handleSteamOwnedGames(url, env);
      }

      if (path === "/steam/recently-played") {
        return await handleSteamRecentlyPlayed(url, env);
      }

      if (path === "/steam/resolve-vanity") {
        return await handleSteamResolveVanity(url, env);
      }

      return errorResponse(404, "not_found", "Endpoint was not found.");
    } catch (error) {
      const status = error instanceof ApiError ? error.status : 500;
      const code = error instanceof ApiError
        ? error.code
        : "internal_server_error";
      const message = error instanceof ApiError
        ? error.message
        : "An unexpected server error occurred.";

      console.error(JSON.stringify({
        event: "request_failed",
        path,
        status,
        code,
        message: error instanceof Error ? error.message : String(error)
      }));

      return errorResponse(status, code, message);
    }
  }
};

async function handleGames(url, env) {
  const search = sanitizeSearch(url.searchParams.get("search") ?? "");
  const limit = parseBoundedInteger(
    url.searchParams.get("limit"),
    DEFAULT_PAGE_SIZE,
    1,
    MAX_PAGE_SIZE,
    "limit"
  );
  const offset = parseBoundedInteger(
    url.searchParams.get("offset"),
    0,
    0,
    100000,
    "offset"
  );

  const requestedSort = url.searchParams.get("sort");
  const sort = requestedSort ?? (search ? "relevance" : "users");
  const sortStatement = buildSortStatement(sort);
  const isTopGames = !search && (
    sort === "top" || url.searchParams.get("topOnly") === "true"
  );

  if (isTopGames && offset >= TOP_GAMES_LIMIT) {
    return jsonResponse({
      items: [],
      pagination: {
        limit: 0,
        offset,
        returned: 0,
        hasMore: false
      }
    });
  }

  const effectiveLimit = isTopGames
    ? Math.min(limit, TOP_GAMES_LIMIT - offset)
    : limit;

  const filters = [];

  if (isTopGames) {
    filters.push("parent_game = null");
    filters.push("cover != null");
  }

  appendReferenceFilter(filters, url, "genreId", "genres");
  appendReferenceFilter(filters, url, "platformId", "platforms");
  appendReferenceListFilter(filters, url, "platformIds", "platforms");
  appendReferenceFilter(
    filters,
    url,
    "ageRatingCategoryId",
    "age_ratings.rating_category"
  );

  const developerId = optionalPositiveInteger(
    url.searchParams.get("developerId"),
    "developerId"
  );
  if (developerId !== null) {
    filters.push(`involved_companies.company = ${developerId}`);
    filters.push("involved_companies.developer = true");
  }

  const publisherId = optionalPositiveInteger(
    url.searchParams.get("publisherId"),
    "publisherId"
  );
  if (publisherId !== null) {
    filters.push(`involved_companies.company = ${publisherId}`);
    filters.push("involved_companies.publisher = true");
  }

  if (!search && (sort === "users" || sort === "top")) {
    const minimumRatings = parseBoundedInteger(
      url.searchParams.get("minimumRatings"),
      isTopGames ? TOP_MINIMUM_USER_RATINGS : 0,
      0,
      1000000,
      "minimumRatings"
    );
    filters.push("rating != null");
    if (minimumRatings > 0) {
      filters.push(`rating_count >= ${minimumRatings}`);
    }
  }

  if (!search && sort === "critics") {
    const minimumCriticRatings = parseBoundedInteger(
      url.searchParams.get("minimumCriticRatings"),
      5,
      0,
      1000000,
      "minimumCriticRatings"
    );
    filters.push("aggregated_rating != null");
    filters.push(`aggregated_rating_count >= ${minimumCriticRatings}`);
  }

  if (!search && sort === "users" && !isTopGames) {
    const items = await getPrioritizedUserRatedGames(
      env,
      filters,
      offset,
      effectiveLimit
    );
    return jsonResponse({
      items,
      pagination: {
        limit: effectiveLimit,
        offset,
        returned: items.length,
        hasMore: items.length === effectiveLimit
      }
    });
  }

  const queryParts = [];
  if (search) {
    queryParts.push(`search "${escapeApicalypseString(search)}";`);
  }
  queryParts.push(`fields ${LIST_GAME_FIELDS};`);
  if (filters.length > 0) {
    queryParts.push(`where ${filters.join(" & ")};`);
  }
  if (sortStatement) {
    queryParts.push(sortStatement);
  }
  queryParts.push(`limit ${effectiveLimit};`);
  queryParts.push(`offset ${offset};`);

  const items = await igdbRequest(env, "games", queryParts.join("\n"));

  return jsonResponse({
    items,
    pagination: {
      limit: effectiveLimit,
      offset,
      returned: items.length,
      hasMore: items.length === effectiveLimit &&
        (!isTopGames || offset + items.length < TOP_GAMES_LIMIT)
    }
  });
}

async function handleGameDetails(gameId, env) {
  const query = `
    query games "game" {
      fields ${DETAIL_GAME_FIELDS};
      where id = ${gameId};
      limit 1;
    };
    query game_time_to_beats "timeToBeat" {
      fields game_id,hastily,normally,completely,count;
      where game_id = ${gameId};
      limit 1;
    };
  `;

  const result = await igdbRequest(env, "multiquery", query);
  const game = findMultiQueryResult(result, "game")[0] ?? null;

  if (!game) {
    throw new ApiError(404, "game_not_found", "Game was not found.");
  }

  const timeToBeat = findMultiQueryResult(result, "timeToBeat")[0] ?? null;

  return jsonResponse({ game, timeToBeat });
}

async function handleGenres(env) {
  const items = await igdbRequest(
    env,
    "genres",
    "fields id,name,slug; sort name asc; limit 500;"
  );
  return jsonResponse({ items });
}

async function handlePlatforms(env) {
  const items = await igdbRequest(
    env,
    "platforms",
    "fields id,name,abbreviation,slug; sort name asc; limit 500;"
  );
  return jsonResponse({ items });
}

async function handleAgeRatingCategories(env) {
  const items = await igdbRequest(
    env,
    "age_rating_categories",
    [
      "fields id,rating,organization.id,organization.name;",
      "sort organization asc;",
      "limit 500;"
    ].join(" ")
  );
  return jsonResponse({ items });
}

async function handleSteamPlayerSummaries(url, env) {
  const steamIds = parseSteamIds(url.searchParams.get("steamIds"));
  return steamRequest(
    env,
    "/ISteamUser/GetPlayerSummaries/v0002/",
    { steamids: steamIds.join(","), format: "json" }
  );
}

async function handleSteamOwnedGames(url, env) {
  const steamId = parseSteamId(url.searchParams.get("steamId"));
  return steamRequest(
    env,
    "/IPlayerService/GetOwnedGames/v0001/",
    {
      steamid: steamId,
      include_appinfo: "true",
      include_played_free_games: "true",
      format: "json"
    }
  );
}

async function handleSteamRecentlyPlayed(url, env) {
  const steamId = parseSteamId(url.searchParams.get("steamId"));
  return steamRequest(
    env,
    "/IPlayerService/GetRecentlyPlayedGames/v0001/",
    { steamid: steamId, count: "0", format: "json" }
  );
}

async function handleSteamResolveVanity(url, env) {
  const vanityUrl = (url.searchParams.get("vanityUrl") ?? "").trim();
  if (!/^[A-Za-z0-9_-]{2,64}$/.test(vanityUrl)) {
    throw new ApiError(
      400,
      "invalid_vanity_url",
      "Steam vanity name has an invalid format."
    );
  }

  return steamRequest(
    env,
    "/ISteamUser/ResolveVanityURL/v0001/",
    { vanityurl: vanityUrl, url_type: "1", format: "json" }
  );
}

async function getPrioritizedUserRatedGames(env, filters, offset, limit) {
  const priorityFilters = [
    ...filters,
    `rating_count > ${PRIORITY_USER_RATINGS}`
  ];
  const priorityCountResponse = await igdbRequest(
    env,
    "games/count",
    `where ${priorityFilters.join(" & ")};`
  );
  const priorityCount = Number(priorityCountResponse?.count) || 0;
  const items = [];

  if (offset < priorityCount) {
    const priorityLimit = Math.min(limit, priorityCount - offset);
    items.push(...await requestGameList(
      env,
      priorityFilters,
      priorityLimit,
      offset,
      "sort rating desc;"
    ));
  }

  const remaining = limit - items.length;
  if (remaining > 0) {
    const fallbackOffset = Math.max(0, offset - priorityCount);
    const fallbackFilters = [
      ...filters,
      `rating_count <= ${PRIORITY_USER_RATINGS}`
    ];
    items.push(...await requestGameList(
      env,
      fallbackFilters,
      remaining,
      fallbackOffset,
      "sort rating desc;"
    ));
  }

  return items;
}

async function requestGameList(env, filters, limit, offset, sortStatement) {
  const query = [
    `fields ${LIST_GAME_FIELDS};`,
    `where ${filters.join(" & ")};`,
    sortStatement,
    `limit ${limit};`,
    `offset ${offset};`
  ].join("\n");
  return igdbRequest(env, "games", query);
}

async function handleSteamGameMatch(url, env) {
  const appId = parsePositiveInteger(
    url.searchParams.get("appId"),
    "appId"
  );
  const steamSourceId = await getSteamExternalSourceId(env);
  const externalGames = await igdbRequest(
    env,
    "external_games",
    [
      "fields uid,game;",
      `where external_game_source = ${steamSourceId}` +
        ` & uid = \"${appId}\";`,
      "limit 20;"
    ].join(" ")
  );
  const gameIds = [
    ...new Set(
      externalGames
        .map((item) => item.game)
        .filter((gameId) => Number.isSafeInteger(gameId) && gameId > 0)
    )
  ];

  if (gameIds.length === 1) {
    return jsonResponse({
      steamAppId: appId,
      igdbGameId: gameIds[0],
      status: "exact"
    });
  }

  return jsonResponse({
    steamAppId: appId,
    igdbGameId: null,
    status: gameIds.length === 0 ? "unmatched" : "ambiguous"
  });
}

async function getSteamExternalSourceId(env) {
  const cache = caches.default;
  const cachedResponse = await cache.match(STEAM_SOURCE_CACHE_KEY);
  if (cachedResponse) {
    const cached = await cachedResponse.json();
    if (Number.isSafeInteger(cached.id) && cached.id > 0) {
      return cached.id;
    }
  }

  const sources = await igdbRequest(
    env,
    "external_game_sources",
    'fields id,name; where name = "Steam"; limit 1;'
  );
  const sourceId = sources[0]?.id;
  if (!Number.isSafeInteger(sourceId) || sourceId <= 0) {
    throw new ApiError(
      502,
      "steam_source_not_found",
      "IGDB did not return its Steam external source."
    );
  }

  await cache.put(
    STEAM_SOURCE_CACHE_KEY,
    new Response(JSON.stringify({ id: sourceId }), {
      headers: {
        "Content-Type": "application/json",
        "Cache-Control": "public, max-age=86400"
      }
    })
  );
  return sourceId;
}

async function igdbRequest(env, endpoint, body, retryAuthentication = true) {
  const clientId = getIgdbClientId(env);
  const accessToken = await getIgdbAccessToken(env);

  const response = await fetch(`${IGDB_API_BASE_URL}/${endpoint}`, {
    method: "POST",
    headers: {
      "Client-ID": clientId,
      "Authorization": `Bearer ${accessToken}`,
      "Accept": "application/json",
      "Content-Type": "text/plain"
    },
    body
  });

  if (response.status === 401 && retryAuthentication && env.IGDB_CLIENT_SECRET) {
    await clearCachedIgdbToken();
    return igdbRequest(env, endpoint, body, false);
  }

  if (!response.ok) {
    throw upstreamError("IGDB", response.status);
  }

  return response.json();
}

async function steamRequest(env, pathname, queryParameters) {
  if (!env.STEAM_API_KEY) {
    throw new ApiError(
      503,
      "steam_not_configured",
      "Steam integration is not configured."
    );
  }

  const url = new URL(pathname, STEAM_API_BASE_URL);
  for (const [name, value] of Object.entries(queryParameters)) {
    url.searchParams.set(name, value);
  }

  const response = await fetch(url, {
    headers: {
      "x-webapi-key": env.STEAM_API_KEY,
      "Accept": "application/json"
    }
  });

  if (!response.ok) {
    throw upstreamError("Steam", response.status);
  }

  return withCommonHeaders(new Response(response.body, {
    status: 200,
    headers: {
      "Content-Type": "application/json",
      "Cache-Control": "no-store"
    }
  }));
}

async function getIgdbAccessToken(env) {
  if (!env.IGDB_CLIENT_SECRET && env.ACCESS_TOKEN) {
    return env.ACCESS_TOKEN;
  }

  if (!hasIgdbConfiguration(env)) {
    throw new ApiError(
      503,
      "igdb_not_configured",
      "IGDB integration is not configured."
    );
  }

  const cache = caches.default;
  const cachedResponse = await cache.match(TOKEN_CACHE_KEY);
  if (cachedResponse) {
    const cached = await cachedResponse.json();
    if (cached.accessToken) {
      return cached.accessToken;
    }
  }

  if (!tokenRequestInFlight) {
    tokenRequestInFlight = requestAndCacheIgdbToken(env)
      .finally(() => {
        tokenRequestInFlight = null;
      });
  }

  return tokenRequestInFlight;
}

async function requestAndCacheIgdbToken(env) {
  const tokenUrl = new URL(TWITCH_TOKEN_URL);
  tokenUrl.searchParams.set("client_id", getIgdbClientId(env));
  tokenUrl.searchParams.set("client_secret", env.IGDB_CLIENT_SECRET);
  tokenUrl.searchParams.set("grant_type", "client_credentials");

  const response = await fetch(tokenUrl, { method: "POST" });
  if (!response.ok) {
    throw upstreamError("Twitch authentication", response.status);
  }

  const token = await response.json();
  if (!token.access_token || !Number.isFinite(token.expires_in)) {
    throw new ApiError(
      502,
      "invalid_twitch_response",
      "Twitch returned an invalid authentication response."
    );
  }

  const cacheSeconds = Math.max(
    60,
    token.expires_in - TOKEN_REFRESH_MARGIN_SECONDS
  );

  await caches.default.put(
    TOKEN_CACHE_KEY,
    new Response(JSON.stringify({ accessToken: token.access_token }), {
      headers: {
        "Content-Type": "application/json",
        "Cache-Control": `public, max-age=${cacheSeconds}`
      }
    })
  );

  return token.access_token;
}

async function clearCachedIgdbToken() {
  tokenRequestInFlight = null;
  await caches.default.delete(TOKEN_CACHE_KEY);
}

async function cachedPublicResponse(request, context, maxAgeSeconds, producer) {
  const cache = caches.default;
  const cacheKey = new Request(request.url, { method: "GET" });
  const cached = await cache.match(cacheKey);

  if (cached) {
    return withCommonHeaders(cached);
  }

  const response = await producer();
  const cacheable = new Response(response.body, response);
  cacheable.headers.set(
    "Cache-Control",
    `public, max-age=${maxAgeSeconds}`
  );

  context.waitUntil(cache.put(cacheKey, cacheable.clone()));
  return withCommonHeaders(cacheable);
}

function buildSortStatement(sort) {
  switch (sort) {
    case "relevance":
      return "";
    case "users":
    case "top":
      return "sort rating desc;";
    case "critics":
      return "sort aggregated_rating desc;";
    case "popular":
      return "sort total_rating_count desc;";
    case "newest":
      return "sort first_release_date desc;";
    default:
      throw new ApiError(
        400,
        "invalid_sort",
        "sort must be relevance, top, users, critics, popular, or newest."
      );
  }
}

function appendReferenceFilter(filters, url, parameterName, fieldName) {
  const value = optionalPositiveInteger(
    url.searchParams.get(parameterName),
    parameterName
  );
  if (value !== null) {
    filters.push(`${fieldName} = (${value})`);
  }
}

function appendReferenceListFilter(filters, url, parameterName, fieldName) {
  const rawValue = url.searchParams.get(parameterName);
  if (rawValue === null || rawValue === "") {
    return;
  }
  const values = rawValue.split(",").map((value) =>
    parsePositiveInteger(value.trim(), parameterName)
  );
  if (values.length > 20) {
    throw new ApiError(
      400,
      "invalid_parameter",
      `${parameterName} must contain no more than 20 IDs.`
    );
  }
  filters.push(`${fieldName} = (${[...new Set(values)].join(",")})`);
}

function findMultiQueryResult(results, name) {
  const item = results.find((entry) => entry.name === name);
  return Array.isArray(item?.result) ? item.result : [];
}

function parseSteamIds(value) {
  if (!value) {
    throw new ApiError(400, "missing_steam_ids", "steamIds is required.");
  }

  const ids = value.split(",").map((item) => parseSteamId(item));
  if (ids.length > 100) {
    throw new ApiError(
      400,
      "too_many_steam_ids",
      "No more than 100 Steam IDs are allowed."
    );
  }
  return ids;
}

function parseSteamId(value) {
  const steamId = (value ?? "").trim();
  if (!/^\d{17}$/.test(steamId)) {
    throw new ApiError(
      400,
      "invalid_steam_id",
      "Steam ID must contain exactly 17 digits."
    );
  }
  return steamId;
}

function sanitizeSearch(value) {
  return value.replace(/[\u0000-\u001F\u007F]/g, " ").trim().slice(0, 80);
}

function escapeApicalypseString(value) {
  return value.replace(/\\/g, "\\\\").replace(/"/g, '\\"');
}

function parsePositiveInteger(value, name) {
  const parsed = Number(value);
  if (!Number.isSafeInteger(parsed) || parsed <= 0) {
    throw new ApiError(400, "invalid_parameter", `${name} must be positive.`);
  }
  return parsed;
}

function optionalPositiveInteger(value, name) {
  if (value === null || value === "") {
    return null;
  }
  return parsePositiveInteger(value, name);
}

function parseBoundedInteger(value, defaultValue, minimum, maximum, name) {
  if (value === null || value === "") {
    return defaultValue;
  }

  const parsed = Number(value);
  if (
    !Number.isSafeInteger(parsed) ||
    parsed < minimum ||
    parsed > maximum
  ) {
    throw new ApiError(
      400,
      "invalid_parameter",
      `${name} must be between ${minimum} and ${maximum}.`
    );
  }
  return parsed;
}

function normalizePath(pathname) {
  const withoutTrailingSlash = pathname.length > 1
    ? pathname.replace(/\/+$/, "")
    : pathname;

  if (withoutTrailingSlash === "/v1") {
    return "/";
  }

  return withoutTrailingSlash.startsWith("/v1/")
    ? withoutTrailingSlash.slice(3)
    : withoutTrailingSlash;
}

function getIgdbClientId(env) {
  const clientId = env.IGDB_CLIENT_ID ?? env.ID_CLIENT_IGDB;
  if (!clientId) {
    throw new ApiError(
      503,
      "igdb_not_configured",
      "IGDB Client ID is missing."
    );
  }
  return clientId;
}

function hasIgdbConfiguration(env) {
  const hasClientId = Boolean(env.IGDB_CLIENT_ID ?? env.ID_CLIENT_IGDB);
  const hasAuthentication = Boolean(
    env.IGDB_CLIENT_SECRET ?? env.ACCESS_TOKEN
  );
  return hasClientId && hasAuthentication;
}

function upstreamError(service, status) {
  if (status === 401 || status === 403) {
    return new ApiError(
      502,
      "upstream_authentication_failed",
      `${service} rejected server credentials.`
    );
  }
  if (status === 404) {
    return new ApiError(404, "upstream_not_found", `${service} found no data.`);
  }
  if (status === 429) {
    return new ApiError(
      429,
      "upstream_rate_limited",
      `${service} request limit was reached.`
    );
  }
  return new ApiError(
    502,
    "upstream_failed",
    `${service} is temporarily unavailable.`
  );
}

function jsonResponse(data, status = 200) {
  return withCommonHeaders(new Response(JSON.stringify(data), {
    status,
    headers: { "Content-Type": "application/json; charset=utf-8" }
  }));
}

function errorResponse(status, code, message) {
  return jsonResponse({ error: { code, message } }, status);
}

function withCommonHeaders(response) {
  const result = new Response(response.body, response);
  result.headers.set("Access-Control-Allow-Origin", "*");
  result.headers.set("Access-Control-Allow-Methods", "GET, OPTIONS");
  result.headers.set("Access-Control-Allow-Headers", "Content-Type");
  result.headers.set("X-Content-Type-Options", "nosniff");
  return result;
}

class ApiError extends Error {
  constructor(status, code, message) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.code = code;
  }
}
