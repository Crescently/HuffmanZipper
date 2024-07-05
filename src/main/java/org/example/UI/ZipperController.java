package org.example.UI;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.example.FileUtil.FileUtil;

import java.io.File;

public class ZipperController {
    @FXML
    private ComboBox<String> selectionType;

    @FXML
    private TextField filePathField;

    @FXML
    private TextField targetPathField;

    @FXML
    private void handleBrowse(ActionEvent event) {
        String type = selectionType.getValue();
        if (type.equals("文件")) {
            FileChooser fileChooser = new FileChooser();
            File selectedFile = fileChooser.showOpenDialog(null);
            if (selectedFile != null) {
                filePathField.setText(selectedFile.getAbsolutePath());
            }
        } else if (type.equals("文件夹")) {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File selectedDirectory = directoryChooser.showDialog(null);
            if (selectedDirectory != null) {
                filePathField.setText(selectedDirectory.getAbsolutePath());
            }
        }
    }

    @FXML
    private void handleBrowseTarget(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(null);
        if (selectedDirectory != null) {
            targetPathField.setText(selectedDirectory.getAbsolutePath());
        }
    }

    @FXML
    private void handleUnzip(ActionEvent event) {
        String filePath = filePathField.getText();
        String targetPath = targetPathField.getText();

        FileUtil.getFileInfo(filePath, targetPath);

    }
}
