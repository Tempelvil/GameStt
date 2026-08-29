# GameShelf API Worker

Cloudflare Worker that keeps IGDB and Steam credentials outside the Android APK.
Android calls this Worker; only the Worker calls Twitch OAuth, IGDB, and Steam.

## Required Cloudflare secrets

- `IGDB_CLIENT_ID`
- `IGDB_CLIENT_SECRET`
- `STEAM_API_KEY`

The old names `ID_CLIENT_IGDB` and `ACCESS_TOKEN` are accepted temporarily for
compatibility. Once automatic token refresh works, remove `ACCESS_TOKEN` and use
`IGDB_CLIENT_ID` plus `IGDB_CLIENT_SECRET`.

Never commit `.dev.vars`, access tokens, client secrets, or Steam keys.

## Public endpoints

All routes work both with and without the `/v1` prefix. Android should use `/v1`.

- `GET /v1/health`
- `GET /v1/games`
- `GET /v1/games/{id}`
- `GET /v1/genres`
- `GET /v1/platforms`
- `GET /v1/age-rating-categories`
- `GET /v1/steam/player-summaries?steamIds={id}`
- `GET /v1/steam/owned-games?steamId={id}`
- `GET /v1/steam/recently-played?steamId={id}`
- `GET /v1/steam/resolve-vanity?vanityUrl={name}`
- `GET /v1/steam/game-match?appId={steamAppId}`

`game-match` resolves a Steam App ID through IGDB `external_games`. An exact
match contains `igdbGameId`; missing or non-unique matches return `unmatched`
or `ambiguous` without guessing by title.

### Games parameters

- `search`: up to 80 characters
- `limit`: 1-50, default 20
- `offset`: default 0
- `sort`: `relevance`, `top`, `users`, `critics`, `popular`, or `newest`
- `genreId`
- `platformId` (IGDB PC is `6`)
- `developerId`
- `publisherId`
- `ageRatingCategoryId`
- `minimumRatings`: optional; no minimum for regular filtered lists
- `platformIds`: optional comma-separated platform IDs (used by PC, Xbox,
  PlayStation, and Nintendo family filters)

Regular user-rating lists are ordered in two groups: games with more than 50
user ratings first, followed by every other game that has a user rating. Each
group is ordered by rating descending. A missing cover does not remove a game
from regular genre or platform results. Reference-array filters use IGDB's
parenthesized list syntax (for example, `genres = (36)`), so games that belong
to several genres are included as expected.
- `minimumCriticRatings`: default 5 for `sort=critics`

Examples:

```text
/v1/games?sort=users&offset=0
/v1/games?sort=top&offset=0
/v1/games?search=witcher&offset=0
/v1/games?genreId=12&sort=users
/v1/games?platformId=6&sort=critics
/v1/games/1942
```

`sort=top` returns at most 100 standalone games, ordered by IGDB user rating.
Games need at least 500 user ratings so a tiny number of votes cannot dominate
the list. Ratings use IGDB's 0-100 scale.

The list response is:

```json
{
  "items": [],
  "pagination": {
    "limit": 20,
    "offset": 0,
    "returned": 0,
    "hasMore": false
  }
}
```

The details response contains the game and all three IGDB time-to-beat values:

```json
{
  "game": {},
  "timeToBeat": {
    "hastily": 0,
    "normally": 0,
    "completely": 0,
    "count": 0
  }
}
```

IGDB time-to-beat values are seconds. Android should convert them to hours.

## Dashboard deployment

If the Worker is edited in the Cloudflare dashboard, paste the contents of
`src/index.js` into **Edit code**, save, and deploy. Add the three required
secrets under **Settings > Variables and secrets**.

Test these URLs before changing Android:

```text
https://YOUR-WORKER.workers.dev/v1/health
https://YOUR-WORKER.workers.dev/v1/games?sort=users
https://YOUR-WORKER.workers.dev/v1/games/1942
https://YOUR-WORKER.workers.dev/v1/genres
```

`/health` reports only whether each integration is configured. It never returns
secret values.
