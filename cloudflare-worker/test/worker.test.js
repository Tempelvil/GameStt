import assert from "node:assert/strict";
import test from "node:test";
import worker from "../src/index.js";

class MemoryCache {
  constructor() {
    this.values = new Map();
  }

  async match(request) {
    const value = this.values.get(request.url);
    return value ? value.clone() : undefined;
  }

  async put(request, response) {
    this.values.set(request.url, response.clone());
  }

  async delete(request) {
    return this.values.delete(request.url);
  }

  clear() {
    this.values.clear();
  }
}

const memoryCache = new MemoryCache();
globalThis.caches = { default: memoryCache };

function createContext() {
  const pending = [];
  return {
    pending,
    waitUntil(promise) {
      pending.push(promise);
    }
  };
}

async function readJson(response) {
  return JSON.parse(await response.text());
}

test.beforeEach(() => {
  memoryCache.clear();
});

test("health exposes configuration state without secret values", async () => {
  const response = await worker.fetch(
    new Request("https://worker.example/v1/health"),
    {
      IGDB_CLIENT_ID: "client-id",
      IGDB_CLIENT_SECRET: "client-secret",
      STEAM_API_KEY: "steam-secret"
    },
    createContext()
  );

  assert.equal(response.status, 200);
  const body = await readJson(response);
  assert.deepEqual(body, {
    status: "ok",
    services: { igdb: true, steam: true }
  });
  assert.equal(JSON.stringify(body).includes("client-secret"), false);
  assert.equal(JSON.stringify(body).includes("steam-secret"), false);
});

test("games obtains a token server-side and sends it only to IGDB", async () => {
  const calls = [];
  globalThis.fetch = async (request, init = {}) => {
    const url = request instanceof Request ? request.url : String(request);
    calls.push({ url, init });

    if (url.startsWith("https://id.twitch.tv/oauth2/token")) {
      return Response.json({
        access_token: "server-only-token",
        expires_in: 3600,
        token_type: "bearer"
      });
    }

    if (url === "https://api.igdb.com/v4/games") {
      assert.equal(init.headers.Authorization, "Bearer server-only-token");
      assert.equal(init.headers["Client-ID"], "client-id");
      assert.match(init.body, /search "Chrono Trigger";/);
      assert.match(init.body, /limit 20;/);
      return Response.json([{ id: 19, name: "Chrono Trigger" }]);
    }

    throw new Error(`Unexpected fetch: ${url}`);
  };

  const context = createContext();
  const response = await worker.fetch(
    new Request(
      "https://worker.example/v1/games?search=Chrono%20Trigger"
    ),
    {
      IGDB_CLIENT_ID: "client-id",
      IGDB_CLIENT_SECRET: "client-secret"
    },
    context
  );

  await Promise.all(context.pending);
  assert.equal(response.status, 200);
  const body = await readJson(response);
  assert.equal(body.items[0].name, "Chrono Trigger");
  assert.equal(body.pagination.returned, 1);
  assert.equal(calls.length, 2);
});

test("top games are trustworthy user ratings and stop after 100", async () => {
  globalThis.fetch = async (request, init = {}) => {
    const url = request instanceof Request ? request.url : String(request);
    assert.equal(url, "https://api.igdb.com/v4/games");
    assert.match(init.body, /rating_count >= 500/);
    assert.match(init.body, /parent_game = null/);
    assert.match(init.body, /sort rating desc/);
    assert.match(init.body, /limit 20/);
    assert.match(init.body, /offset 80/);
    return Response.json(
      Array.from({ length: 20 }, (_, index) => ({
        id: index + 1,
        name: `Top game ${index + 1}`
      }))
    );
  };

  const response = await worker.fetch(
    new Request(
      "https://worker.example/v1/games?sort=top&limit=20&offset=80"
    ),
    {
      IGDB_CLIENT_ID: "client-id",
      ACCESS_TOKEN: "legacy-token"
    },
    createContext()
  );

  const body = await readJson(response);
  assert.equal(body.items.length, 20);
  assert.equal(body.pagination.hasMore, false);
});

test("game details combines the game and time-to-beat data", async () => {
  globalThis.fetch = async (request, init = {}) => {
    const url = request instanceof Request ? request.url : String(request);

    if (url.startsWith("https://id.twitch.tv/oauth2/token")) {
      return Response.json({ access_token: "token", expires_in: 3600 });
    }

    if (url === "https://api.igdb.com/v4/multiquery") {
      assert.match(init.body, /query games "game"/);
      assert.match(init.body, /query game_time_to_beats "timeToBeat"/);
      return Response.json([
        {
          name: "game",
          result: [{ id: 19, name: "Chrono Trigger" }]
        },
        {
          name: "timeToBeat",
          result: [{
            game_id: 19,
            hastily: 72000,
            normally: 90000,
            completely: 144000,
            count: 100
          }]
        }
      ]);
    }

    throw new Error(`Unexpected fetch: ${url}`);
  };

  const context = createContext();
  const response = await worker.fetch(
    new Request("https://worker.example/v1/games/19"),
    {
      IGDB_CLIENT_ID: "client-id",
      IGDB_CLIENT_SECRET: "client-secret"
    },
    context
  );

  await Promise.all(context.pending);
  const body = await readJson(response);
  assert.equal(body.game.id, 19);
  assert.equal(body.timeToBeat.completely, 144000);
});

