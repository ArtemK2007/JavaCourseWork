package ua.sumdu.oop.ui;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ua.sumdu.oop.dao.DepartmentDao;
import ua.sumdu.oop.dao.EmployeeDao;
import ua.sumdu.oop.model.Department;
import ua.sumdu.oop.model.DepartmentStats;
import ua.sumdu.oop.model.Employee;
import ua.sumdu.oop.service.JsonService;

import java.io.File;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class MainView {
    private final DepartmentDao departmentDao = new DepartmentDao();
    private final EmployeeDao employeeDao = new EmployeeDao();
    private final JsonService jsonService = new JsonService(departmentDao, employeeDao);

    private final ObservableList<Employee> employeeList = FXCollections.observableArrayList();
    private final ObservableList<Department> departmentList = FXCollections.observableArrayList();

    private TableView<Employee> employeeTable;
    private TableView<Department> departmentTable;

    public void show(Stage stage) {
        // Верхня панель інструментів (JSON Експорт/Імпорт на 100 балів + Топ підрозділ на 85 балів)
        Button exportJsonBtn = new Button("Експорт у JSON");
        Button importJsonBtn = new Button("Імпорт з JSON");
        Button topDeptBtn = new Button("Топ підрозділ (найвища сер. ЗП)");
        topDeptBtn.setStyle("-fx-font-weight: bold;");

        exportJsonBtn.setOnAction(e -> handleExportJson(stage));
        importJsonBtn.setOnAction(e -> handleImportJson(stage));
        topDeptBtn.setOnAction(e -> handleShowTopDepartment());

        HBox topBar = new HBox(10, exportJsonBtn, importJsonBtn, new Separator(), topDeptBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #f0f2f5; -fx-border-color: #dcdcdc; -fx-border-width: 0 0 1 0;");

        TabPane tabPane = new TabPane();
        Tab empTab = new Tab("Працівники", createEmployeesPane());
        empTab.setClosable(false);

        Tab deptTab = new Tab("Підрозділи", createDepartmentsPane());
        deptTab.setClosable(false);

        tabPane.getTabs().addAll(empTab, deptTab);

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(tabPane);

        refreshAllData();

        stage.setTitle("Облік працівників та підрозділів — Варіант 5");
        stage.setScene(new Scene(root, 1020, 620));
        stage.setResizable(true);
        stage.show();
    }

    private VBox createEmployeesPane() {
        employeeTable = new TableView<>(employeeList);
        employeeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<Employee, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getId()));
        idCol.setMaxWidth(60);

        TableColumn<Employee, String> nameCol = new TableColumn<>("ПІБ працівника");
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));

        TableColumn<Employee, String> posCol = new TableColumn<>("Посада");
        posCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPosition()));

        TableColumn<Employee, BigDecimal> salCol = new TableColumn<>("Заробітна плата");
        salCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getSalary()));

        TableColumn<Employee, LocalDate> dateCol = new TableColumn<>("Дата прийняття");
        dateCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getHireDate()));

        TableColumn<Employee, String> deptCol = new TableColumn<>("Підрозділ");
        deptCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDepartmentName()));

        TableColumn<Employee, String> mgrCol = new TableColumn<>("Безпосередній керівник");
        mgrCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getManagerName()));

        employeeTable.getColumns().addAll(idCol, nameCol, posCol, salCol, dateCol, deptCol, mgrCol);

        Button addBtn = new Button("Додати працівника");
        Button editBtn = new Button("Редагувати");
        Button deleteBtn = new Button("Видалити");
        Button changeMgrBtn = new Button("Змінити керівника");

        addBtn.setOnAction(e -> showEmployeeDialog(null));
        editBtn.setOnAction(e -> {
            Employee selected = employeeTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "Увага", "Виберіть працівника для редагування.");
                return;
            }
            showEmployeeDialog(selected);
        });
        deleteBtn.setOnAction(e -> handleDeleteEmployee());
        changeMgrBtn.setOnAction(e -> handleChangeManager());

        HBox crudBar = new HBox(10, addBtn, editBtn, deleteBtn, new Separator(), changeMgrBtn);
        crudBar.setAlignment(Pos.CENTER_LEFT);

        // Панель фільтрації на 75 балів (ЗП вище середньої по відділу + сортування)
        CheckBox aboveAvgCheck = new CheckBox("ЗП вище середньої по відділу");
        ComboBox<String> sortBox = new ComboBox<>(FXCollections.observableArrayList(
                "Сортувати ЗП: за спаданням",
                "Сортувати ЗП: за зростанням"
        ));
        sortBox.getSelectionModel().selectFirst();
        sortBox.setDisable(true);

        Button resetBtn = new Button("Показати всіх");

        Runnable applyFilter = () -> {
            if (aboveAvgCheck.isSelected()) {
                sortBox.setDisable(false);
                boolean asc = sortBox.getSelectionModel().getSelectedIndex() == 1;
                try {
                    employeeList.setAll(employeeDao.findAboveDepartmentAverage(asc));
                } catch (SQLException ex) {
                    showError("Помилка фільтрації", ex);
                }
            } else {
                sortBox.setDisable(true);
                refreshEmployees();
            }
        };

        aboveAvgCheck.setOnAction(e -> applyFilter.run());
        sortBox.setOnAction(e -> applyFilter.run());
        resetBtn.setOnAction(e -> {
            aboveAvgCheck.setSelected(false);
            sortBox.setDisable(true);
            refreshEmployees();
        });

        HBox filterBar = new HBox(10, new Label("Аналітика (75 балів):"), aboveAvgCheck, sortBox, resetBtn);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setPadding(new Insets(6));
        filterBar.setStyle("-fx-background-color: #eef5ff; -fx-background-radius: 5;");

        VBox box = new VBox(10, crudBar, filterBar, employeeTable);
        box.setPadding(new Insets(12));
        VBox.setVgrow(employeeTable, Priority.ALWAYS);
        return box;
    }

    private VBox createDepartmentsPane() {
        departmentTable = new TableView<>(departmentList);
        departmentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<Department, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getId()));
        idCol.setMaxWidth(60);

        TableColumn<Department, String> nameCol = new TableColumn<>("Назва підрозділу");
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));

        TableColumn<Department, String> locCol = new TableColumn<>("Розташування");
        locCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLocation()));

        departmentTable.getColumns().addAll(idCol, nameCol, locCol);

        Button addBtn = new Button("Додати підрозділ");
        Button editBtn = new Button("Редагувати");
        Button deleteBtn = new Button("Видалити");

        addBtn.setOnAction(e -> showDepartmentDialog(null));
        editBtn.setOnAction(e -> {
            Department selected = departmentTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "Увага", "Виберіть підрозділ для редагування.");
                return;
            }
            showDepartmentDialog(selected);
        });
        deleteBtn.setOnAction(e -> handleDeleteDepartment());

        HBox toolbar = new HBox(10, addBtn, editBtn, deleteBtn);
        VBox box = new VBox(10, toolbar, departmentTable);
        box.setPadding(new Insets(12));
        VBox.setVgrow(departmentTable, Priority.ALWAYS);
        return box;
    }

    private void showEmployeeDialog(Employee existing) {
        Dialog<Employee> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Новий працівник" : "Редагування працівника");

        ButtonType saveBtnType = new ButtonType("Зберегти", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        TextField nameField = new TextField(existing != null ? existing.getFullName() : "");
        TextField posField = new TextField(existing != null ? existing.getPosition() : "");
        TextField salField = new TextField(existing != null ? existing.getSalary().toString() : "");
        DatePicker datePicker = new DatePicker(existing != null ? existing.getHireDate() : LocalDate.now());

        ComboBox<Department> deptBox = new ComboBox<>(departmentList);
        if (existing != null) {
            departmentList.stream()
                    .filter(d -> d.getId() == existing.getDepartmentId())
                    .findFirst()
                    .ifPresent(deptBox::setValue);
        } else if (!departmentList.isEmpty()) {
            deptBox.getSelectionModel().selectFirst();
        }

        ObservableList<Employee> possibleManagers = FXCollections.observableArrayList();
        possibleManagers.add(null); // Варіант "Без керівника"
        for (Employee emp : employeeList) {
            if (existing == null || emp.getId() != existing.getId()) {
                possibleManagers.add(emp);
            }
        }
        ComboBox<Employee> mgrBox = new ComboBox<>(possibleManagers);
        mgrBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Employee item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : (item == null ? "— (Без керівника)" : item.toString()));
            }
        });
        mgrBox.setButtonCell(mgrBox.getCellFactory().call(null));

        if (existing != null && existing.getManagerId() != null) {
            possibleManagers.stream()
                    .filter(m -> m != null && m.getId() == existing.getManagerId())
                    .findFirst()
                    .ifPresent(mgrBox::setValue);
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        grid.add(new Label("ПІБ:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Посада:"), 0, 1);
        grid.add(posField, 1, 1);
        grid.add(new Label("Зарплата (грн):"), 0, 2);
        grid.add(salField, 1, 2);
        grid.add(new Label("Дата прийняття:"), 0, 3);
        grid.add(datePicker, 1, 3);
        grid.add(new Label("Підрозділ:"), 0, 4);
        grid.add(deptBox, 1, 4);
        grid.add(new Label("Керівник:"), 0, 5);
        grid.add(mgrBox, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtnType) {
                try {
                    String fullName = nameField.getText().trim();
                    String position = posField.getText().trim();
                    if (fullName.isEmpty() || position.isEmpty()) {
                        throw new IllegalArgumentException("ПІБ та посада не можуть бути порожніми.");
                    }
                    BigDecimal salary = new BigDecimal(salField.getText().trim().replace(",", "."));
                    if (salary.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new IllegalArgumentException("Заробітна плата має бути більшою за 0.");
                    }
                    if (datePicker.getValue() == null || deptBox.getValue() == null) {
                        throw new IllegalArgumentException("Виберіть дату прийняття та підрозділ.");
                    }
                    Employee selectedMgr = mgrBox.getValue();
                    Integer mgrId = selectedMgr != null ? selectedMgr.getId() : null;

                    return new Employee(
                            existing != null ? existing.getId() : 0,
                            fullName,
                            position,
                            salary,
                            datePicker.getValue(),
                            deptBox.getValue().getId(),
                            mgrId
                    );
                } catch (NumberFormatException ex) {
                    showAlert(Alert.AlertType.ERROR, "Помилка вводу", "Некоректний формат заробітної плати.");
                } catch (IllegalArgumentException ex) {
                    showAlert(Alert.AlertType.WARNING, "Помилка валідації", ex.getMessage());
                }
            }
            return null;
        });

        Optional<Employee> result = dialog.showAndWait();
        result.ifPresent(emp -> {
            try {
                if (existing == null) {
                    employeeDao.create(emp);
                } else {
                    employeeDao.update(emp);
                }
                refreshEmployees();
            } catch (SQLException ex) {
                showError("Помилка збереження працівника", ex);
            }
        });
    }

    // Завдання на 85 балів (п. 1): Зміна безпосереднього керівника
    private void handleChangeManager() {
        Employee selected = employeeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Увага", "Виберіть працівника, якому потрібно змінити керівника.");
            return;
        }

        try {
            List<Employee> allEmployees = employeeDao.findAll();
            ObservableList<Employee> candidates = FXCollections.observableArrayList();
            candidates.add(null);
            for (Employee e : allEmployees) {
                if (e.getId() != selected.getId()) {
                    candidates.add(e);
                }
            }

            Dialog<Employee> dialog = new Dialog<>();
            dialog.setTitle("Зміна безпосереднього керівника");
            dialog.setHeaderText("Працівник: " + selected.getFullName());

            ButtonType okBtn = new ButtonType("Оновити керівника", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(okBtn, ButtonType.CANCEL);

            ComboBox<Employee> mgrBox = new ComboBox<>(candidates);
            mgrBox.setPrefWidth(300);
            mgrBox.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Employee item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : (item == null ? "— (Без керівника)" : item.toString()));
                }
            });
            mgrBox.setButtonCell(mgrBox.getCellFactory().call(null));

            if (selected.getManagerId() != null) {
                candidates.stream()
                        .filter(m -> m != null && m.getId() == selected.getManagerId())
                        .findFirst()
                        .ifPresent(mgrBox::setValue);
            }

            VBox content = new VBox(10, new Label("Виберіть нового керівника:"), mgrBox);
            content.setPadding(new Insets(15));
            dialog.getDialogPane().setContent(content);

            dialog.setResultConverter(btn -> btn == okBtn ? mgrBox.getValue() : null);

            Button okButtonNode = (Button) dialog.getDialogPane().lookupButton(okBtn);
            okButtonNode.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
                try {
                    Employee newMgr = mgrBox.getValue();
                    Integer newMgrId = newMgr != null ? newMgr.getId() : null;
                    employeeDao.updateManager(selected.getId(), newMgrId);
                    refreshEmployees();
                } catch (SQLException ex) {
                    showError("Помилка оновлення керівника", ex);
                }
            });

            dialog.showAndWait();
        } catch (SQLException ex) {
            showError("Помилка завантаження списку керівників", ex);
        }
    }

    // Завдання на 85 балів (п. 2): Підрозділ з найбільшою середньою ЗП
    private void handleShowTopDepartment() {
        try {
            DepartmentStats stats = departmentDao.findTopDepartmentByAvgSalary();
            if (stats == null) {
                showAlert(Alert.AlertType.INFORMATION, "Інформація", "У базі даних немає підрозділів із працівниками.");
                return;
            }

            String info = String.format("""
                    Підрозділ-лідер: %s
                    Розташування: %s
                    Кількість працівників: %d
                    Середня заробітна плата: %s грн
                    Мінімальна зарплата: %s грн
                    Максимальна зарплата: %s грн
                    Загальний фонд оплати праці: %s грн
                    """,
                    stats.getDepartmentName(),
                    stats.getLocation(),
                    stats.getEmployeeCount(),
                    stats.getAvgSalary(),
                    stats.getMinSalary(),
                    stats.getMaxSalary(),
                    stats.getTotalPayroll()
            );

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Аналітика (85 балів)");
            alert.setHeaderText("Підрозділ із найбільшою середньою заробітною платою");
            alert.setContentText(info);
            alert.showAndWait();
        } catch (SQLException ex) {
            showError("Помилка розрахунку показників підрозділу", ex);
        }
    }

    // Завдання на 100 балів: Експорт у JSON
    private void handleExportJson(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Експорт даних у JSON");
        fileChooser.setInitialFileName("company_backup.json");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON файли (*.json)", "*.json"));

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try {
                jsonService.exportToFile(file);
                showAlert(Alert.AlertType.INFORMATION, "Експорт завершено",
                        "Дані про підрозділи та працівників успішно збережено у файл:\n" + file.getAbsolutePath());
            } catch (Exception ex) {
                showError("Помилка експорту в JSON", ex);
            }
        }
    }

    // Завдання на 100 балів: Імпорт з JSON
    private void handleImportJson(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Імпорт даних з JSON");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON файли (*.json)", "*.json"));

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try {
                jsonService.importFromFile(file);
                refreshAllData();
                showAlert(Alert.AlertType.INFORMATION, "Імпорт завершено",
                        "Дані успішно імпортовано з JSON у базу даних PostgreSQL.");
            } catch (Exception ex) {
                showError("Помилка імпорту з JSON", ex);
            }
        }
    }

    private void showDepartmentDialog(Department existing) {
        Dialog<Department> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Новий підрозділ" : "Редагування підрозділу");

        ButtonType saveBtnType = new ButtonType("Зберегти", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        TextField nameField = new TextField(existing != null ? existing.getName() : "");
        TextField locField = new TextField(existing != null ? existing.getLocation() : "");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.add(new Label("Назва підрозділу:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Розташування:"), 0, 1);
        grid.add(locField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtnType) {
                String name = nameField.getText().trim();
                String loc = locField.getText().trim();
                if (name.isEmpty() || loc.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Помилка валідації", "Назва та розташування не можуть бути порожніми.");
                    return null;
                }
                return new Department(existing != null ? existing.getId() : 0, name, loc);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(dept -> {
            try {
                if (existing == null) {
                    departmentDao.create(dept);
                } else {
                    departmentDao.update(dept);
                }
                refreshAllData();
            } catch (SQLException ex) {
                showError("Помилка збереження підрозділу", ex);
            }
        });
    }

    private void handleDeleteEmployee() {
        Employee selected = employeeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Увага", "Виберіть працівника для видалення.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Видалити працівника " + selected.getFullName() + "?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.YES) {
                try {
                    employeeDao.delete(selected.getId());
                    refreshEmployees();
                } catch (SQLException ex) {
                    showError("Помилка видалення працівника", ex);
                }
            }
        });
    }

    private void handleDeleteDepartment() {
        Department selected = departmentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Увага", "Виберіть підрозділ для видалення.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Видалити підрозділ \"" + selected.getName() + "\" та всіх його працівників?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.YES) {
                try {
                    departmentDao.delete(selected.getId());
                    refreshAllData();
                } catch (SQLException ex) {
                    showError("Помилка видалення підрозділу", ex);
                }
            }
        });
    }

    private void refreshAllData() {
        try {
            departmentList.setAll(departmentDao.findAll());
            employeeList.setAll(employeeDao.findAll());
        } catch (SQLException ex) {
            showError("Помилка завантаження даних із БД", ex);
        }
    }

    private void refreshEmployees() {
        try {
            employeeList.setAll(employeeDao.findAll());
        } catch (SQLException ex) {
            showError("Помилка оновлення списку працівників", ex);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, Exception ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Виникла помилка при роботі з даними");
        alert.setContentText(ex.getMessage());
        alert.showAndWait();
    }
}