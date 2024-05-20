package com.example.lp;

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
import java.util.ArrayList;

public class MainApplication extends Application {

    //Размеры окна приложения
    int height = 800, width = 600;

    SQLClient client = new SQLClient();

    //Дополнительные переменные для меню "Канцелярия" и Резолюция
    boolean value = false;
    int option = 0;

    //Текстовые поля для заявителя
    TextField applicantField, managerField, addressField,
            matterField, contentsField;
    Label mainLabel, applicantLabel, managerLabel, addressLabel,
            matterLabel, contentsLabel, statusLabel;

    //Заранее инициализируем 2 поля для ввода номера и статуса сканирования
    // (понадобятся в будущем в нескольких окнах)
    TextField numberField = new TextField();
    Label qrStatusLabel = new Label();

    //Заранее инициализируем необходимые кнопки
    Button connectButton = new Button("Подключение к бд");

    Button userButton = new Button("Я заявитель");
    Button chancelleryButton = new Button("Я работник канцелярии");
    Button firstWorkerButton = new Button("Я работник офиса 1");
    Button managerButton = new Button("Я руководитель");
    Button secondWorkerButton = new Button("Я работник офиса 2");

    Button senderButton = new Button("Отправить заявку");
    Button sendToDataBaseButton = new Button("Отправить результат в БД 1");

    Button returnButton = new Button("Выйти из приложения");

    Button getUnpreparedDataButton = new Button("Посмотреть номера текущих заявок");
    Button getSeenDataButton = new Button("Посмотреть номера рассмотренных заявок");
    Button getPreparedDataButton = new Button("Просмотреть номера распечатанных заявок");
    Button getSentDataButton = new Button("Просмотреть номера рассматриваемых заявок");
    Button printRequestButton = new Button("Перейти к заявке под номером");

    Button encodeButton = new Button("Записать обращение QR-Код");
    Button decodeButton = new Button("Декодировать из QR-Кода обращение");
    Button resolutionButton = new Button("Перейти к резолюции по обращению");

    Label resolutionLabel = new Label("Резолюция");
    Label statusRenewLabel = new Label("Статус");
    Label infoLabel = new Label("Дополнительная информация");

    //Дополнительные объекты для меню "Резолюция"
    TextField resolutionField = new TextField();
    Button toggleButton = new Button(String.valueOf(false));

    Button setResolution = new Button("Создать резолюцию");
    TextField infoField = new TextField();

    //Получаем данные из БД в виде массива строк
    private String[] getFromDataBase(String dataBase, String tableName, boolean isDataSeen){
        return client.getEachID(dataBase, tableName, isDataSeen).split("; ");
    }

    private String printRequestInfo(String dataBase, String tableName, String applicant) throws SQLException {
        return client.getUserInformation(dataBase, tableName, applicant);
    }

    //Проверка ввода идентификатора на валидность
    private boolean isValidInput(TextField textField, GridPane gridPane) {
        if (textField.getText().isEmpty()) {
            return false;
        }
        int inputValue;
        try {
            inputValue = Integer.parseInt(textField.getText());
        } catch (NumberFormatException e) {
            return false;
        }
        for (javafx.scene.Node node : gridPane.getChildren()) {
            if (node instanceof TextField) {
                TextField gridTextField = (TextField) node;
                try {
                    int gridValue = Integer.parseInt(gridTextField.getText());
                    if (gridValue == inputValue) {
                        return true; // Значение найдено в GridPane
                    }
                } catch (NumberFormatException e) {
                    // Пропускаем элементы, которые не являются целыми числами
                }
            }
        }
        return false;
    }

