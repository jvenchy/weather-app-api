import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;

class Weather {
    private double temperature;
    private String condition;

    // Method to fetch weather data from OpenWeatherMap API
    public void currentWeather(String location) {
        try {
            String apiKey = "";
            String encodedLocation = URLEncoder.encode(location, StandardCharsets.UTF_8.toString());
            String urlString = "https://api.openweathermap.org/data/2.5/weather?q=" +
                    encodedLocation + "&appid=" + apiKey + "&units=metric";
            System.out.println("Request URL: " + urlString);
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                Scanner scanner = new Scanner(connection.getInputStream());
                String response = scanner.useDelimiter("\\A").next();
                scanner.close();

                JSONObject json = new JSONObject(response);
                JSONObject main = json.getJSONObject("main");
                temperature = main.getDouble("temp");
                JSONArray weatherArray = json.getJSONArray("weather");
                condition = weatherArray.getJSONObject(0).getString("main");
            } else {
                System.out.println("Error fetching weather data: " + responseCode);
                Scanner scanner = new Scanner(connection.getErrorStream());
                String errorResponse = scanner.useDelimiter("\\A").next();
                scanner.close();
                System.out.println("Error response: " + errorResponse);
                temperature = 0;
                condition = "Error";
            }
        } catch (Exception e) {
            e.printStackTrace();
            temperature = 0;
            condition = "Error";
        }
    }

    public double getTemperature() {
        return temperature;
    }

    public String getCondition() {
        return condition;
    }

    public String clothingSuggestion() {
        switch (condition.toLowerCase()) {
            case "clear":
            case "sunny":
                return "Wear light clothes and sunglasses!";
            case "clouds":
            case "partly cloudy":
                return "A light jacket might be useful.";
            case "rain":
            case "drizzle":
                return "Don't forget your umbrella and raincoat!";
            case "snow":
                return "Wear warm layers and a heavy coat.";
            default:
                return "Dress according to the weather!";
        }
    }

    public Color backgroundColor() {
        switch (condition.toLowerCase()) {
            case "clear":
            case "sunny":
                return new Color(255, 255, 204); // Light yellow
            case "clouds":
            case "partly cloudy":
                return new Color(204, 229, 255); // Light blue
            case "rain":
            case "drizzle":
                return new Color(153, 153, 255); // Light gray-blue
            case "snow":
                return new Color(224, 224, 224); // Light gray
            default:
                return Color.WHITE;
        }
    }

    public String emoji() {
        switch (condition.toLowerCase()) {
            case "clear":
            case "sunny":
                return "☀️";
            case "clouds":
            case "partly cloudy":
                return "☁️";
            case "rain":
            case "drizzle":
                return "🌧️";
            case "snow":
                return "❄️";
            default:
                return "❓";
        }
    }
}

class WeatherFrame extends JFrame {
    private Weather weather;
    private JLabel cityLabel;
    private JLabel temperatureLabel;
    private JLabel conditionLabel;
    private JLabel emojiLabel;
    private JLabel suggestionLabel;
    private JComboBox<String> cityDropdown;
    private boolean isCelsius = true; // Default to Celsius

    public WeatherFrame(Weather weather) {
        this.weather = weather;
        setTitle("Weather App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // City Label
        cityLabel = new JLabel("Toronto");
        cityLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        cityLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // City Dropdown
        String[] cities = { "Toronto", "Tokyo", "Los Angeles" };
        cityDropdown = new JComboBox<>(cities);
        cityDropdown.setMaximumSize(new Dimension(200, 30));
        cityDropdown.setAlignmentX(Component.CENTER_ALIGNMENT);
        cityDropdown.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedCity = (String) cityDropdown.getSelectedItem();
                cityLabel.setText(selectedCity);
                weather.currentWeather(selectedCity);
                updateWeatherUI();
            }
        });

        // Temperature Label (toggleable)
        temperatureLabel = new JLabel();
        temperatureLabel.setFont(new Font("Arial", Font.BOLD, 40));
        temperatureLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        temperatureLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                toggleTemperature();
            }
        });

        // Condition Label
        conditionLabel = new JLabel();
        conditionLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        conditionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Emoji Label
        emojiLabel = new JLabel();
        emojiLabel.setFont(new Font("Arial", Font.PLAIN, 48));
        emojiLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Suggestion Label
        suggestionLabel = new JLabel();
        suggestionLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        suggestionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add components to the frame with spacing
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(cityLabel);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(cityDropdown);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(temperatureLabel);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(conditionLabel);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(emojiLabel);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(suggestionLabel);
        add(Box.createRigidArea(new Dimension(0, 20)));

        updateWeatherUI();
    }

    private void toggleTemperature() {
        isCelsius = !isCelsius;
        updateWeatherUI();
    }

    private void updateWeatherUI() {
        double temp = weather.getTemperature();
        String tempUnit = isCelsius ? "°C" : "°F";
        temperatureLabel.setText(String.format("%.1f %s", isCelsius ? temp : (temp * 9 / 5) + 32, tempUnit));
        conditionLabel.setText(weather.getCondition());
        emojiLabel.setText(weather.emoji());
        suggestionLabel.setText(weather.clothingSuggestion());
        getContentPane().setBackground(weather.backgroundColor());
    }
}

public class WeatherApp {
    public static void main(String[] args) {
        Weather weather = new Weather();
        weather.currentWeather("Toronto"); // Default location

        WeatherFrame frame = new WeatherFrame(weather);
        frame.setSize(400, 400);
        frame.setLocationRelativeTo(null); // Center the frame
        frame.setVisible(true);
    }
}