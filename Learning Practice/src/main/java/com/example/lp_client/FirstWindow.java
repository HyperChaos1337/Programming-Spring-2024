package com.example.lp_client;

import com.google.zxing.WriterException;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

public class FirstWindow extends Application {

    int height = 800, width = 600;

    SQLClient client = new SQLClient();

    TextField applicantField, managerField, addressField,
            matterField, contentsField, numberField;
    Label mainLabel, applicantLabel, managerLabel, addressLabel,
            matterLabel, contentsLabel, statusLabel, qrStatusLabel;

    Button connectButton = new Button("Подключение к бд");

    Button userButton = new Button("Я заявитель");
    Button chancelleryButton = new Button("Я работник канцелярии");
    Button firstWorkerButton = new Button("Я работник офиса 1");
    Button secondWorkerButton = new Button("Я работник офиса 2");

    Button senderButton = new Button("Отправить заявку");

    Button returnButton = new Button("Выйти из приложения");

    Button getButton = new Button("Посмотреть номера текущих заявок");
    Button printRequestButton = new Button("Перейти к заявке под номером");
    Button encodeButton = new Button("Записать обращение в QR-Код");
    Button decodeButton = new Button("Декодировать QR-Код");

    public void connectToDataBase(){
        try {
            client.connect("jdbc:postgresql://localhost:5432/Office1",
                    "postgres", "ChaosNova2020");
            if(client.isConnected()){
                senderButton.setDisable(false);
                statusLabel.setTextFill(Color.GREEN);
                statusLabel.setText("Подключение выполнено успешно. Можете заполнить анкету");
                connectButton.setVisible(false);
            }
        }catch (SQLException e){
            statusLabel.setTextFill(Color.RED);
            statusLabel.setText("Ошибка подключения к БД");
        }
    }

    public void sendToDataBase(){
        String applicant = applicantField.getText();
        String manager = managerField.getText();
        String address = addressField.getText();
        String matter = matterField.getText();
        String contents = contentsField.getText();
        if(applicant.isEmpty() || manager.isEmpty() || address.isEmpty()
                || matter.isEmpty() || contents.isEmpty()){
            statusLabel.setTextFill(Color.RED);
            statusLabel.setText("Не все поля заполнены");
        }
        else{
            try {
                int id = IDGenerator.generateUniqueID();
                client.insertData(id, applicant, manager, address,
                        matter, contents, "Рассматривается", "По усмотрению руководителя");
                statusLabel.setTextFill(Color.GREEN);
                statusLabel.setText("Заявка принята! Ее номер: " + String.valueOf(id));
                applicantField.clear();
                managerField.clear();
                addressField.clear();
                matterField.clear();
                contentsField.clear();
            } catch (SQLException e) {
                statusLabel.setTextFill(Color.RED);
                statusLabel.setText("Не удалось вставить данные. Повторите попытку");
            }
        }
    }

    public String[] getFromDataBase(){
        return client.getEachID().split("; ");
    }

    public void printReceivedData(GridPane gridPane){
        numberField = new TextField();
        int row = 0, col = 0;
        for(String data: getFromDataBase()){
            String filename = "request_ID_" + data + ".png";
            File file = new File(filename);
            if (!file.exists()) {
                Label idLabel = new Label(data);
                gridPane.add(idLabel, row, col);
                col++;
                if (col == 5) {
                    col = 0;
                    row++;
                }
            }
        }

    }

    GridPane getDataFromID() throws SQLException {
        int id = Integer.parseInt(numberField.getText());
        String data = client.getCurrentID(id);
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(50);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        String[] entries = data.split("; ");
        for (int i = 0; i < entries.length; i++) {
            String[] parts = entries[i].split(": ");
            if (parts.length == 2) {
                if(Objects.equals(parts[0], "status"))
                    parts[1] = "На рассмотрении";
                Label columnName = new Label(parts[0]);
                Label columnValue = new Label(parts[1]);
                columnValue.setPrefWidth(400);
                columnValue.setAlignment(Pos.CENTER_LEFT);
                columnValue.setAlignment(Pos.CENTER_RIGHT);
                grid.add(columnName, 0, i);
                grid.add(columnValue, 1, i);
            }
        }
        return grid;
    }

    void encodeSingleData() throws SQLException, IOException, WriterException {
        int id = Integer.parseInt(numberField.getText());
        String data = client.getCurrentID(id);
        QRCodeTool.generateQRCode(data, id);
    }