    //Печатаем идентификаторы полученных данных, которые еще не закодированы
    private void printReceivedData(GridPane gridPane, String fileFormat, String[] received){
        int row = 0, col = 0;
        for(String data: received){
            String filename = "request_ID_" + data + fileFormat;
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

    //Печатаем идентификаторы полученных данных, которые уже были закодированы
    private void printEncodedData(GridPane gridPane, String fileFormat, String[] encoded){
        int row = 0, col = 0;
        for(String data: encoded) {
            String filename = "request_ID_" + data + fileFormat;
            File file = new File(filename);
            if (file.exists()) {
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

    //Получаем отсканированные данные по заданным условиям
    private void printScannedData(GridPane gridPane, String[] scanned){
        int row = 0, col = 0;
        for(String data: scanned){
            Label idLabel = new Label(data);
            gridPane.add(idLabel, row, col);
            col++;
            if (col == 5) {
                col = 0;
                row++;
            }
        }
    }

    //Выводим данные из строки в виде сетки
    private GridPane getDataFromID(String dataBase, String tableName) throws SQLException {
        int id = Integer.parseInt(numberField.getText());
        String data = client.getCurrentID(dataBase, tableName, id);
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(50);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        String[] entries = data.split("; ");
        for (int i = 0; i < entries.length; i++) {
            String[] parts = entries[i].split(": ");
            if (parts.length == 2) {
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

    //Подготавливаем данные к отправке
    private void prepareToSend(String dataBase, String dataToSend, String tableName) throws SQLException {
        ArrayList<String> data = new ArrayList<>();
        String[] pairs = dataToSend.split("; ");
        for (String current : pairs) {
            String[] entries = current.split(": ");
            data.add(entries[1]);
        }

        // Проверка на "null"
        Boolean status = null;
        if (!data.get(7).equalsIgnoreCase("null")) {
            status = Boolean.valueOf(data.get(7));
        }

        //Вставка данных в таблицу
        client.insertData(dataBase, tableName, Integer.parseInt(data.get(0)),
                data.get(1), data.get(2), data.get(3), data.get(4),
                data.get(5), data.get(6), status, data.get(8));
    }

    //Кодирование данных
    private void encodeSingleData(String dataBase, String tableName, String fileName) throws SQLException, IOException, WriterException {
        int id = Integer.parseInt(numberField.getText());
        String data = client.getCurrentID(dataBase, tableName ,id);
        QRCodeTool.generateQRCode(data, fileName);
    }

    //Декодирование данных
    private String decodeSingleData(String imagePath) throws SQLException {
        return QRCodeTool.decodeQRCode(imagePath);
    }

    //Печатаем данные для подготовки к созданию резолюции
    private GridPane dataToResolution(String inputString) {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        String[] entries = inputString.split("; ");
        for (int i = 0; i < entries.length-3; i++) {
            String[] parts = entries[i].split(": ");
            if (parts.length == 2) {
                Label columnName = new Label(parts[0]);
                Label columnValue = new Label(parts[1]);

                grid.add(columnName, 0, i);
                grid.add(columnValue, 1, i);
            }
        }

        toggleButton.setPadding(new Insets(10));
        toggleButton.setOnAction(event -> {
            value = !value;
            toggleButton.setText(String.valueOf(value));
        });

        grid.add(resolutionLabel, 0, entries.length-3);
        grid.add(statusRenewLabel, 0, entries.length-2);
        grid.add(infoLabel, 0, entries.length-1);
        grid.add(resolutionField, 1, entries.length-3);
        grid.add(toggleButton, 1, entries.length-2);
        grid.add(infoField, 1,entries.length-1);

        return grid;
    }

    //Отправляем резолюцию в БД, обновляя данные
    private void sendResolution(String dataBase, String tableName, String resolution, boolean status, String info, int id){
        client.updateData(dataBase, tableName, resolution, status, info, id);
    }

    //Основное меню программы
    private void mainMenu(Stage stage){
        VBox vBox = new VBox(userButton, chancelleryButton, managerButton,
                firstWorkerButton, secondWorkerButton);
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(20);
        Scene scene = new Scene(vBox, height, width);
        stage.setScene(scene);
        stage.setTitle("Авторизация");
        stage.show();
    }

    //Меню заявителя
    private void userMenu(Stage stage){

        mainLabel = new Label("Добро пожаловать в сервис социальных услуг! Заполните анкету");
        applicantLabel = new Label("Ваше ФИО");
        managerLabel = new Label("ФИО руководителя");
        addressLabel = new Label("Адрес проживания");
        matterLabel = new Label("Причина заявки");
        contentsLabel = new Label("Пояснение к причине");
        statusLabel = new Label();

        //Для возможности посмотреть обращение по ФИО
        Label getRequestLabel = new Label("Или введите ФИО для просмотра статуса созданной заявки");
        Label getRequestInfo = new Label();
        Button getRequestButton = new Button("Отследить статус заявки");

        applicantField = new TextField();
        managerField = new TextField();
        addressField = new TextField();
        matterField = new TextField();
        contentsField = new TextField();

        TextField getRequestField = new TextField();

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

        //Биндим на кнопку создание заявки
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
                    int id = IDGenerator.generateUniqueID();
                    client.insertData(client.FIRST_HOST,client.REQUESTS ,id, applicant, manager,
                            address, matter, contents, "Рассматривается",
                            null,"По усмотрению руководителя");
                    statusLabel.setTextFill(Color.GREEN);
                    statusLabel.setText("Заявка принята! Ее номер: " + String.valueOf(id) + ". Ожидайте" +
                            " декодирования и/или рассмотрения");
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
        vBox.getChildren().add(menuPane);
        vBox.getChildren().add(senderButton);
        vBox.getChildren().add(statusLabel);
        vBox.getChildren().add(getRequestLabel);
        vBox.getChildren().add(getRequestField);
        vBox.getChildren().add(getRequestButton);
        vBox.getChildren().add(getRequestInfo);
        getRequestButton.setOnAction(actionEvent -> {

            //Получаем данные по созданной заявке, если она декодирована
            if(getRequestField.getText().isEmpty()){
                getRequestInfo.setTextFill(Color.RED);
                getRequestInfo.setText("Поле не заполнено");
            }
            else {
                try {
                    getRequestInfo.setText(
                            printRequestInfo(client.FIRST_HOST, client.PRINTED_REQUESTS, getRequestField.getText()));
                    getRequestField.clear();
                } catch (SQLException e) {
                    //throw new RuntimeException(e);
                }
            }
        });
        getRequestField.setMaxWidth(300);
        vBox.setAlignment(Pos.CENTER);
        vBox.setMaxSize(height, width);
        Scene scene = new Scene(vBox, height, width);
        vBox.setSpacing(10);
        stage.setScene(scene);
        stage.setTitle("Клиент");
        stage.show();

    }

    //Меню работника
    private void chancelleryMenu(Stage stage){
        VBox root = new VBox(10); // Создаем VBox для размещения кнопки и GridPane
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(25, 25, 25, 25));
        root.getChildren().add(qrStatusLabel);
        root.getChildren().add(returnButton);
        root.getChildren().add(getUnpreparedDataButton);
        root.getChildren().add(getSeenDataButton);
        getUnpreparedDataButton.setVisible(true);
        getSeenDataButton.setVisible(true);
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        //Биндим на кнопки вывод необходимых данных,
        //изменяя переменную option в зависимости от нажатой кнопки
        getUnpreparedDataButton.setOnAction(actionEvent -> {
            numberField.clear();
            gridPane.getChildren().clear();
            getUnpreparedDataButton.setDisable(true);
            getSeenDataButton.setDisable(false);
            if(getFromDataBase(client.FIRST_HOST,
                    client.REQUESTS, false)[0].isEmpty()){
                qrStatusLabel.setTextFill(Color.YELLOWGREEN);
                qrStatusLabel.setText("На данный момент все заявки рассмотрены и/или отклонены. Вернитесь позже");
                getUnpreparedDataButton.setVisible(false);
            }
            else{
                printReceivedData(gridPane,
                        ".png", getFromDataBase(client.FIRST_HOST,
                                client.REQUESTS, false));
                if(gridPane.getChildren().isEmpty()){
                    qrStatusLabel.setTextFill(Color.YELLOWGREEN);
                    qrStatusLabel.setText("Нет доступных заявок для создания QR. Вернитесь позже");
                    getUnpreparedDataButton.setVisible(false);
                }
                else{
                    gridPane.add(printRequestButton, 0, 6);
                    gridPane.add(numberField, 1, 6);
                    getUnpreparedDataButton.setVisible(true);
                    option = 1;
                }
            }
        });
        getSeenDataButton.setOnAction(actionEvent -> {
            numberField.clear();
            gridPane.getChildren().clear();
            getSeenDataButton.setDisable(true);
            getUnpreparedDataButton.setDisable(false);
            if(getFromDataBase(client.FIRST_HOST,
                    client.PRINTED_REQUESTS, true)[0].isEmpty()){
                qrStatusLabel.setTextFill(Color.YELLOWGREEN);
                qrStatusLabel.setText("На данный момент все заявки рассмотрены. Вернитесь позже");
                getSeenDataButton.setVisible(false);
            }else{
                printReceivedData(gridPane, "_solved.png",
                        getFromDataBase(client.FIRST_HOST,
                        client.PRINTED_REQUESTS, true));
                if(gridPane.getChildren().isEmpty()){
                    qrStatusLabel.setTextFill(Color.YELLOWGREEN);
                    qrStatusLabel.setText("Нет доступных заявок для создания QR. Вернитесь позже");
                    getSeenDataButton.setVisible(false);
                }
                else{
                    gridPane.add(printRequestButton, 0, 6);
                    gridPane.add(numberField, 1, 6);
                    getSeenDataButton.setVisible(true);
                    option = 2;
                }
            }
        });

        //Кнопка для перехода в одно из двух меню
        printRequestButton.setOnAction(actionEvent -> {
            if(!isValidInput(numberField, gridPane)){
                qrStatusLabel.setTextFill(Color.RED);
                qrStatusLabel.setText("Что-то пошло не так. Проверьте корректность введенных данных");
            }
            else{
                try {
                    if(option == 1)
                        printMenu(stage);
                    if(option == 2)
                        printSeenMenu(stage);
                } catch (SQLException e) {
                    qrStatusLabel.setTextFill(Color.RED);
                    qrStatusLabel.setText("Заявка с номером " + numberField.getText() + " отсутсвует или " +
                            "по каким-то причинам недоступна");
                }
            }
        });
        root.getChildren().add(gridPane);
        Scene scene = new Scene(root, height, width);
        stage.setScene(scene);
        stage.setTitle("Канцелярия");
        stage.show();
    }

    //Меню создания QR для нерассмотренных заявок
    private void printMenu(Stage stage) throws SQLException {
        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(20);
        GridPane dataGrid = getDataFromID(client.FIRST_HOST, client.REQUESTS);
        vBox.getChildren().add(dataGrid);
        vBox.getChildren().add(encodeButton);

        //Создание файла с кодом
        encodeButton.setOnAction(actionEvent -> {
            chancelleryMenu(stage);
            try {
                encodeSingleData(client.FIRST_HOST,
                        client.REQUESTS, "request_ID_" + numberField.getText() + ".png");
                qrStatusLabel.setTextFill(Color.GREEN);
                qrStatusLabel.setText("QR-код обращения " + numberField.getText() + " успешно создан!");
                numberField.clear();
            } catch (SQLException | IOException | WriterException e) {
                qrStatusLabel.setTextFill(Color.RED);
                qrStatusLabel.setText("Не удалось создать QR-код обращения " + numberField.getText() +
                        ". Повторите попытку позже");
                numberField.clear();
            }
        });
        Scene scene = new Scene(vBox, height, width);
        stage.setScene(scene);
        stage.setTitle("Обращение");
        stage.show();
    }

    //Меню создания QR для рассмотренных заявок
    private void printSeenMenu(Stage stage) throws SQLException {
        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(20);
        GridPane dataGrid = getDataFromID(client.FIRST_HOST, client.PRINTED_REQUESTS);
        vBox.getChildren().add(dataGrid);
        vBox.getChildren().add(encodeButton);

        //Создание файла с кодом
        encodeButton.setOnAction(actionEvent -> {
            chancelleryMenu(stage);
            try {
                encodeSingleData(client.FIRST_HOST,
                        client.PRINTED_REQUESTS, "request_ID_" +
                                numberField.getText() + "_solved" + ".png");
                qrStatusLabel.setTextFill(Color.GREEN);
                qrStatusLabel.setText("QR-код обращения " + numberField.getText() + " успешно создан!");
                numberField.clear();
            } catch (SQLException | IOException | WriterException e) {
                //throw new RuntimeException(e);
                qrStatusLabel.setTextFill(Color.RED);
                qrStatusLabel.setText("Не удалось создать QR-код обращения " + numberField.getText() +
                        ". Повторите попытку позже");
                numberField.clear();
            }
        });
        Scene scene = new Scene(vBox, height, width);
        stage.setScene(scene);
        stage.setTitle("Обращение");
        stage.show();
    }

    //Меню работника Офиса 1
    private void firstWorkerMenu(Stage stage){
        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(25, 25, 25, 25));
        root.getChildren().add(qrStatusLabel);
        root.getChildren().add(returnButton);
        root.getChildren().add(getPreparedDataButton);
        getPreparedDataButton.setVisible(true);
        getPreparedDataButton.setDisable(false);
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        //Смотрим закодированные заявки
        getPreparedDataButton.setOnAction(actionEvent -> {
            gridPane.getChildren().clear();
            getPreparedDataButton.setDisable(false);
            if(getFromDataBase(client.FIRST_HOST,
                    client.REQUESTS, false)[0].isEmpty()){
                qrStatusLabel.setTextFill(Color.YELLOWGREEN);
                qrStatusLabel.setText("Обращения отсутствуют или они были рассмотрены");
                getPreparedDataButton.setVisible(false);
            }
            else{
                printEncodedData(gridPane, ".png", getFromDataBase(client.FIRST_HOST,
                        client.REQUESTS, false));
                if(gridPane.getChildren().isEmpty()){
                    qrStatusLabel.setTextFill(Color.YELLOWGREEN);
                    qrStatusLabel.setText("QR коды отсутствуют или данные были декодированы и рассмотрены");
                    getPreparedDataButton.setVisible(false);
                }
                else{
                    gridPane.add(decodeButton, 0, 6);
                    gridPane.add(numberField, 1, 6);
                    getPreparedDataButton.setVisible(true);
                }
            }
        });

        //Декодируем заявку и отправляем в БД
        decodeButton.setOnAction(actionEvent -> {
            gridPane.getChildren().clear();
            try {
                if(client.isIdExists(client.FIRST_HOST,
                        client.PRINTED_REQUESTS, Integer.parseInt(numberField.getText()))){
                    qrStatusLabel.setTextFill(Color.RED);
                    qrStatusLabel.setText("Обращение с номером " + numberField.getText() + " уже есть в базе данных. " +
                            "Выберете другое обращение");
                    numberField.clear();
                }
                else firstSenderMenu(stage);

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        root.getChildren().add(gridPane);
        Scene scene = new Scene(root, height, width);
        stage.setScene(scene);
        stage.setTitle("Офис 1");
        stage.show();
    }

    //Меню отправки в БД декодированной заявки (с возможностью редактирования)
    private void firstSenderMenu(Stage stage) throws SQLException {
        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(20);
        TextField decodedData = new TextField();
        decodedData.setText(decodeSingleData("request_ID_" + numberField.getText() + ".png"));
        decodedData.setPrefWidth(400);
        vBox.getChildren().add(decodedData);
        vBox.getChildren().add(sendToDataBaseButton);
        sendToDataBaseButton.setVisible(true);

        //Отправка в бд
        sendToDataBaseButton.setOnAction(actionEvent -> {
            try{
                firstWorkerMenu(stage);
                prepareToSend(client.FIRST_HOST, decodedData.getText(), client.PRINTED_REQUESTS);
                qrStatusLabel.setTextFill(Color.GREEN);
                qrStatusLabel.setText("Обращение с номером " + numberField.getText() + " декодировано " +
                        "и отправлено в базу данных");
                numberField.clear();
            } catch (SQLException e) {
                qrStatusLabel.setTextFill(Color.RED);
                qrStatusLabel.setText("Произошла ошибка при отправке результатов. Попробуйте еще раз");
                numberField.clear();
                throw new RuntimeException(e);
            }
        });

        Scene scene = new Scene(vBox, height, width);
        stage.setScene(scene);
        stage.setTitle("Обращение");
        stage.show();
    }

    //Меню руководителя
    private void managerMenu(Stage stage){
        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(25, 25, 25, 25));
        root.getChildren().add(qrStatusLabel);
        root.getChildren().add(returnButton);
        root.getChildren().add(getSentDataButton);
        GridPane gridPane = new GridPane();
        root.getChildren().add(gridPane);
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        getSentDataButton.setVisible(true);
        getSentDataButton.setDisable(false);

        //Получаем номера декодированных заявок
        getSentDataButton.setOnAction(actionEvent -> {
            gridPane.getChildren().clear();
            getSentDataButton.setDisable(true);
            if(getFromDataBase(client.FIRST_HOST,
                    client.PRINTED_REQUESTS, false)[0].isEmpty()){
                qrStatusLabel.setTextFill(Color.YELLOWGREEN);
                qrStatusLabel.setText("Обращения отсутствуют или они были рассмотрены");
                getSentDataButton.setVisible(false);
            }
            else{
                printScannedData(gridPane, getFromDataBase(client.FIRST_HOST,
                        client.PRINTED_REQUESTS, false));
                if(gridPane.getChildren().isEmpty()){
                    qrStatusLabel.setTextFill(Color.YELLOWGREEN);
                    qrStatusLabel.setText("Нет отсканированных обращений. Обратитесь к сотруднику офиса 1" +
                            "или канцелярии");
                    getSentDataButton.setVisible(false);
                }
                else{
                    gridPane.add(resolutionButton, 0, 6);
                    gridPane.add(numberField, 1, 6);
                    getSentDataButton.setVisible(true);
                }
            }

        });

        //Переходим к резолюции
        resolutionButton.setOnAction(actionEvent -> {
            try {
                resolutionMenu(stage);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        Scene scene = new Scene(root, height, width);
        stage.setScene(scene);
        stage.setTitle("Руководитель");
        stage.show();
    }

    //Меню создания резолюции
    private void resolutionMenu(Stage stage) throws SQLException {
        VBox root = new VBox(10); // Создаем VBox для размещения кнопки и GridPane
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(25, 25, 25, 25));
        int id = Integer.parseInt(numberField.getText());
        GridPane gridPane = dataToResolution(client.getCurrentID(client.FIRST_HOST,
                client.PRINTED_REQUESTS, id));
        root.getChildren().add(gridPane);
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        root.getChildren().add(setResolution);

        //Создаем резолюцию и обновляем данные в БД
        setResolution.setOnAction(actionEvent -> {
            managerMenu(stage);
            sendResolution(client.FIRST_HOST, client.PRINTED_REQUESTS,
                    resolutionField.getText(), Boolean.parseBoolean(toggleButton.getText()),
                    infoField.getText(), id);
            qrStatusLabel.setTextFill(Color.GREEN);
            qrStatusLabel.setText("Резолюция по обращению " + numberField.getText() +
                    " успешно отправлена в бд");
            numberField.clear();
        });
        Scene scene = new Scene(root, height, width);
        stage.setScene(scene);
        stage.setTitle("Резолюция по обращению");
        stage.show();
    }

    //Меню работника офиса 2
    private void secondWorkerMenu(Stage stage){
        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(25, 25, 25, 25));
        root.getChildren().add(qrStatusLabel);
        root.getChildren().add(returnButton);
        root.getChildren().add(getPreparedDataButton);
        getPreparedDataButton.setVisible(true);
        getPreparedDataButton.setDisable(false);
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        //Печать закодированных рассмотренных заявок
        getPreparedDataButton.setOnAction(actionEvent -> {
            gridPane.getChildren().clear();
            getPreparedDataButton.setDisable(true);
            if(getFromDataBase(client.FIRST_HOST,
                    client.PRINTED_REQUESTS, true)[0].isEmpty()){
                qrStatusLabel.setTextFill(Color.YELLOWGREEN);
                qrStatusLabel.setText("Отсутствуют рассмотренные обращения");
                getPreparedDataButton.setVisible(false);
            }
            else{
                printEncodedData(gridPane, "_solved.png",
                        getFromDataBase(client.FIRST_HOST,
                                client.PRINTED_REQUESTS, true));
                if(gridPane.getChildren().isEmpty()){
                    qrStatusLabel.setTextFill(Color.YELLOWGREEN);
                    qrStatusLabel.setText("QR коды отсутствуют. Обратитесь в канцелярию");
                    getPreparedDataButton.setVisible(false);
                }
                else{
                    gridPane.add(decodeButton, 0, 6);
                    gridPane.add(numberField, 1, 6);
                    getPreparedDataButton.setVisible(true);
                }
            }
        });

        //Декодирование рассмотренных сообщений
        decodeButton.setOnAction(actionEvent -> {
            try {
                if(client.isIdExists(client.SECOND_HOST,client.SEEN_REQUESTS,
                        Integer.parseInt(numberField.getText()))){
                    qrStatusLabel.setTextFill(Color.RED);
                    qrStatusLabel.setText("Обращение с номером " + numberField.getText() + " уже есть в базе данных. " +
                            "Выберете другое обращение");
                    numberField.clear();
                }
                else secondSenderMenu(stage);

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        root.getChildren().add(gridPane);
        Scene scene = new Scene(root, height, width);
        stage.setScene(scene);
        stage.setTitle("Офис 2");
        stage.show();
    }

    //Меню отправки сообщений в БД из офиса 2
    private void secondSenderMenu(Stage stage) throws SQLException {
        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(20);
        TextField decodedData = new TextField();
        sendToDataBaseButton.setText("Отправить результат в БД 2");
        decodedData.setText(decodeSingleData("request_ID_" + numberField.getText()
                + "_solved" + ".png"));
        decodedData.setPrefWidth(400);
        vBox.getChildren().add(decodedData);
        vBox.getChildren().add(sendToDataBaseButton);
        sendToDataBaseButton.setVisible(true);

        //Отправка результатов в БД2
        sendToDataBaseButton.setOnAction(actionEvent -> {
            try{
                secondWorkerMenu(stage);
                prepareToSend(client.SECOND_HOST, decodedData.getText(), client.SEEN_REQUESTS);
                qrStatusLabel.setTextFill(Color.GREEN);
                qrStatusLabel.setText("Обращение с номером " + numberField.getText() + " декодировано " +
                        "и отправлено в базу данных");
                numberField.clear();
            } catch (SQLException e) {
                qrStatusLabel.setTextFill(Color.RED);
                qrStatusLabel.setText("Произошла ошибка при отправке результатов. Попробуйте еще раз");
                numberField.clear();
                throw new RuntimeException(e);
            }
        });
        Scene scene = new Scene(vBox, height, width);
        stage.setScene(scene);
        stage.setTitle("Обращение");
        stage.show();
    }

    //Инициализация кнопок основного меню
    @Override
    public void start(Stage stage) throws IOException {
        mainMenu(stage);
        returnButton.setOnAction(actionEvent -> {
            qrStatusLabel.setText("");
            mainMenu(stage);
        });
        connectButton.setOnAction(actionEvent -> mainMenu(stage));
        userButton.setOnAction(actionEvent -> userMenu(stage));
        chancelleryButton.setOnAction(actionEvent -> chancelleryMenu(stage));
        firstWorkerButton.setOnAction(actionEvent -> firstWorkerMenu(stage));
        managerButton.setOnAction(actionEvent -> managerMenu(stage));
        secondWorkerButton.setOnAction(actionEvent -> secondWorkerMenu(stage));
    }

    //Запуск программы
    public static void main(String[] args){
        launch();
    }
}