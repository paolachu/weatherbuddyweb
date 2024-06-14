const apiUrl = 'http://localhost:8080/weather';

function getWeatherIcon(description) {
    const icons = {
        "Céu limpo": "wi-day-sunny",
        "Poucas nuvens": "wi-day-cloudy",
        "Nuvens dispersas": "wi-cloud",
        "Nuvens quebradas": "wi-cloudy",
        "Nublado": "wi-cloudy",
        "Chuva leve": "wi-day-sprinkle",
        "Chuva moderada": "wi-rain",
        "Chuva forte": "wi-rain-wind"
    };
    return icons[description] || "wi-na";
}

function formatDate(datetime) {
    const [date, time] = datetime.split(' ');
    const [year, month, day] = date.split('-');
    const [hour, minute] = time.split(':');
    return {
        date: `${day}/${month}`,
        time: `${hour}:${minute}`
    };
}

function setWeatherClass(description, hour) {
    const body = document.body;
    let weatherClass = '';

    if (description.includes('Céu limpo')) {
        weatherClass = 'clear';
    } else if (description.includes('Nublado') || description.includes('Nuvens')) {
        weatherClass = 'cloudy';
    } else if (description.includes('Chuva')) {
        weatherClass = 'rainy';
    } else if (description.includes('Neve')) {
        weatherClass = 'snowy';
    }

    let timeClass = '';
    if (hour >= 6 && hour < 18) {
        timeClass = 'day';
    } else {
        timeClass = 'night';
    }

    body.className = `${weatherClass} ${timeClass}`;
}

function displayWeatherData(data) {
    if (!data || !data.current || !data.forecast) {
        console.error('Invalid weather data:', data);
        return;
    }

    const location = data.location;
    const currentWeatherData = data.current;
    const forecastData = data.forecast;
    const currentIcon = getWeatherIcon(currentWeatherData.description);

    const currentHour = new Date().getHours();
    setWeatherClass(currentWeatherData.description, currentHour);

    document.getElementById('current-weather-data').innerHTML = `
        <h3>Localização: ${location}</h3>
        <p>Condições Atuais:</p>
        <i class="wi ${currentIcon}" style="font-size: 48px;"></i>
        <p>${currentWeatherData.description}</p>
        <p>Temperatura: ${currentWeatherData.temperature}°C</p>
        <p>Pressão: ${currentWeatherData.pressure} hPa</p>
        <p>Umidade: ${currentWeatherData.humidity}%</p>
    `;

    const groupedForecasts = forecastData.reduce((acc, forecast) => {
        const { date, time } = formatDate(forecast.datetime);
        if (!acc[date]) {
            acc[date] = [];
        }
        acc[date].push({ ...forecast, formattedTime: time });
        return acc;
    }, {});

    const forecastHtml = Object.keys(groupedForecasts).map(date => {
        const dailyForecasts = groupedForecasts[date].map(forecast => {
            const forecastIcon = getWeatherIcon(forecast.description);
            return `
                <div class="forecast-item">
                    <p>${forecast.formattedTime}</p>
                    <i class="wi ${forecastIcon}" style="font-size: 48px;"></i>
                    <p>${forecast.description}</p>
                    <p>Temperatura: ${forecast.temperature}°C</p>
                </div>
            `;
        }).join('');
        return `
            <div class="forecast-day">
                <h4>${date}</h4>
                <div class="forecast-container">
                    ${dailyForecasts}
                </div>
            </div>
        `;
    }).join('');

    document.getElementById('forecast-data').innerHTML = forecastHtml;

    document.getElementById('current-weather').style.display = 'block';
    document.getElementById('forecast').style.display = 'block';
}

function getLocation() {
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(position => {
            const lat = position.coords.latitude;
            const lon = position.coords.longitude;
            getWeatherData(null, lat, lon);
        }, showError);
    } else {
        console.error('Geolocation is not supported by this browser.');
    }
}

function showError(error) {
    switch (error.code) {
        case error.PERMISSION_DENIED:
            console.error('User denied the request for Geolocation.');
            break;
        case error.POSITION_UNAVAILABLE:
            console.error('Location information is unavailable.');
            break;
        case error.TIMEOUT:
            console.error('The request to get user location timed out.');
            break;
        case error.UNKNOWN_ERROR:
            console.error('An unknown error occurred.');
            break;
    }
}

async function getWeatherData(city, lat, lon) {
    try {
        let url = apiUrl;
        if (city) {
            url += `?city=${city}`;
        } else if (lat && lon) {
            url += `?lat=${lat}&lon=${lon}`;
        }
        console.log(`Fetching weather data from: ${url}`);
        const response = await fetch(url);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const data = await response.json();
        console.log('Weather data received:', data);
        displayWeatherData(data);
    } catch (error) {
        console.error('Error fetching weather data:', error);
    }
}

document.getElementById('search-button').addEventListener('click', () => {
    const city = document.getElementById('city-input').value;
    if (city) {
        getWeatherData(city);
    } else {
        getLocation();
    }
});

window.onload = getLocation;
