package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class StartWindowController {
    private Stage stage = new Stage();
    @FXML
    void btn_createMarkSheet_onAction(ActionEvent event) {
        try {
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/view/new-mark-sheet-window.fxml"))));
            stage.show();
            NewMarkSheetController.setCurrentStage(stage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void btn_generatePDF_onAction(ActionEvent event) {

    }

    @FXML
    void btn_updateDeleteMarkSheet_onAction(ActionEvent event) {
        try {
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/view/update-delete-window.fxml"))));
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
