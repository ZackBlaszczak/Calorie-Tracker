import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import java.time.LocalDate;

public class CalorieTrackerGUI extends Application {

    private CalorieTracker tracker;

    @Override
    public void start(Stage primaryStage) {
        tracker = new CalorieTracker();

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        Label nameLabel = new Label("Name:");
        TextField nameField = new TextField();
        Label caloriesLabel = new Label("Calories:");
        TextField caloriesField = new TextField();
        Button addButton = new Button("Add Meal");
        Label totalCaloriesLabel = new Label("Total Calories for Today: ");
        DatePicker datePicker = new DatePicker();

        gridPane.add(nameLabel, 0, 0);
        gridPane.add(nameField, 1, 0);
        gridPane.add(caloriesLabel, 0, 1);
        gridPane.add(caloriesField, 1, 1);
        gridPane.add(addButton, 0, 2);
        gridPane.add(datePicker, 1, 2);
        gridPane.add(totalCaloriesLabel, 0, 3, 2, 1);

        addButton.setOnAction(event -> {
            String name = nameField.getText();
            int calories = Integer.parseInt(caloriesField.getText());
            LocalDate date = datePicker.getValue();
            Meal meal = new Meal(name, calories, date);
            tracker.addMeal(meal);
            nameField.clear();
            caloriesField.clear();
        });

        datePicker.setOnAction(event -> {
            LocalDate selectedDate = datePicker.getValue();
            int totalCalories = tracker.calculateTotalCaloriesForDay(selectedDate);
            totalCaloriesLabel.setText("Total Calories for " + selectedDate + ": " + totalCalories);
        });

        Scene scene = new Scene(gridPane, 300, 200);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Calorie Tracker");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}




