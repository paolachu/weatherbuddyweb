package Projeto.Java;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@SpringBootApplication
@RestController
public class Aplicativo_clima {

    private static final String API_KEY = "ba12429249f1001d5a938308548c96b2";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s";
    private static final String FORECAST_URL = "https://api.openweathermap.org/data/2.5/forecast?q=%s&appid=%s";
    private static final String REVERSE_GEOCODING_URL = "http://api.openweathermap.org/geo/1.0/reverse?lat=%f&lon=%f&appid=%s";
    private static final String COORDINATES_URL = "https://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&appid=%s";

    public static void main(String[] args) {
        SpringApplication.run(Aplicativo_clima.class, args);
    }

    @GetMapping("/weather")
    @ResponseBody
    public WeatherResponse getWeather(@RequestParam(required = false) String city,
                                      @RequestParam(required = false) Double lat,
                                      @RequestParam(required = false) Double lon) throws IOException {
        if (lat != null && lon != null) {
            return getWeatherByCoordinates(lat, lon);
        } else if (city != null && !city.isEmpty()) {
            return getWeatherByCity(city);
        } else {
            throw new IllegalArgumentException("Please provide either city name or coordinates.");
        }
    }

    private WeatherResponse getWeatherByCoordinates(double lat, double lon) throws IOException {
        String weatherJson = getWeatherJson(String.format(COORDINATES_URL, lat, lon, API_KEY));
        JsonObject currentWeatherJson = JsonParser.parseString(weatherJson).getAsJsonObject();
        CurrentWeather currentWeather = formatCurrentWeather(currentWeatherJson);
        String city = getPlaceName(lat, lon);
        List<ForecastWeather> forecastWeather = formatForecastWeather(city);
        WeatherResponse response = new WeatherResponse();
        response.setLocation(city);
        response.setCurrent(currentWeather);
        response.setForecast(forecastWeather);
        return response;
    }

    private WeatherResponse getWeatherByCity(String city) throws IOException {
        String cidadeEncoded = URLEncoder.encode(city, StandardCharsets.UTF_8.toString());
        String weatherJson = getWeatherJson(String.format(BASE_URL, cidadeEncoded, API_KEY));
        JsonObject currentWeatherJson = JsonParser.parseString(weatherJson).getAsJsonObject();
        JsonObject location = currentWeatherJson.getAsJsonObject("coord");
        double latitude = location.get("lat").getAsDouble();
        double longitude = location.get("lon").getAsDouble();
        String placeName = currentWeatherJson.get("name").getAsString(); // Use o nome da cidade da resposta do OpenWeatherMap
        CurrentWeather currentWeather = formatCurrentWeather(currentWeatherJson);
        List<ForecastWeather> forecastWeather = formatForecastWeather(placeName); // Passa o nome da cidade corretamente
        WeatherResponse response = new WeatherResponse();
        response.setLocation(placeName);
        response.setCurrent(currentWeather);
        response.setForecast(forecastWeather);
        return response;
    }


