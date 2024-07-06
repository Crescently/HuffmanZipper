package org.example.ui;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class HuffmanZipperStarter extends Application {
    private static final Logger log = LoggerFactory.getLogger(HuffmanZipperStarter.class);

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