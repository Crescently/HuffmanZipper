package org.example.view;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public class HuffmanZipperStarter extends Application {

    public static void main(String[] args) {
        // 启动UI界面
        log.info("Starting HuffmanZipper...");
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        Scene scene = new Scene(FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/UI.fxml"))));
        stage.setTitle("Huffman Zipper");
        stage.setScene(scene);
        stage.show();
    }
}