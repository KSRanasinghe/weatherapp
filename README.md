# Weather Comfort Ranking – Backend

Backend for the Weather Comfort Ranking app. Built with Java and Spring Boot. It fetches live weather data for 10 cities, calculates a Comfort Score (0-100) for each one, and returns them ranked from most comfortable to least comfortable.

Frontend repo: https://github.com/KSRanasinghe/weatherapp-frontend

---

## 1. Setup Instructions

**Requirements**
- Java 17 or newer (developed using Java 25)
- Maven
- Internet connection (calls OpenWeatherMap live)

**Steps**
1. Clone this repo.
2. Open in IntelliJ (or any Java IDE).
3. `src/main/resources/application.properties` already has a working OpenWeatherMap API key and the Auth0 issuer URL, so no extra config is needed to run it.
4. Run the app (right-click `Application.java` → Run), or:
   
   ```
   mvn spring-boot:run
   ```
6. Backend runs on `http://localhost:8080`.
7. The frontend needs to be running too for the full login + dashboard flow. See the frontend repo above.

**Test login (used by frontend)**
- Email: `careers@fidenz.com`
- Password: `Pass#fidenz`

**Endpoints to test directly**
- `GET /api/weather-all` – ranked list of cities. Needs a login token from the frontend, returns 401 without one.
- `GET /api/cache-status` – shows HIT/MISS per city. Open, no login needed.

---

## 2. Comfort Index Formula

I used four weather values: Temperature, Humidity, Wind Speed, and Visibility.

**Temperature, Humidity, Wind Speed** all work the same way: each one has a comfortable middle value (an "ideal"), and comfort gets worse the further the real value is from that ideal, in either direction.

Formula for each of these three:
```
score = 100 - |actual value - ideal value| x penalty factor
```

| Parameter | Ideal | Penalty Factor |
|---|---|---|
| Temperature | 22.5 C | 3 |
| Humidity | 50% | 2 |
| Wind Speed | 2.5 m/s | 6 |

**How I picked each penalty factor:**
1. Pick the ideal value.
2. Pick two realistic extremes for that value (a low and a high a city could reasonably have).
3. Work out the distance from the ideal to each extreme: `|extreme - ideal|`.
4. Take the bigger of the two distances.
5. Penalty factor = `100 / that bigger distance`.

This makes sure the worst realistic case lands the score exactly at 0, instead of going negative or stopping too high.

Example for Temperature:
- Ideal = 22.5
- Extremes used: -10 and 40
- Distances: `|-10 - 22.5| = 32.5` and `|40 - 22.5| = 17.5`
- Bigger distance = 32.5
- Penalty factor = `100 / 32.5 ≈ 3`

Same steps for Humidity (extremes 0 and 100, since that's humidity's real range) and Wind Speed (extremes 0 and 20).

**Visibility works differently.** More visibility is always better — there's no "too much" like there is with temperature. So instead of measuring distance from an ideal, I used a straight ratio against OpenWeatherMap's own maximum reported value (10,000 meters):
```
visibilityScore = (visibility / 10000) x 100
```
At 10,000m visibility (max clarity) this gives 100. At 0m (fog) this gives 0.

**Final Comfort Score:**
```
comfortScore = (tempScore x 0.4) + (humidityScore x 0.3) + (windScore x 0.2) + (visibilityScore x 0.1)
```
The result is capped between 0 and 100 in case of any extreme edge-case values.

---

## 3. Reasoning Behind the Weights

- **Temperature (0.4)** – biggest weight, since it's usually the first thing people notice about weather.
- **Humidity (0.3)** – matters a lot too, especially since it makes heat feel worse, but people usually notice it less directly than temperature.
- **Wind Speed (0.2)** – a bit of wind is usually pleasant, only becomes uncomfortable at higher speeds, so smaller weight.
- **Visibility (0.1)** – affects comfort the least directly out of the four, mostly noticeable in extreme cases like fog, so it gets the smallest weight.

These are my own judgment calls. Someone else could reasonably weigh these differently, but I think this order matches how most people actually experience weather.

---

## 4. Trade-offs I Considered

- **Building my own cache with a Java Map** instead of using an existing caching library. More code to write, but I could fully control the 5-minute expiry and show HIT/MISS status per city for the debug endpoint.
- **Only caching the raw weather data, not the final processed/ranked list.** The comfort score math and sorting still run on every request. This is a much smaller, faster operation than the external API call, so caching it separately would save comparatively little.
- **Committing `application.properties` with real values as-is.** Not something I'd do for a real production project, but for this assignment it means reviewers can run the project immediately without extra setup.

---

## 5. Cache Design

A simple in-memory cache using a Java `Map`, storing each city's last fetched weather data along with the time it was fetched.

**How it works:**
1. Request comes in for a city.
2. If there's a saved response for that city less than 5 minutes old, return it. Mark as **HIT**.
3. Otherwise, call OpenWeatherMap again, save the new response with a fresh timestamp, mark as **MISS**.

A separate map tracks HIT/MISS status per city so `/api/cache-status` can show it directly.

**Limitation:** This cache lives in memory only, so it resets if the app restarts. Fine for this assignment's scope.

---

## 6. Known Limitations

- Only 8 cities were given in the sample `cities.json`. Since the assignment asks for a minimum of 10, I added 2 more (London and New York) myself.
- The final processed/ranked list isn't cached separately, only the raw per-city data (see Trade-offs above).
- Cache is in-memory only and resets on restart.
- Real API key and Auth0 config are committed as-is for reviewer convenience, instead of using environment variables like I would for a production project.
