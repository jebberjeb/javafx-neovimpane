package neovim_javafx;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.embed.swing.JFXPanel;

public class NeovimPaneTest {

    public static void main(String[] args) {

        // Initialize the JFX runtime.
        new JFXPanel();

        // Let us close & recreate the test UI without having to restart the REPL.
        Platform.setImplicitExit(false);

        Platform.runLater(
                new Runnable() {
                    public void run() {
                        try {
                            NeovimPane npane = new NeovimPane();
                            TextArea text = new TextArea();
                            SplitPane spane = new SplitPane();
                            spane.getItems().add(npane);
                            spane.getItems().add(text);
                            Scene scene = new Scene(spane);
                            Stage stage = new Stage();
                            stage.setScene(scene);
                            stage.show();
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                });
    }
}
