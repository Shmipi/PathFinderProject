// PROG2 VT2022, Inlämningsuppgift, del 1
// Grupp 333
// Sarah Abbas Jawad saab3689
// Özge Alkan ozal4139
// Ludvig Burud lubu5130

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.WindowEvent;
import javafx.util.Pair;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage; //awt?
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;


public class PathFinder extends Application {

    //DISPLAYS
    private Stage primaryStage;
    private BorderPane root;
    private Pane center;
    private Canvas nodeCanvas;
    private GraphicsContext gc;
    private ImageView imageView;
    private Scene scene;

    //BOOLEANS
    private boolean notSaved;
    private boolean fromClicked;
    private boolean toClicked;

    //BUTTONS
    private Button newPlace;

    //NODES
    private Node fromNode;
    private Node toNode;

    private ListGraph<Node> listGraph = new ListGraph<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage pathFinder) {

        this.primaryStage = pathFinder;

        primaryStage.setTitle("PathFinder");
        root = new BorderPane();
        center = new Pane();
        VBox topBox = new VBox();
        root.setTop(topBox);
        root.setCenter(center);
        nodeCanvas = new Canvas(618, 729);
        gc = nodeCanvas.getGraphicsContext2D();

        center.setId("outputArea");

        MenuBar menuBar = new MenuBar();
        menuBar.setId("menu");

        Menu fileMenu = new Menu("Menu");
        fileMenu.setId("menuFile");

        topBox.getChildren().add(menuBar);
        menuBar.getMenus().add(fileMenu);

        //NEW MAP
        MenuItem newMap = new MenuItem("New Map");
        fileMenu.getItems().add(newMap);
        newMap.setOnAction(new NewMapHandler());
        newMap.setId("menuNewMap");

        //OPEN
        MenuItem open = new MenuItem("Open");
        fileMenu.getItems().add(open);
        open.setOnAction(new OpenHandler());
        open.setId("menuOpenFile");

        //SAVE
        MenuItem save = new MenuItem("Save");
        fileMenu.getItems().add(save);
        save.setOnAction(new SaveHandler());
        save.setId("menuSaveFile");

        //SAVE IMAGE
        MenuItem saveImage = new MenuItem("Save Image");
        fileMenu.getItems().add(saveImage);
        saveImage.setOnAction(new SaveImageHandler());
        saveImage.setId("menuSaveImage");

        //EXIT
        MenuItem exit = new MenuItem("Exit");
        fileMenu.getItems().add(exit);
        exit.setOnAction(new ExitItemHandler());
        exit.setId("menuExit");

        //BUTTONS
        HBox buttons = new HBox();

        Button findPath = new Button("Find Path");
        findPath.setOnAction(new FindPathHandler());
        findPath.setId("btnFindPath");

        Button showConnection = new Button("Show Connection");
        showConnection.setOnAction(new ShowConnectionHandler());
        showConnection.setId("btnShowConnection");

        newPlace = new Button("New Place");
        newPlace.setOnAction(new NewPlaceHandler());
        newPlace.setId("btnNewPlace");

        Button newConnection = new Button("New Connection");
        newConnection.setOnAction(new NewConnectionHandler());
        newConnection.setId("btnNewConnection");

        Button changeConnection = new Button("Change Connection");
        changeConnection.setOnAction(new ChangeConnectionHandler());
        changeConnection.setId("btnChangeConnection");

        buttons.getChildren().addAll(findPath, showConnection, newPlace, newConnection, changeConnection);

        ////////
        scene = new Scene(root);
        pathFinder.setScene(scene);
        pathFinder.centerOnScreen();

        pathFinder.setMinHeight(120);
        pathFinder.setMinWidth(640);

        pathFinder.show();
        pathFinder.setOnCloseRequest(new ExitHandler());

        buttons.setAlignment(Pos.TOP_CENTER);
        buttons.setPadding(new Insets(10,  10,10, 10));
        buttons.setSpacing(10);

        topBox.getChildren().add(buttons);
    }

    class NewMapHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle (ActionEvent event) {
            if(notSaved) {
                Alert alert = new Alert(AlertType.CONFIRMATION, "Unsaved changes");
                alert.setTitle("WARNING");
                alert.setHeaderText(null);
                Optional<ButtonType> res = alert.showAndWait();
                if (res.isPresent() && res.get().equals(ButtonType.CANCEL)) {
                    event.consume();
                } else {
                    notSaved = false;
                }
            }
            if(!notSaved) {
                fromClicked = false;
                toClicked = false;
                fromNode = null;
                toNode = null;
                center.getChildren().clear();
                gc.clearRect(0, 0, nodeCanvas.getWidth(), nodeCanvas.getHeight());

                Image image = new Image("file:europa.gif");
                imageView = new ImageView(image);
                center.getChildren().add(imageView);
                center.getChildren().add(nodeCanvas);

                primaryStage.setMinHeight(840);

                for (Node node : listGraph.getNodes()) {
                    listGraph.remove(node);
                }

                notSaved = true;
            }
        }
    }

    class OpenHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            if(notSaved) {
                Alert alert = new Alert(AlertType.CONFIRMATION, "Unsaved changes");
                alert.setTitle("WARNING!");
                alert.setHeaderText(null);
                Optional<ButtonType> res = alert.showAndWait();
                if (res.isPresent() && res.get().equals(ButtonType.CANCEL)) {
                    event.consume();
                } else {
                    notSaved = false;
                }
            }
            if(!notSaved) {
                try(BufferedReader br = new BufferedReader(new FileReader("europa.graph"))) {

                    fromClicked = false;
                    toClicked = false;
                    fromNode = null;
                    toNode = null;

                    for (Node node : listGraph.getNodes()) {
                        listGraph.remove(node);
                    }

                    center.getChildren().clear();
                    gc.clearRect(0, 0, nodeCanvas.getWidth(), nodeCanvas.getHeight());
                    int lineNr = 0;
                    String str;

                    while((str = br.readLine()) != null) {

                        lineNr++;

                        if(lineNr == 1) {
                            Image image = new Image(str);
                            imageView = new ImageView(image);
                            primaryStage.setMinHeight(840);
                            center.getChildren().add(imageView);
                            center.getChildren().add(nodeCanvas);
                        }

                        if (lineNr == 2) {
                            String[] stringArray1 = str.split(";");

                            for(int i = 0; i < stringArray1.length; i += 3) {
                                nodeSpawner(stringArray1[i], Double.parseDouble(stringArray1[i+1]), Double.parseDouble(stringArray1[i+2]));
                            }
                        } else if (lineNr > 2) {

                            String[] stringArray2 = str.split(";");
                            for(int i = 0; i < stringArray2.length; i += 4) {
                                Node nodeA = null;
                                Node nodeB = null;

                                Set<Node> nodeSet = listGraph.getNodes();

                                for (Node node: nodeSet) {
                                    if(node.getName().equals(stringArray2[i+1])) {
                                        nodeB = node;
                                    }
                                }
                                for (Node node : nodeSet) {
                                    if(node.getName().equals(stringArray2[i])) {
                                        nodeA = node;
                                    }
                                }
                                if(listGraph.getEdgeBetween(nodeA, nodeB) == null) {
                                    drawConnection(nodeA, nodeB, stringArray2[i+2], Integer.parseInt(stringArray2[i+3]));
                                }
                            }
                        }
                    }

                    notSaved = true;

                } catch (IOException e) {
                    notSaved = true;
                    Alert alert = new Alert(AlertType.ERROR, e.getMessage());
                    alert.setTitle("Error!");
                    alert.setHeaderText(null);
                    alert.showAndWait();
                }
            }
        }
    }

    class SaveImageHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle (ActionEvent event) {
            try {
                WritableImage image = center.snapshot(null, null);
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
                ImageIO.write(bufferedImage, "png", new File("capture.png"));
            } catch (IOException exception) {
                Alert alert = new Alert(AlertType.ERROR, exception.getMessage());
                alert.showAndWait();
            }
        }
    }

    class ExitHandler implements EventHandler<WindowEvent> {
        @Override
        public void handle(WindowEvent event) {
            if(notSaved) {
                Alert alert = new Alert(AlertType.CONFIRMATION);
                alert.setTitle("WARNING!");
                alert.setHeaderText(null);
                alert.setContentText("Unsaved changes, exit anyway?");
                Optional<ButtonType> res = alert.showAndWait();
                if (res.isPresent() && res.get() == ButtonType.CANCEL) {
                    event.consume();
                }
            }
        }
    }

    class ExitItemHandler implements EventHandler<ActionEvent>{
        @Override public void handle(ActionEvent event){
            primaryStage.fireEvent(new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST));
        }
    }

    class SaveHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {

            File file = new File("europa.graph");

            try (PrintWriter bw = new PrintWriter(new FileWriter(file))) {

                StringBuilder nodeStr = new StringBuilder();
                bw.println(imageView.getImage().getUrl());

                for (Node node: listGraph.getNodes()) {
                    nodeStr.append(node.print());
                }

                if(nodeStr.length() > 0) {
                    nodeStr = new StringBuilder(nodeStr.substring(0, nodeStr.length() - 1));
                    bw.println(nodeStr);
                }

                for (Node node : listGraph.getNodes()) {
                    for (Edge<Node> edge : listGraph.getEdgesFrom(node)) {
                        bw.println(node.getName() + ";" + edge.getDestination().getName() + ";" + edge.print());
                    }
                }

                if(toNode != null){
                    toNode.paintUnselected();
                    toNode = null;
                    toClicked = false;
                }

                if(fromNode != null) {
                    fromNode.paintUnselected();
                    fromNode = null;
                    fromClicked = false;
                }
                notSaved = false;
            } catch (FileNotFoundException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Can't open file!");
                alert.setTitle("Error!");
                alert.setHeaderText(null);
                alert.showAndWait();
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "IO-error " + e.getMessage());
                alert.setTitle("Error!");
                alert.setHeaderText(null);
                alert.showAndWait();
            }
        }
    }


    //BUTTONS HANDLERS
    class ClickHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent event) {
            double x = event.getX();
            double y = event.getY();

            TextInputDialog nameSelect = new TextInputDialog();
            nameSelect.setTitle("Name?");
            nameSelect.setContentText("Name: ");

            Optional<String> res = nameSelect.showAndWait();
            if (res.isPresent() && !nameSelect.getEditor().getText().isEmpty()) {
                nodeSpawner(nameSelect.getEditor().getText(), x, y);
                notSaved = true;
            }
            center.setOnMouseClicked(null);
            scene.setCursor(Cursor.DEFAULT);
            newPlace.setDisable(false);
        }
    }

    class NodeClickHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent event) {
            Node node = (Node) event.getSource();

            //System.out.println(node.getFill());

            if(!fromClicked && !toClicked) {
                node.paintSelected();
                fromNode = node;
                fromClicked = true;
            } else if(!toClicked && node != fromNode) {
                node.paintSelected();
                toNode = node;
                toClicked = true;
            } else if(toClicked && fromClicked && node == toNode) {
                node.paintUnselected();
                toNode = null;
                toClicked = false;
            } else if (fromClicked && toClicked && node == fromNode) {
                node.paintUnselected();
                toClicked = false;
                fromNode = toNode;
                toNode = null;
            } else if(fromClicked && !toClicked) {
                node.paintUnselected();
                fromNode = null;
                fromClicked = false;
            }
        }
    }

    class NewPlaceHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            newPlace.setDisable(true);
            scene.setCursor(Cursor.CROSSHAIR);
            center.setOnMouseClicked(new ClickHandler());
        }
    }

    class FindPathHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            if(fromNode == null || toNode == null) {
                missingNodesAlert();
            } else {
                if(listGraph.getPath(fromNode, toNode) == null){
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Error!");
                    alert.setContentText("There is no path between these cities!");
                    alert.setHeaderText(null);
                    alert.showAndWait();
                    return;
                }
                Dialog<Pair<String, String>> dialog = new Dialog<>();
                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);

                TextArea textArea = new TextArea();
                List<String> strings = new ArrayList<>();

                int weight = 0;

                for (int i = 0; i < listGraph.getPath(fromNode, toNode).size(); i++) {
                    strings.add(listGraph.getPath(fromNode, toNode).get(i).toString());
                    weight += listGraph.getPath(fromNode, toNode).get(i).getWeight();
                }

                StringBuilder sb = new StringBuilder();
                for (String str : strings) {
                    if(sb.length() > 0) {
                        sb.append("\n");
                    }
                    sb.append(str);
                }
                sb.append("\n");
                sb.append("Total ");
                sb.append(weight);

                textArea.setEditable(false);
                textArea.appendText(sb.toString());

                dialog.getDialogPane().setContent(textArea);
                dialog.setTitle("Message");
                dialog.setHeaderText("The path from " + fromNode.getName() + " to " + toNode.getName());
                dialog.showAndWait();
            }
        }
    }

    class NewConnectionHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {

            if(fromNode == null || toNode == null) {
                missingNodesAlert();
            } else {
                if(listGraph.getEdgeBetween(fromNode, toNode) != null) {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Error!");
                    alert.setHeaderText(null);
                    alert.setContentText("There is already a connection!");
                    alert.showAndWait();
                    return;
                }
                Dialog<Pair<String, String>> dialog = new Dialog<>();
                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

                TextField nameTf = new TextField();
                nameTf.setPromptText("Name");
                TextField timeTf = new TextField();
                timeTf.setPromptText("Time");

                gridLayout(dialog, nameTf, timeTf);

                dialog.setResultConverter ( dialogButton -> {
                    if(dialogButton == ButtonType.OK) {
                        return new Pair<>(nameTf.getText(), timeTf.getText());
                    }
                    return null;
                });

                try {
                    Optional<Pair<String, String>> res = dialog.showAndWait();
                    if (res.isPresent() && !nameTf.getText().isEmpty() && !timeTf.getText().isEmpty()) {
                        drawConnection(fromNode, toNode, nameTf.getText(), Integer.parseInt(timeTf.getText()));
                        notSaved = true;
                    } else if (res.isPresent()){
                        Alert alert = new Alert(AlertType.ERROR);
                        alert.setTitle("Error!");
                        alert.setHeaderText(null);
                        alert.setContentText("Missing input");
                        alert.showAndWait();
                    }
                } catch (NumberFormatException e ){
                    Alert alert = new Alert(AlertType.ERROR, e.getMessage());
                    alert.setTitle("Error!");
                    alert.setHeaderText(null);
                    alert.showAndWait();
                }
            }
        }
    }

    class ShowConnectionHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            if(fromNode == null || toNode == null) {
                missingNodesAlert();
            } else {
                if (listGraph.getEdgeBetween(fromNode, toNode) == null) {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Error!");
                    alert.setHeaderText(null);
                    alert.setContentText("There is no connection between these cities!");
                    alert.showAndWait();
                    return;
                }

                Dialog<Pair<String, String>> dialog = new Dialog<>();
                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

                TextField nameTf = new TextField(listGraph.getEdgeBetween(fromNode, toNode).getName());
                nameTf.setPromptText("Name");
                TextField timeTf = new TextField(listGraph.getEdgeBetween(fromNode, toNode).getWeight() + "");
                timeTf.setPromptText("Time");

                nameTf.setEditable(false);
                timeTf.setEditable(false);

                gridLayout(dialog, nameTf, timeTf);

                dialog.showAndWait();
            }
        }
    }

    class ChangeConnectionHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            if(fromNode == null || toNode == null) {
                missingNodesAlert();
            } else {
                if (listGraph.getEdgeBetween(fromNode, toNode) == null) {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Error!");
                    alert.setHeaderText(null);
                    alert.setContentText("There is no connection between these cities!");
                    alert.showAndWait();
                    return;
                }

                Dialog<Pair<String, String>> dialog = new Dialog<>();
                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

                TextField nameTf = new TextField(listGraph.getEdgeBetween(fromNode, toNode).getName());
                nameTf.setPromptText("Name");
                TextField timeTf = new TextField(listGraph.getEdgeBetween(fromNode, toNode).getWeight() + "");
                timeTf.setPromptText("Time");

                nameTf.setEditable(false);

                gridLayout(dialog, nameTf, timeTf);

                dialog.setResultConverter ( dialogButton -> {
                    if(dialogButton == ButtonType.OK) {
                        return new Pair<>(nameTf.getText(), timeTf.getText());
                    }
                    return null;
                });

                try {
                    Optional<Pair<String, String>> res = dialog.showAndWait();
                    if (res.isPresent() && !timeTf.getText().isEmpty()) {
                        listGraph.setConnectionWeight(fromNode, toNode, Integer.parseInt(timeTf.getText()));
                        notSaved = true;
                    } else if (res.isPresent() && timeTf.getText().isEmpty()){
                        Alert alert = new Alert(AlertType.ERROR);
                        alert.setTitle("Error!");
                        alert.setHeaderText(null);
                        alert.setContentText("Missing input");
                        alert.showAndWait();
                    }
                } catch (NumberFormatException e ){
                    Alert alert = new Alert(AlertType.ERROR, e.getMessage());
                    alert.showAndWait();
                }
            }
        }
    }

    //HELP METHODS
    private void gridLayout(Dialog<Pair<String, String>> dialog, TextField nameTf, TextField timeTf) {
        GridPane gp = new GridPane();
        gp.setHgap(10);
        gp.setVgap(10);
        gp.setPadding(new Insets(20, 150, 10, 10));

        gp.add(nameTf, 1, 0);
        gp.add(new Label("Name: "), 0, 0);
        gp.add(timeTf, 1, 1);
        gp.add(new Label("Time: "), 0, 1);

        dialog.getDialogPane().setContent(gp);
        dialog.setTitle("Connection");
        dialog.setHeaderText("Connection from " + fromNode.getName() + " to " + toNode.getName());
    }

    private void missingNodesAlert() {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error!");
        alert.setHeaderText(null);
        alert.setContentText("Two places must be selected!");
        alert.showAndWait();
    }

    private void nodeSpawner(String name, double x, double y) {
        Node c = new Node(name, x, y);
        c.setOnMouseClicked(new NodeClickHandler());
        c.setId(name);

        Label nodeLabel = new Label(name);
        nodeLabel.setTranslateX(x);
        nodeLabel.setTranslateY(y+10);
        nodeLabel.setStyle("-fx-font-weight: bold");

        center.getChildren().add(c);
        center.getChildren().add(nodeLabel);
        listGraph.add(c);
    }

    private void drawConnection(Node a, Node b, String name, int weight) {
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(5);
        gc.strokeLine(a.getX(), a.getY(), b.getX(), b.getY());

        listGraph.connect(a, b, name, weight);
    }
}