    private static String getWeatherJson(String url) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        HttpResponse httpResponse = httpClient.execute(httpGet);
        HttpEntity httpEntity = httpResponse.getEntity();
        return EntityUtils.toString(httpEntity);
    }

    private static String getPlaceName(double latitude, double longitude) throws IOException {
        String url = String.format(REVERSE_GEOCODING_URL, latitude, longitude, API_KEY);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        HttpResponse httpResponse = httpClient.execute(httpGet);
        HttpEntity httpEntity = httpResponse.getEntity();
        String responseString = EntityUtils.toString(httpEntity);
        JsonArray jsonResponse = JsonParser.parseString(responseString).getAsJsonArray();
        if (jsonResponse.size() > 0) {
            JsonObject location = jsonResponse.get(0).getAsJsonObject();
            return location.get("name").getAsString();
        } else {
            throw new IOException("Error in reverse geocoding: empty response.");
        }
    }

    private static CurrentWeather formatCurrentWeather(JsonObject jsonObject) {
        JsonObject main = jsonObject.getAsJsonObject("main");
        JsonArray weatherArray = jsonObject.getAsJsonArray("weather");

        if (main == null || weatherArray == null || weatherArray.size() == 0) {
            throw new IllegalArgumentException("Error getting current weather data.");
        }

        JsonObject weatherObj = weatherArray.get(0).getAsJsonObject();
        String description = translateDescription(weatherObj.getAsJsonPrimitive("description").getAsString());
        double temperatureKelvin = main.getAsJsonPrimitive("temp").getAsDouble();
        double temperatureCelsius = temperatureKelvin - 273.15;
        double pressure = main.getAsJsonPrimitive("pressure").getAsDouble();
        int humidity = main.getAsJsonPrimitive("humidity").getAsInt();

        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.forLanguageTag("pt-BR")));

        CurrentWeather currentWeather = new CurrentWeather();
        currentWeather.setDescription(description);
        currentWeather.setTemperature(decimalFormat.format(temperatureCelsius));
        currentWeather.setPressure(decimalFormat.format(pressure));
        currentWeather.setHumidity(humidity);
        return currentWeather;
    }

    private static List<ForecastWeather> formatForecastWeather(String city) throws IOException {
        if (city == null || city.isEmpty()) {
            throw new IllegalArgumentException("City name cannot be null or empty");
        }

        String cidadeEncoded = URLEncoder.encode(city, StandardCharsets.UTF_8.toString());
        String forecastJson = getWeatherJson(String.format(FORECAST_URL, cidadeEncoded, API_KEY));
        JsonObject forecastObject = JsonParser.parseString(forecastJson).getAsJsonObject();
        JsonArray forecastList = forecastObject.getAsJsonArray("list");

        if (forecastList == null || forecastList.size() == 0) {
            throw new IllegalArgumentException("Error getting forecast weather data.");
        }

        List<ForecastWeather> forecastWeatherList = new ArrayList<>();
        for (int i = 0; i < forecastList.size(); i++) {
            JsonObject forecastObj = forecastList.get(i).getAsJsonObject();
            JsonObject main = forecastObj.getAsJsonObject("main");
            JsonArray weatherArray = forecastObj.getAsJsonArray("weather");

            if (main == null || weatherArray == null || weatherArray.size() == 0) {
                continue;
            }

            JsonObject weatherObj = weatherArray.get(0).getAsJsonObject();
            String description = translateDescription(weatherObj.getAsJsonPrimitive("description").getAsString());
            double temperatureKelvin = main.getAsJsonPrimitive("temp").getAsDouble();
            double temperatureCelsius = temperatureKelvin - 273.15;

            DecimalFormat decimalFormat = new DecimalFormat("#.##");
            decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.forLanguageTag("pt-BR")));

            ForecastWeather forecastWeather = new ForecastWeather();
            forecastWeather.setDatetime(forecastObj.getAsJsonPrimitive("dt_txt").getAsString());
            forecastWeather.setDescription(description);
            forecastWeather.setTemperature(decimalFormat.format(temperatureCelsius));

            forecastWeatherList.add(forecastWeather);
        }
        return forecastWeatherList;
    }

    private static String translateDescription(String description) {
        switch (description) {
            case "clear sky":
                return "Céu limpo";
            case "few clouds":
                return "Poucas nuvens";
            case "scattered clouds":
                return "Nuvens dispersas";
            case "broken clouds":
                return "Nuvens quebradas";
            case "overcast clouds":
                return "Nublado";
            case "light rain":
                return "Chuva leve";
            case "moderate rain":
                return "Chuva moderada";
            case "heavy rain":
                return "Chuva forte";
            default:
                return description;
        }
    }

    public static class WeatherResponse {
        private String location;
        private CurrentWeather current;
        private List<ForecastWeather> forecast;

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public CurrentWeather getCurrent() {
            return current;
        }

        public void setCurrent(CurrentWeather current) {
            this.current = current;
        }

        public List<ForecastWeather> getForecast() {
            return forecast;
        }

        public void setForecast(List<ForecastWeather> forecast) {
            this.forecast = forecast;
        }
    }

    public static class CurrentWeather {
        private String description;
        private String temperature;
        private String pressure;
        private int humidity;

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getTemperature() {
            return temperature;
        }

        public void setTemperature(String temperature) {
            this.temperature = temperature;
        }

        public String getPressure() {
            return pressure;
        }

        public void setPressure(String pressure) {
            this.pressure = pressure;
        }

        public int getHumidity() {
            return humidity;
        }

        public void setHumidity(int humidity) {
            this.humidity = humidity;
        }
    }

    public static class ForecastWeather {
        private String datetime;
        private String description;
        private String temperature;

        public String getDatetime() {
            return datetime;
        }

        public void setDatetime(String datetime) {
            this.datetime = datetime;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getTemperature() {
            return temperature;
        }

        public void setTemperature(String temperature) {
            this.temperature = temperature;
        }
    }
}
