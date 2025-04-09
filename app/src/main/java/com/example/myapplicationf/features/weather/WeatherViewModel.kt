package com.example.myapplicationf.features.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.random.Random

data class WeatherInfo(
    val temperature: Int,
    val humidity: Int,
    val description: String,
    val date: String
)

data class CropWeatherPlan(
    val cropName: String,
    val idealTemperature: String,
    val idealHumidity: String,
    val recommendations: List<String>,
    val precautions: List<String>
)

data class WeatherState(
    val currentWeather: WeatherInfo? = null,
    val forecast: List<WeatherInfo> = emptyList(),
    val searchQuery: String = "",
    val selectedCrop: CropWeatherPlan? = null
)

class WeatherViewModel : ViewModel() {
    private val _weatherState = MutableStateFlow(WeatherState())
    val weatherState: StateFlow<WeatherState> = _weatherState

    private val cropDatabase = mapOf(
        "sugarcane" to CropWeatherPlan(
            cropName = "Sugarcane",
            idealTemperature = "27-38°C",
            idealHumidity = "70-80%",
            recommendations = listOf(
                "Plant during spring season for optimal growth",
                "Ensure adequate irrigation during dry spells",
                "Maintain proper row spacing for air circulation",
                "Apply mulch to retain soil moisture",
                "Schedule harvesting before winter"
            ),
            precautions = listOf(
                "Protect from frost damage in winter",
                "Ensure proper drainage during monsoon",
                "Watch for pest increase in humid conditions",
                "Provide wind breaks in storm-prone areas",
                "Monitor for drought stress in summer"
            )
        ),
        "ginger" to CropWeatherPlan(
            cropName = "Ginger",
            idealTemperature = "22-30°C",
            idealHumidity = "70-90%",
            recommendations = listOf(
                "Plant at start of monsoon season",
                "Use shade nets during peak summer",
                "Maintain proper soil moisture",
                "Apply organic mulch for temperature control",
                "Ensure good air circulation"
            ),
            precautions = listOf(
                "Avoid waterlogging conditions",
                "Protect from direct sunlight in summer",
                "Monitor for fungal diseases in high humidity",
                "Provide drainage during heavy rains",
                "Watch for rhizome rot in wet conditions"
            )
        ),
        "turmeric" to CropWeatherPlan(
            cropName = "Turmeric",
            idealTemperature = "20-30°C",
            idealHumidity = "70-90%",
            recommendations = listOf(
                "Plant during pre-monsoon showers",
                "Use raised beds for better drainage",
                "Apply organic mulch",
                "Maintain consistent soil moisture",
                "Schedule harvest in dry weather"
            ),
            precautions = listOf(
                "Protect from waterlogging",
                "Watch for leaf spot in wet weather",
                "Monitor root health in heavy rains",
                "Avoid frost exposure",
                "Protect from strong winds"
            )
        ),
        "onion" to CropWeatherPlan(
            cropName = "Onion",
            idealTemperature = "13-24°C",
            idealHumidity = "60-70%",
            recommendations = listOf(
                "Plant in cool season",
                "Ensure proper spacing",
                "Maintain consistent moisture",
                "Use drip irrigation",
                "Schedule harvest in dry weather"
            ),
            precautions = listOf(
                "Protect from extreme heat",
                "Watch for fungal growth in wet conditions",
                "Avoid waterlogging",
                "Monitor for bolting in cold weather",
                "Protect from heavy rains during harvest"
            )
        ),
        "chili" to CropWeatherPlan(
            cropName = "Chili",
            idealTemperature = "20-30°C",
            idealHumidity = "65-75%",
            recommendations = listOf(
                "Use greenhouse in extreme weather",
                "Maintain proper spacing",
                "Provide support structures",
                "Regular pruning for airflow",
                "Monitor soil moisture"
            ),
            precautions = listOf(
                "Protect from frost damage",
                "Watch for fruit drop in high temperatures",
                "Monitor for disease in humid conditions",
                "Provide shade in extreme heat",
                "Protect from strong winds"
            )
        ),
        "soybean" to CropWeatherPlan(
            cropName = "Soybean",
            idealTemperature = "20-30°C",
            idealHumidity = "60-70%",
            recommendations = listOf(
                "Plant after last frost",
                "Maintain proper row spacing",
                "Monitor soil moisture",
                "Use crop rotation",
                "Time planting with rainfall"
            ),
            precautions = listOf(
                "Watch for pod shattering in dry weather",
                "Monitor for rust in humid conditions",
                "Protect from waterlogging",
                "Watch for frost damage",
                "Ensure proper drainage"
            )
        ),
        "groundnut" to CropWeatherPlan(
            cropName = "Groundnut",
            idealTemperature = "25-35°C",
            idealHumidity = "65-75%",
            recommendations = listOf(
                "Plant in well-drained soil",
                "Maintain consistent moisture",
                "Use light irrigation",
                "Monitor soil temperature",
                "Time harvest with dry weather"
            ),
            precautions = listOf(
                "Avoid waterlogging",
                "Watch for leaf spots in humid weather",
                "Protect from late season drought",
                "Monitor for aflatoxin in wet conditions",
                "Ensure proper drying after harvest"
            )
        ),
        "rice" to CropWeatherPlan(
            cropName = "Rice",
            idealTemperature = "20-35°C",
            idealHumidity = "70-85%",
            recommendations = listOf(
                "Best planting time during monsoon season",
                "Ensure proper drainage during heavy rains",
                "Monitor water levels daily",
                "Consider crop insurance during extreme weather",
                "Plan harvesting in dry period"
            ),
            precautions = listOf(
                "Protect from cold stress below 15°C",
                "Provide shade during extreme heat",
                "Prepare for pest increase in humid conditions",
                "Install wind barriers if needed",
                "Watch for lodging in strong winds"
            )
        ),
        "wheat" to CropWeatherPlan(
            cropName = "Wheat",
            idealTemperature = "15-25°C",
            idealHumidity = "50-70%",
            recommendations = listOf(
                "Plant during winter season",
                "Ensure adequate irrigation",
                "Monitor soil moisture regularly",
                "Apply weather-appropriate fertilizers",
                "Time harvest before monsoon"
            ),
            precautions = listOf(
                "Protect from frost damage",
                "Avoid waterlogging",
                "Watch for fungal diseases in humid weather",
                "Prepare for rain during harvest",
                "Monitor for heat stress"
            )
        ),
        "tomato" to CropWeatherPlan(
            cropName = "Tomato",
            idealTemperature = "20-30°C",
            idealHumidity = "65-75%",
            recommendations = listOf(
                "Use greenhouse in extreme weather",
                "Implement drip irrigation",
                "Provide support structures",
                "Regular pruning for air circulation",
                "Monitor fruit development"
            ),
            precautions = listOf(
                "Protect from frost and cold",
                "Use shade cloth in extreme heat",
                "Monitor for disease in high humidity",
                "Secure plants before storms",
                "Watch for blossom end rot"
            )
        ),
        "cotton" to CropWeatherPlan(
            cropName = "Cotton",
            idealTemperature = "25-35°C",
            idealHumidity = "50-60%",
            recommendations = listOf(
                "Plant after last frost",
                "Maintain consistent irrigation",
                "Monitor soil temperature",
                "Plan harvest before rainy season"
            ),
            precautions = listOf(
                "Protect from waterlogging",
                "Watch for pest increase in warm weather",
                "Prepare for wind damage",
                "Monitor for heat stress"
            )
        ),
        "potato" to CropWeatherPlan(
            cropName = "Potato",
            idealTemperature = "15-25°C",
            idealHumidity = "60-70%",
            recommendations = listOf(
                "Plant in cool season",
                "Maintain consistent soil moisture",
                "Use mulch for temperature control",
                "Monitor soil temperature"
            ),
            precautions = listOf(
                "Protect from frost damage",
                "Avoid exposure to high heat",
                "Watch for disease in wet conditions",
                "Ensure good drainage"
            )
        )
    )

