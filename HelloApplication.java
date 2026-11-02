package com.example.nupurattendance;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HelloApplication extends Application {

    private static final String[] DAYS = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

    public static class Student {
        private final SimpleStringProperty id;
        private final SimpleStringProperty name;
        private final Map<String, SimpleStringProperty> dailyStatus = new HashMap<>();
        private final SimpleStringProperty percentage = new SimpleStringProperty("0.0%");

        public Student(String id, String name) {
            this.id = new SimpleStringProperty(id);
            this.name = new SimpleStringProperty(name);

            for (String day : DAYS) {
                dailyStatus.put(day, new SimpleStringProperty(""));
            }
            updatePercentage();
        }

        public String getId() { return id.get(); }
        public void setId(String id) { this.id.set(id); }
        public SimpleStringProperty idProperty() { return id; }

        public String getName() { return name.get(); }
        public void setName(String name) { this.name.set(name); }
        public SimpleStringProperty nameProperty() { return name; }

        public String getDayStatus(String day) { return dailyStatus.get(day).get(); }
        public void setDayStatus(String day, String status) {
            dailyStatus.get(day).set(status);
            updatePercentage();
        }
        public SimpleStringProperty dayStatusProperty(String day) { return dailyStatus.get(day); }

        public SimpleStringProperty percentageProperty() { return percentage; }

        public void updatePercentage() {
            int presentCount = 0;
            int totalMarked = 0;

            for (String day : DAYS) {
                String status = dailyStatus.get(day).get();
                if ("✅".equals(status)) {
                    presentCount++;
                    totalMarked++;
                } else if ("❌".equals(status)) {
                    totalMarked++;
                }
            }

            if (totalMarked == 0) {
                percentage.set("0.0%");
            } else {
                double pct = ((double) presentCount / totalMarked) * 100;
                percentage.set(String.format("%.1f%%", pct));
            }
        }
    }

    private boolean isDarkMode = false;
    private final ObservableList<Student> masterStudentList = FXCollections.observableArrayList();
    private final PieChart pieChart = new PieChart();
    private final Label analyticsTitleLabel = new Label("📈 Quick Analytics (Class)");
    private final Label presentSummaryLabel = new Label("Total Present: 0");
    private final Label absentSummaryLabel = new Label("Total Absent: 0");
    private final Label percentageSummaryLabel = new Label("Attendance Rate: 0.0%");
    private TableView<Student> table;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Advanced Attendance System - Nupur Project");

        // --- HEADER SECTION ---
        Label titleLabel = new Label("📊 Attendance Dashboard");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");

        // Real-Time Date & Time Label
        Label dateTimeLabel = new Label();
        dateTimeLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #ecf0f1;");

        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            LocalTime currentTime = LocalTime.now();
            LocalDate currentDate = LocalDate.now();
            String formattedDateTime = String.format("📅 %s | ⏰ %s",
                    currentDate.format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy")),
                    currentTime.format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
            dateTimeLabel.setText(formattedDateTime);
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();

        ComboBox<String> weekSelector = new ComboBox<>();
        weekSelector.getItems().addAll("Week 1", "Week 2", "Week 3", "Week 4");
        weekSelector.setValue("Week 1");
        weekSelector.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        ToggleButton darkModeToggle = new ToggleButton("🌙 Dark Mode");
        darkModeToggle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        HBox headerBox = new HBox(15, titleLabel, dateTimeLabel, headerSpacer, weekSelector, darkModeToggle);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(12, 18, 12, 18));
        headerBox.setStyle("-fx-background-color: #2b579a;");

        // --- SEARCH TOOLBAR SECTION ---
        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search student by ID or Name...");
        searchField.setPrefWidth(300);
        searchField.setStyle("-fx-font-size: 15px;");

        HBox toolBar = new HBox(15, searchField);
        toolBar.setPadding(new Insets(12));
        toolBar.setAlignment(Pos.CENTER_LEFT);

        // --- TABLE SETUP ---
        table = new TableView<>();
        table.setEditable(true);
        table.setStyle("-fx-font-size: 16px;");

        FilteredList<Student> filteredData = new FilteredList<>(masterStudentList, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(student -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                return student.getName().toLowerCase().contains(lowerCaseFilter) ||
                        student.getId().toLowerCase().contains(lowerCaseFilter);
            });
        });
        table.setItems(filteredData);

        TableColumn<Student, String> colId = new TableColumn<>("Student ID");
        colId.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        colId.setCellFactory(TextFieldTableCell.forTableColumn());
        colId.setOnEditCommit(e -> e.getRowValue().setId(e.getNewValue()));
        colId.setPrefWidth(120);

        TableColumn<Student, String> colName = new TableColumn<>("Student Name");
        colName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        colName.setCellFactory(TextFieldTableCell.forTableColumn());
        colName.setOnEditCommit(e -> e.getRowValue().setName(e.getNewValue()));
        colName.setPrefWidth(180);

        table.getColumns().addAll(colId, colName);

        for (String day : DAYS) {
            TableColumn<Student, String> dayCol = new TableColumn<>(day);
            dayCol.setPrefWidth(75);
            dayCol.setCellValueFactory(cellData -> cellData.getValue().dayStatusProperty(day));

            dayCol.setCellFactory(tc -> new TableCell<Student, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        setAlignment(Pos.CENTER);
                        if ("✅".equals(item)) {
                            setStyle("-fx-background-color: #d4edda; -fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: 18px;");
                        } else if ("❌".equals(item)) {
                            setStyle("-fx-background-color: #f8d7da; -fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: 18px;");
                        } else {
                            setStyle("-fx-font-size: 16px;");
                        }
                    }
                }

                {
                    setOnMouseClicked(event -> {
                        if (!isEmpty()) {
                            Student student = getTableView().getItems().get(getIndex());
                            String currentStatus = student.getDayStatus(day);

                            if (currentStatus.isEmpty()) {
                                student.setDayStatus(day, "✅");
                            } else if ("✅".equals(currentStatus)) {
                                student.setDayStatus(day, "❌");
                            } else {
                                student.setDayStatus(day, "");
                            }
                            updateAnalyticsForSelectedStudent();
                        }
                    });
                }
            });

            table.getColumns().add(dayCol);
        }

        TableColumn<Student, String> colPct = new TableColumn<>("Attendance %");
        colPct.setCellValueFactory(cellData -> cellData.getValue().percentageProperty());
        colPct.setPrefWidth(130);
        colPct.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-font-size: 16px;");
        table.getColumns().add(colPct);

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            updateAnalyticsForSelectedStudent();
        });

        table.setRowFactory(tv -> {
            TableRow<Student> row = new TableRow<>();
            row.setPrefHeight(40);
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && row.isEmpty()) {
                    showAddStudentDialog();
                }
            });
            return row;
        });

        // --- DASHBOARD / PIE CHART PANEL ---
        pieChart.setPrefSize(280, 220);
        pieChart.setLegendVisible(false);

        analyticsTitleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        presentSummaryLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");
        absentSummaryLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #c0392b;");
        percentageSummaryLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2980b9;");

        VBox statsBox = new VBox(12, analyticsTitleLabel, presentSummaryLabel, absentSummaryLabel, percentageSummaryLabel, pieChart);
        statsBox.setPadding(new Insets(15));
        statsBox.setStyle("-fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-color: #f9f9f9;");
        statsBox.setPrefWidth(300);

        HBox centerContent = new HBox(12, table, statsBox);
        HBox.setHgrow(table, Priority.ALWAYS);

        // --- BOTTOM INPUT FORM & ACTIONS ---
        TextField idInput = new TextField();
        idInput.setPromptText("Student ID");
        idInput.setStyle("-fx-font-size: 15px;");

        TextField nameInput = new TextField();
        nameInput.setPromptText("Student Name");
        nameInput.setStyle("-fx-font-size: 15px;");

        Button addButton = new Button("Add Student");
        addButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px;");

        Button deleteButton = new Button("Delete Selected");
        deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px;");

        addButton.setOnAction(e -> {
            if (!idInput.getText().trim().isEmpty() && !nameInput.getText().trim().isEmpty()) {
                masterStudentList.add(new Student(idInput.getText().trim(), nameInput.getText().trim()));
                idInput.clear();
                nameInput.clear();
                updateAnalyticsForSelectedStudent();
            }
        });

        deleteButton.setOnAction(e -> {
            Student selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                masterStudentList.remove(selected);
                updateAnalyticsForSelectedStudent();
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Select a student to delete!");
                alert.showAndWait();
            }
        });

        HBox inputBox = new HBox(12, idInput, nameInput, addButton, deleteButton);
        inputBox.setPadding(new Insets(12));
        inputBox.setAlignment(Pos.CENTER);

        // --- MAIN LAYOUT ---
        VBox mainLayout = new VBox(12, headerBox, toolBar, centerContent, inputBox);
        Scene scene = new Scene(mainLayout, 1300, 680);

        scene.setOnMouseClicked(event -> {
            Node target = (Node) event.getTarget();
            boolean clickedInsideRow = false;

            while (target != null) {
                if (target instanceof TableRow && !((TableRow<?>) target).isEmpty()) {
                    clickedInsideRow = true;
                    break;
                }
                target = target.getParent();
            }

            if (!clickedInsideRow) {
                table.getSelectionModel().clearSelection();
                updateAnalyticsForSelectedStudent();
            }
        });

        darkModeToggle.setOnAction(e -> {
            isDarkMode = darkModeToggle.isSelected();
            if (isDarkMode) {
                darkModeToggle.setText("☀️ Light Mode");
                scene.getRoot().setStyle("-fx-base: #2b2b2b; -fx-background-color: #1e1e1e;");
                headerBox.setStyle("-fx-background-color: #3c3f41;");
                statsBox.setStyle("-fx-border-color: #555555; -fx-background-color: #2a2a2a;");
                analyticsTitleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
            } else {
                darkModeToggle.setText("🌙 Dark Mode");
                scene.getRoot().setStyle("");
                headerBox.setStyle("-fx-background-color: #2b579a;");
                statsBox.setStyle("-fx-border-color: #cccccc; -fx-background-color: #f9f9f9;");
                analyticsTitleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: black;");
            }
        });

        // 📌 UPDATED STUDENT DATA FROM YOUR SCREENSHOT
        masterStudentList.addAll(
                new Student("4420250989", "Nupur Tashnova"),
                new Student("4420250985", "Chakma Sheli"),
                new Student("4420250995", "Bishwas Sharmila")
        );
        updateAnalyticsForSelectedStudent();

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void updateAnalyticsForSelectedStudent() {
        Student selected = table.getSelectionModel().getSelectedItem();

        if (selected != null) {
            analyticsTitleLabel.setText("👤 Analytics: " + selected.getName());

            int presentCount = 0;
            int absentCount = 0;

            for (String day : DAYS) {
                String status = selected.getDayStatus(day);
                if ("✅".equals(status)) presentCount++;
                else if ("❌".equals(status)) absentCount++;
            }

            presentSummaryLabel.setText("Present Days: " + presentCount);
            absentSummaryLabel.setText("Absent Days: " + absentCount);
            percentageSummaryLabel.setText("Attendance Rate: " + selected.percentageProperty().get());

            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                    new PieChart.Data("Present", presentCount),
                    new PieChart.Data("Absent", absentCount)
            );
            pieChart.setData(pieChartData);

        } else {
            analyticsTitleLabel.setText("📈 Quick Analytics (Class)");

            int totalPresent = 0;
            int totalAbsent = 0;

            for (Student student : masterStudentList) {
                for (String day : DAYS) {
                    String status = student.getDayStatus(day);
                    if ("✅".equals(status)) totalPresent++;
                    else if ("❌".equals(status)) totalAbsent++;
                }
            }

            presentSummaryLabel.setText("Total Class Present: " + totalPresent);
            absentSummaryLabel.setText("Total Class Absent: " + totalAbsent);

            int totalMarked = totalPresent + totalAbsent;
            double classPct = totalMarked == 0 ? 0.0 : ((double) totalPresent / totalMarked) * 100;
            percentageSummaryLabel.setText(String.format("Overall Rate: %.1f%%", classPct));

            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                    new PieChart.Data("Present", totalPresent),
                    new PieChart.Data("Absent", totalAbsent)
            );
            pieChart.setData(pieChartData);
        }
    }

    private void showAddStudentDialog() {
        Dialog<Student> dialog = new Dialog<>();
        dialog.setTitle("Add New Student");
        dialog.setHeaderText("Enter Details:");

        ButtonType addBtnType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));

        TextField idField = new TextField();
        idField.setStyle("-fx-font-size: 15px;");
        TextField nameField = new TextField();
        nameField.setStyle("-fx-font-size: 15px;");

        grid.add(new Label("ID:"), 0, 0);
        grid.add(idField, 1, 0);
        grid.add(new Label("Name:"), 0, 1);
        grid.add(nameField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == addBtnType && !idField.getText().trim().isEmpty() && !nameField.getText().trim().isEmpty()) {
                return new Student(idField.getText().trim(), nameField.getText().trim());
            }
            return null;
        });

        Optional<Student> res = dialog.showAndWait();
        res.ifPresent(s -> {
            masterStudentList.add(s);
            updateAnalyticsForSelectedStudent();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}