package org.example.UI;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.example.FileUtil.FileUtil;
import org.example.constant.OperateType;

import java.io.File;

import static org.example.FileUtil.FileUtil.FileTypeReader;

public class ZipperController {
    public Button compress;

    public Button decompress;
    @FXML
    private ComboBox<String> selectionType;
    @FXML
    private TextField filePathField;
    @FXML
    private TextField targetPathField;

    @FXML
    private void handleBrowse() {
        String type = selectionType.getValue();
        if (type.equals("文件")) {
            FileChooser fileChooser = new FileChooser();
            File selectedFile = fileChooser.showOpenDialog(null);
            if (selectedFile != null) {
                filePathField.setText(selectedFile.getAbsolutePath());
            }
        } else if (type.equals("文件夹")) {
            this.handleBrowseTarget();
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File selectedDirectory = directoryChooser.showDialog(null);
            if (selectedDirectory != null) {
                filePathField.setText(selectedDirectory.getAbsolutePath());
            }
        }
    }

    @FXML
    private void handleBrowseTarget() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(null);
        if (selectedDirectory != null) {
            targetPathField.setText(selectedDirectory.getAbsolutePath());
        }
    }

    @FXML
    private void handleZip(ActionEvent event) {
        String filePath = filePathField.getText();
        String targetPath = targetPathField.getText();

        // 创建解压任务并在新线程中运行 防止界面卡死
        Task<Void> unzipTask = new Task<>() {
            @Override
            protected Void call() {
                if (filePath != null && targetPath != null) {
                    FileUtil.getFileInfo(filePath, targetPath);
                    if (event.getSource() == compress) {
                        FileTypeReader(filePath, OperateType.COMPRESS);
                    } else if (event.getSource() == decompress) {
                        FileTypeReader(filePath, OperateType.DECOMPRESS);
                    }

                }
                return null;
            }
        };
        new Thread(unzipTask).start();


    }
}