    init {
        loadWeatherData()
    }

    private fun loadWeatherData() {
        viewModelScope.launch {
            val current = WeatherInfo(
                temperature = Random.nextInt(20, 35),
                humidity = Random.nextInt(40, 90),
                description = getRandomWeatherDescription(),
                date = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
            )

            val forecast = (1..5).map {
                WeatherInfo(
                    temperature = Random.nextInt(18, 37),
                    humidity = Random.nextInt(40, 90),
                    description = getRandomWeatherDescription(),
                    date = LocalDate.now().plusDays(it.toLong()).format(DateTimeFormatter.ISO_DATE)
                )
            }

            _weatherState.value = WeatherState(
                currentWeather = current,
                forecast = forecast
            )
        }
    }

    fun updateSearchQuery(query: String) {
        _weatherState.value = _weatherState.value.copy(
            searchQuery = query,
            selectedCrop = if (query.isEmpty()) null else searchCrop(query)
        )
    }

    private fun searchCrop(query: String): CropWeatherPlan? {
        return cropDatabase.entries.find { (key, _) ->
            key.contains(query.trim().lowercase())
        }?.value
    }

    private fun getRandomWeatherDescription(): String {
        return listOf(
            "Sunny",
            "Partly Cloudy",
            "Cloudy",
            "Light Rain",
            "Heavy Rain",
            "Thunderstorm",
            "Clear Sky",
            "Overcast"
        ).random()
    }
}