    void decodeSingleData(){
        String id = numberField.getText();
        String data = QRCodeTool.decodeQRCode("request_ID_" + id + ".png");
        System.out.println(data);
    }

    void connectionMenu(Stage stage){
        VBox vBox = new VBox(connectButton);
        connectButton.setOnAction(actionEvent -> connectToDataBase());
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(20);
        Scene scene = new Scene(vBox, height, width);
        stage.setScene(scene);
        stage.setTitle("Подключение");
        stage.show();
    }

    void mainMenu(Stage stage){
        VBox vBox = new VBox(userButton, chancelleryButton, firstWorkerButton, secondWorkerButton);
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(20);
        Scene scene = new Scene(vBox, height, width);
        stage.setScene(scene);
        stage.setTitle("Авторизация");
        stage.show();
    }

    public void userMenu(Stage stage){

        mainLabel = new Label("Добро пожаловать в сервис социальных услуг! Заполните анкету");
        applicantLabel = new Label("Ваше ФИО");
        managerLabel = new Label("ФИО руководителя");
        addressLabel = new Label("Адрес проживания");
        matterLabel = new Label("Причина заявки");
        contentsLabel = new Label("Пояснение к причине");
        statusLabel = new Label();

        applicantField = new TextField();
        managerField = new TextField();
        addressField = new TextField();
        matterField = new TextField();
        contentsField = new TextField();

        GridPane menuPane = new GridPane();
        menuPane.setMaxSize(height, width);
        menuPane.setAlignment(Pos.CENTER);
        menuPane.setMaxSize(height, width);
        menuPane.setHgap(10);
        menuPane.setVgap(10);

        applicantField.setPrefWidth(400);

        menuPane.add(applicantLabel, 0, 0);
        menuPane.add(applicantField, 1, 0);
        menuPane.add(managerLabel, 0, 1);
        menuPane.add(managerField, 1, 1);
        menuPane.add(addressLabel, 0, 2);
        menuPane.add(addressField, 1, 2);
        menuPane.add(matterLabel, 0, 3);
        menuPane.add(matterField, 1, 3);
        menuPane.add(contentsLabel, 0, 4);
        menuPane.add(contentsField, 1, 4);

        VBox vBox = new VBox(returnButton, mainLabel);

        senderButton.setOnAction(actionEvent -> sendToDataBase());
        vBox.getChildren().add(menuPane);
        vBox.getChildren().add(senderButton);
        vBox.getChildren().add(statusLabel);
        vBox.setAlignment(Pos.CENTER);
        vBox.setMaxSize(height, width);
        Scene scene = new Scene(vBox, height, width);
        vBox.setSpacing(10);
        stage.setScene(scene);
        stage.setTitle("Клиент");
        stage.show();

    }