test("Steam key is added by the Worker and never accepted from Android", async () => {
  globalThis.fetch = async (request, init = {}) => {
    const url = request instanceof Request ? request.url : String(request);
    assert.match(url, /GetOwnedGames/);
    assert.equal(new URL(url).searchParams.has("key"), false);
    assert.equal(init.headers["x-webapi-key"], "steam-secret");
    return Response.json({ response: { game_count: 0, games: [] } });
  };

  const response = await worker.fetch(
    new Request(
      "https://worker.example/v1/steam/owned-games" +
      "?steamId=76561198000000000"
    ),
    { STEAM_API_KEY: "steam-secret" },
    createContext()
  );

  assert.equal(response.status, 200);
  const body = await readJson(response);
  assert.equal(body.response.game_count, 0);
});

test("genre lists prioritize trusted ratings and then include the rest", async () => {
  const gameQueries = [];
  globalThis.fetch = async (request, init = {}) => {
    const url = request instanceof Request ? request.url : String(request);
    assert.match(init.body, /genres = \(36\)/);
    assert.doesNotMatch(init.body, /cover != null/);
    assert.doesNotMatch(init.body, /version_parent = null/);
    if (url === "https://api.igdb.com/v4/games/count") {
      assert.match(init.body, /rating_count > 50/);
      return Response.json({ count: 1 });
    }
    assert.equal(url, "https://api.igdb.com/v4/games");
    assert.match(init.body, /sort rating desc/);
    gameQueries.push(init.body);
    if (init.body.includes("rating_count > 50")) {
      return Response.json([
        { id: 1, name: "Established MOBA", rating: 75, rating_count: 100 }
      ]);
    }
    assert.match(init.body, /rating_count <= 50/);
    return Response.json([
      { id: 2, name: "League of Legends", rating: 80, rating_count: 40 }
    ]);
  };

  const response = await worker.fetch(
    new Request(
      "https://worker.example/v1/games?genreId=36&sort=users"
    ),
    {
      IGDB_CLIENT_ID: "client-id",
      ACCESS_TOKEN: "legacy-token"
    },
    createContext()
  );

  assert.equal(response.status, 200);
  const body = await readJson(response);
  assert.deepEqual(
    body.items.map((game) => game.name),
    ["Established MOBA", "League of Legends"]
  );
  assert.equal(gameQueries.length, 2);
});

test("platform families filter by every platform ID in the family", async () => {
  globalThis.fetch = async (request, init = {}) => {
    const url = request instanceof Request ? request.url : String(request);
    assert.match(init.body, /platforms = \(11,12,49,169\)/);
    if (url.endsWith("/games/count")) {
      return Response.json({ count: 0 });
    }
    return Response.json([]);
  };

  const response = await worker.fetch(
    new Request(
      "https://worker.example/v1/games" +
      "?platformIds=11,12,49,169&sort=users"
    ),
    {
      IGDB_CLIENT_ID: "client-id",
      ACCESS_TOKEN: "legacy-token"
    },
    createContext()
  );

  assert.equal(response.status, 200);
});

test("Steam app id is resolved through IGDB external games", async () => {
  const requests = [];
  globalThis.fetch = async (request, init = {}) => {
    const url = request instanceof Request ? request.url : String(request);
    requests.push({ url, body: init.body });

    if (url === "https://api.igdb.com/v4/external_game_sources") {
      assert.match(init.body, /name = "Steam"/);
      return Response.json([{ id: 1, name: "Steam" }]);
    }

    if (url === "https://api.igdb.com/v4/external_games") {
      assert.match(init.body, /external_game_source = 1/);
      assert.match(init.body, /uid = "1245620"/);
      return Response.json([{ uid: "1245620", game: 119133 }]);
    }

    throw new Error(`Unexpected fetch: ${url}`);
  };

  const context = createContext();
  const response = await worker.fetch(
    new Request(
      "https://worker.example/v1/steam/game-match?appId=1245620"
    ),
    {
      IGDB_CLIENT_ID: "client-id",
      ACCESS_TOKEN: "legacy-token"
    },
    context
  );

  await Promise.all(context.pending);
  assert.equal(response.status, 200);
  assert.deepEqual(await readJson(response), {
    steamAppId: 1245620,
    igdbGameId: 119133,
    status: "exact"
  });
  assert.equal(requests.length, 2);
});

test("invalid parameters are rejected before an upstream request", async () => {
  let fetchCalled = false;
  globalThis.fetch = async () => {
    fetchCalled = true;
    throw new Error("fetch should not be called");
  };

  const response = await worker.fetch(
    new Request("https://worker.example/v1/games?limit=1000"),
    {
      IGDB_CLIENT_ID: "client-id",
      IGDB_CLIENT_SECRET: "client-secret"
    },
    createContext()
  );

  assert.equal(response.status, 400);
  assert.equal(fetchCalled, false);
  const body = await readJson(response);
  assert.equal(body.error.code, "invalid_parameter");
});
