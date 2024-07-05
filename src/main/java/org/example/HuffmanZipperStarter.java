package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class HuffmanZipperStarter extends Application {
    public static void main(String[] args) {
        // 启动UI界面
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = new Scene(FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/UI.fxml"))));
        stage.setTitle("Huffman Zipper");
        stage.setScene(scene);
        stage.show();
    }
}