    public void chancelleryMenu(Stage stage){
        VBox root = new VBox(10); // Создаем VBox для размещения кнопки и GridPane
        qrStatusLabel = new Label();
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(25, 25, 25, 25));
        root.getChildren().add(qrStatusLabel);
        root.getChildren().add(returnButton);
        root.getChildren().add(getButton);
        getButton.setVisible(true);
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        getButton.setOnAction(actionEvent -> {
            if(getFromDataBase().length == 0){
                qrStatusLabel.setTextFill(Color.YELLOWGREEN);
                qrStatusLabel.setText("На данный момент все заявки рассмотрены. Вернитесь позже");
                getButton.setVisible(false);
            }
            printReceivedData(gridPane);
            if(gridPane.getChildren().isEmpty()){
                qrStatusLabel.setTextFill(Color.YELLOWGREEN);
                qrStatusLabel.setText("На данный момент все заявки закодированы. Вернитесь позже");
                getButton.setVisible(false);
            }
            else{
                gridPane.add(printRequestButton, 0, 6);
                gridPane.add(numberField, 1, 6);
                getButton.setVisible(true);
            }
        });
        root.getChildren().add(gridPane);
        Scene scene = new Scene(root, height, width);
        stage.setScene(scene);
        stage.setTitle("Канцелярия");
        stage.show();
    }

    public void printMenu(Stage stage) throws SQLException {
        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(20);
        GridPane dataGrid = getDataFromID();
        vBox.getChildren().add(dataGrid);
        vBox.getChildren().add(encodeButton);
        encodeButton.setOnAction(actionEvent -> {
            chancelleryMenu(stage);
            try {
                encodeSingleData();
                qrStatusLabel.setTextFill(Color.GREEN);
                qrStatusLabel.setText("QR-код обращения " + numberField.getText() + " успешно создан!");
            } catch (SQLException | IOException | WriterException e) {
                //throw new RuntimeException(e);
                qrStatusLabel.setTextFill(Color.RED);
                qrStatusLabel.setText("Не удалось создать QR-код обращения " + numberField.getText() +
                ". Повторите попытку позже");
            }
        });
        Scene scene = new Scene(vBox, height, width);
        stage.setScene(scene);
        stage.setTitle("Обращение");
        stage.show();
    }

    public void firstWorkerMenu(Stage stage){

    }



    public void secondWorkerMenu(Stage stage){

    }

    @Override
    public void start(Stage stage) throws IOException {
        connectionMenu(stage);
        returnButton.setOnAction(actionEvent -> mainMenu(stage));
        connectButton.setOnAction(actionEvent -> mainMenu(stage));
        userButton.setOnAction(actionEvent -> userMenu(stage));
        chancelleryButton.setOnAction(actionEvent -> chancelleryMenu(stage));
        printRequestButton.setOnAction(actionEvent -> {
            try {
                printMenu(stage);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        /*
        SQLClient client = new SQLClient();

        mainLabel = new Label("Добро пожаловать в сервис социальных услуг! Заполните анкету");
        applicantLabel = new Label("Ваше ФИО");
        managerLabel = new Label("ФИО руководителя");
        addressLabel = new Label("Адрес проживания");
        matterLabel = new Label("Причина заявки");
        contentsLabel = new Label("Пояснение к причине");
        statusLabel = new Label();

        Button senderButton = new Button("Отправить заявку");

        senderButton.setDisable(true);

        applicantField = new TextField();
        managerField = new TextField();
        addressField = new TextField();
        matterField = new TextField();
        contentsField = new TextField();

        applicantField.setMaxWidth(400);
        managerField.setMaxWidth(400);
        addressField.setMaxWidth(400);
        matterField.setMaxWidth(400);
        contentsField.setMaxWidth(400);

        VBox vBox = new VBox(connectButton, mainLabel,
                applicantLabel, applicantField,
                managerLabel, managerField,
                addressLabel, addressField,
                matterLabel, matterField,
                contentsLabel, contentsField,
                senderButton, statusLabel);

        senderButton.setOnAction(actionEvent -> {
            String applicant = applicantField.getText();
            String manager = managerField.getText();
            String address = addressField.getText();
            String matter = matterField.getText();
            String contents = contentsField.getText();
            if(applicant.isEmpty() || manager.isEmpty() || address.isEmpty()
            || matter.isEmpty() || contents.isEmpty()){
                statusLabel.setTextFill(Color.RED);
                statusLabel.setText("Не все поля заполнены");
            }
            else{
                try {
                    client.insertData(IDGenerator.generateUniqueID(), applicant, manager, address,
                            matter, contents, "Рассматривается", "По усмотрению руководителя");
                    statusLabel.setTextFill(Color.GREEN);
                    statusLabel.setText("Заявка принята");
                    applicantField.clear();
                    managerField.clear();
                    addressField.clear();
                    matterField.clear();
                    contentsField.clear();
                } catch (SQLException e) {
                    statusLabel.setTextFill(Color.RED);
                    statusLabel.setText("Не удалось вставить данные. Повторите попытку");
                }
            }
        });

        connectButton.setOnAction(actionEvent -> {
            try {
                client.connect("jdbc:postgresql://localhost:5432/Office1",
                        "postgres", "ChaosNova2020");
                if(client.isConnected()){
                    senderButton.setDisable(false);
                    statusLabel.setTextFill(Color.GREEN);
                    statusLabel.setText("Подключение выполнено успешно. Можете заполнить анкету");
                    connectButton.setVisible(false);
                }
            }catch (SQLException e){
                statusLabel.setTextFill(Color.RED);
                statusLabel.setText("Ошибка подключения к БД");
            }
        });

        vBox.setAlignment(Pos.CENTER);
        Scene scene = new Scene(vBox, 640, 480);
        vBox.setSpacing(10);
        stage.setScene(scene);
        stage.setTitle("Клиент");
        stage.show();
     */
    }

    public static void main(String[] args){
        launch();
    }
}