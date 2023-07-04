package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.net.URL;
import java.util.Calendar;

public class Main extends Application {

    private Label statusLabel, statusLabelHistoric;
    String topic = "broker_mqtt";




    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) throws Exception {
        URL fxmlUrl = getClass().getResource("/FE.fxml");
        if (fxmlUrl == null) {
            System.err.println("Could not find FXML file!");
            Platform.exit();
            return;
        }

        Parent root = FXMLLoader.load(fxmlUrl);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();


        final MqttClient[] mqttClient = {null};
        Button connectButton = (Button) scene.lookup("#connectButton");
        TextField brokerField = (TextField) scene.lookup("#brokerField");
        TextField portField = (TextField) scene.lookup("#portField");
        scene.lookup("#topicField");
        Button publishButton = (Button) scene.lookup("#publishButton");
        TextField messageField = (TextField) scene.lookup("#messageField");
        statusLabel = (Label) scene.lookup("#statusLabel");
        statusLabelHistoric = (Label) scene.lookup("#statusLabelHistoric");
        ProgressBar progressBar = (ProgressBar) scene.lookup("#progressBar");


        messageField.setDisable(true);
        publishButton.setDisable(true);
        progressBar.setVisible(false);

        connectButton.setOnAction((event) -> {

            progressBar.setVisible(true);
            progressBar.setProgress(-1.0);
            String broker = brokerField.getText();
            String clientId = MqttClient.generateClientId();
            MemoryPersistence persistence = new MemoryPersistence();
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);

            String serverURI;
            String portText = portField.getText().trim();
            Integer port;

            if (portText.isEmpty()) {
                port = null;
            } else {
                try {
                    port = Integer.parseInt(portText);
                    if (port < 0 || port > 65535) {
                        progressBar.setVisible(false);
                        Alert alert = new Alert(Alert.AlertType.WARNING, "Port out of range: " + portText);
                        alert.showAndWait();
                        return;
                    }
                } catch (NumberFormatException e) {
                    progressBar.setVisible(false);
                    Alert alert = new Alert(Alert.AlertType.WARNING, "Invalid port number: " + portText);
                    alert.showAndWait();
                    return;
                }
            }
            serverURI = "tcp://" + broker + (port != null ? ":" + port : "");

            //If you are connected to a broker, disconnect from it before doing another one do avoid duplicate connections
            if (mqttClient[0] != null && mqttClient[0].isConnected()) {
                try {
                    mqttClient[0].disconnect();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
            String finalServerURI2 = serverURI;
            Thread connectThread = new Thread(() -> {
                try {
                    mqttClient[0] = new MqttClient(finalServerURI2, clientId, persistence);
                    mqttClient[0].setCallback(new MqttCallback() {
                        public void connectionLost(Throwable throwable) {
                            Platform.runLater(() -> {
                                statusLabel.setStyle("-fx-text-fill: red;");
                                statusLabel.setText("Disconnected");
                                statusLabelHistoric.setText("");
                                // configure the failed connection
                            });
                        }

                        public void messageArrived(String topic, MqttMessage mqttMessage) {
                            Platform.runLater(() -> {
                                TextArea messagesArea = (TextArea) scene.lookup("#messagesArea");
                                Calendar now = Calendar.getInstance();
                                messagesArea.appendText(now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE) + " - " + new String(mqttMessage.getPayload()) + "\n\n");
                            });
                        }

                        public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                            //configure the success of the message
                        }
                    });

                    mqttClient[0].connect(connOpts);
                    mqttClient[0].subscribe(topic);
                    Platform.runLater(() -> {
                        progressBar.setVisible(false);
                        messageField.setDisable(false);
                        publishButton.setDisable(false);
                        statusLabel.setStyle("-fx-text-fill: green;");
                        statusLabel.setText("Connected: " + finalServerURI2);
                        Calendar now = Calendar.getInstance();
                        int year = now.get(Calendar.YEAR);
                        int month = now.get(Calendar.MONTH) + 1; // Calendar.MONTH starts from 0, so adding 1 to get the actual month
                        int day = now.get(Calendar.DAY_OF_MONTH);
                        statusLabelHistoric.setText("Beginning of the topic messages: " + topic + " at " + String.format("%02d:%02d", now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE)) + " on " + String.format("%02d/%02d/%04d", day, month, year));
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Successfully connected to the broker: " + finalServerURI2);
                        alert.showAndWait();
                    });
                } catch (MqttException e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        progressBar.setVisible(false);
                        messageField.setDisable(true);
                        publishButton.setDisable(true);
                        statusLabel.setStyle("-fx-text-fill: red;");
                        statusLabel.setText("Disconnected");
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Connection failed to the broker: " + finalServerURI2);
                        alert.showAndWait();
                    });
                }
            });

            connectThread.start();
        });
        publishButton.setOnAction((event) -> {
            if (mqttClient[0] != null) {
                String message = messageField.getText();
                if (!message.isEmpty()) {
                    MqttMessage mqttMessage = new MqttMessage(message.getBytes());

                    try {

                        mqttClient[0].publish(topic, mqttMessage);
                        messageField.clear();
                        //Configuration of the message sent
                    } catch (MqttException e) {
                        e.printStackTrace();
                        // Configuration of the message not sent
                    }
                }
            }
        });
    }
}
