package org.example.view;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.example.constant.OperateType;
import org.example.util.FileUtil;

import java.io.File;

import static org.example.util.FileUtil.FileTypeReader;


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
        File selected = null;
        if ("文件".equals(type)) {
            FileChooser fileChooser = new FileChooser();
            selected = fileChooser.showOpenDialog(null);
        } else if ("文件夹".equals(type)) {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            selected = directoryChooser.showDialog(null);
        }
        if (selected != null) {
            filePathField.setText(selected.getAbsolutePath());
        } else {
            showAlert("选择错误", "未选择任何文件或文件夹。");
        }
    }

    @FXML
    private void handleBrowseTarget() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(null);
        if (selectedDirectory != null) {
            targetPathField.setText(selectedDirectory.getAbsolutePath());
        } else {
            showAlert("选择错误", "未选择目标目录。");
        }
    }

    @FXML
    private void handleZip(ActionEvent event) {
        String filePath = filePathField.getText();
        String targetPath = targetPathField.getText();

        if (filePath == null || filePath.isEmpty() || targetPath == null || targetPath.isEmpty()) {
            showAlert("错误", "请确保所有字段均已填写。");
            return;
        }

        // 创建任务并在新线程中运行 防止界面卡死
        Task<Void> unzipTask = new Task<>() {
            @Override
            protected Void call() {
                FileUtil.getFileInfo(filePath, targetPath);
                if (event.getSource() == compress) {
                    FileTypeReader(filePath, OperateType.COMPRESS);
                } else if (event.getSource() == decompress) {
                    FileTypeReader(filePath, OperateType.DECOMPRESS);
                }
                return null;
            }
        };
        new Thread(unzipTask).start();
    }

    /**
     * 显示错误提示框
     *
     * @param title   标题
     * @param content 内容
     */
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
