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
        try {
            Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
            Scene scene = new Scene(FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/UI.fxml"))));
            stage.setTitle("Huffman Zipper");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            log.error("Failed to start the application.", e);
            showErrorDialog();
        }
    }

    /**
     * 显示错误对话框
     */
    private void showErrorDialog() {
        javafx.application.Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("应用程序启动失败");
            alert.setContentText("无法加载UI界面，请检查日志获取详细信息。");
            alert.showAndWait();
        });
    }
}