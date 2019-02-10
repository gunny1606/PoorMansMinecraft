package game;

import csse2002.block.world.*;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class View {

    private Stage primaryStage; //the stage for the window
    private WorldMap newMap; //the loaded map object

    private Boolean isMapLoaded = false; //checks if the map is loaded or not

    private TilePane tilePane; //the grid which the tiles are drawn on to
    private Canvas canvasChildren[][]; //an array of canvases which are added to each tilepane element
    private Position current; //the current position of the builder in the map

    private HBox rootBox; //container for the window
    private HBox imageBox; //container for the title image
    private HBox directionsContainer; //container for the directional buttons
    private HBox dropBox; //container for the drop button and drop index textfield
    private HBox digBox; //container for the dig button
    private HBox moveButtonBox; //container for the move buttons
    private HBox leftMenuBox; //container for the left side of the GUI
    private HBox inventBox; //container for the inventory information
    private HBox gridContainer; //container for the grid

    private ArrayList<ButtonBase> buttonList = new ArrayList<>(); //list containing all buttons used
    private ArrayList<ButtonBase> directionalButtonList = new ArrayList<>(); //list containing just directional buttons
    private TextField dropIndex; //textfield for the drop index. default value = 0

    private Label infoField; //label for the information field. updated dynamically
    private Label inventLabel; //label for the inventory field, updated dynamically

    private MenuItem saveItem; //save menuitem
    private MenuItem exitItem; //exit menuitem
    private MenuItem loadItem; //load menuitem
    private MenuItem helpItem;

    //buttons
    private Button digButton;
    private Button dropButton;
    private Button top;
    private Button bottom;
    private Button left;
    private Button right;
    private ToggleButton moveBuilder;
    private ToggleButton moveBlock;

    //set integers for certain values
    private int buttonWidth = 60;
    private int buttonHeight = 30;
    private int canvasSize=50;


    //sets the stage
    public View(Stage primaryStage) {
        rootBox = new HBox();
        addComponents();
        this.primaryStage = primaryStage;
        updateButtonStatus();
    }
    //returns the current scene
    public Scene getScene() {
        Scene scene = new Scene(rootBox);
        useDirectionalButtons();
        return scene;
    }
    //pop up window screen at program entry
    public void welcomeScreen() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Welcome to Poor Man's Minecraft!\nWould you like to load a map?", ButtonType.YES, ButtonType.CANCEL);
        Image mineCraft = new Image("images/minecraft2.jpg");
        alert.setGraphic(new ImageView(mineCraft));
        alert.setTitle("Welcome");
        alert.showAndWait();
        if (alert.getResult() == ButtonType.YES) {
            loadItem.fire();
        } else {
            alert.close();
        }
    }
    //enabling use of the direction buttons to control N/S/E/W
    private void useDirectionalButtons() { //todo add to spec that arrow keys are NOT useable when focus is on text field
        rootBox.setFocusTraversable(true);
        rootBox.setOnKeyPressed(event ->  {
            event.consume();
            if (event.getCode() == KeyCode.UP) {
                top.fire();
                primaryStage.requestFocus();
            } else if (event.getCode() == KeyCode.RIGHT) {
                right.fire();
            } else if (event.getCode() == KeyCode.LEFT) {
                left.fire();
            } else if (event.getCode() == KeyCode.DOWN) {
                bottom.fire();
            } else if (event.getCode() == KeyCode.Z) {
                if (!moveBuilder.isSelected()) {
                        moveBuilder.setSelected(true);
                } else {
                    moveBlock.setSelected(true);
                }
            } else if(event.getCode() == KeyCode.X) {
                digButton.fire();
            } else if (event.getCode() == KeyCode.C) {
                dropButton.fire();
            }
        });
    }

    //abbreviating error alert code
    private void newErrorAlert(String title, String message) {
        Alert newAlert = new Alert(Alert.AlertType.ERROR);
        newAlert.setTitle(title);
        newAlert.setHeaderText(message);
        newAlert.showAndWait();
    }


    //updates all buttons with their current state
    private void updateButtonStatus() {
        //disabling buttons until map is loaded
        buttonList.addAll(Arrays.asList(moveBuilder,moveBlock,digButton,top,
                bottom,left,right));

        for (ButtonBase but: buttonList) {
            if (!isMapLoaded) {
                but.setDisable(true);
                dropIndex.setEditable(false);
            } else {
                but.setDisable(false);
                dropIndex.setEditable(true);
            }
        }
    }

    //ensures that if a move is impossible, the relevant directional button will be disabled
    private void updateDirectionalButtonStatus() {
        directionalButtonList.addAll(Arrays.asList(top,bottom,left,right));
        Map targetTile = newMap.getBuilder().getCurrentTile()
                .getExits();
            if (targetTile.get("north") == null) {
                top.setDisable(true);
            }
            if (targetTile.get("east") == null) {
                right.setDisable(true);
            }
            if (targetTile.get("west") == null) {
                left.setDisable(true);
            }
            if (targetTile.get("south") == null) {
                bottom.setDisable(true);
        }
    }

    //updates the inventory label when a game tick occurs
    private void updateInventoryLabel(Label label) {
        String labelinfo = "";
        List<Block> labelInformation = newMap.getBuilder().getInventory();
        List<String> labelInfoAsString = new ArrayList();
        for (Block block:labelInformation){
            String type = block.getBlockType();
            labelInfoAsString.add(type);
            labelinfo = String.join(", ", labelInfoAsString);
        }
        label.setText("Builder Inventory:\n" + labelinfo);
        label.setWrapText(true);

    }

    //updates the information label when a gametick occurs
    private void updateInformation(Label label) {
        final String buildername = newMap.getBuilder().getName();
        String info = "";
        List<String> labelInformation = new ArrayList();
        List<Block> currentTileBlocks =
                newMap.getBuilder().getCurrentTile().getBlocks();
        for (Block block:currentTileBlocks) {
            String type = block.getBlockType();
            labelInformation.add(type);
            info = String.join(", ", labelInformation);
        }
        label.setText("Information:\nBuilder's Name: "+ buildername + "\n" + "Current Tile " +
                "Blocks: " + info +"(top)" );
        label.setWrapText(true);
    }

    // helper method for when user makes an input, updates all relevant parts
    private void gameTick() {
        drawWorld(current);
        updateInventoryLabel(inventLabel);
        updateButtonStatus();
        updateInformation(infoField);
        updatePos(current);
        updateDirectionalButtonStatus();
    }

    //helper method to update the registered position when the builder is moved
    private void updatePos(Position newPos) {
        current = newPos;
        drawWorld(newPos);
    }

    //ensures that when the textfield is moderated, the keys can still be used
    //single keypress to deselect field then second keypress is functional
    private void ignoreSelectedTextField() {
        ArrayList<KeyCode> ignoredKeyCodes = new ArrayList();
        ignoredKeyCodes.add(KeyCode.Z);
        ignoredKeyCodes.add(KeyCode.X);
        ignoredKeyCodes.add(KeyCode.C);
        ignoredKeyCodes.add(KeyCode.UP);
        ignoredKeyCodes.add(KeyCode.LEFT);
        ignoredKeyCodes.add(KeyCode.RIGHT);
        ignoredKeyCodes.add(KeyCode.DOWN);

        dropIndex.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (ignoredKeyCodes.contains(event.getCode())) {
                    rootBox.requestFocus();
                }
        });
    }

    //creates the 9x9 world map view
    private void createTilePane() {
        // assumes that the tilepane will fill the available space
        tilePane = new TilePane();
        tilePane.setMinSize(450,500.0);
        tilePane.setStyle("-fx-background-color: BLANCHEDALMOND");
        tilePane.setPrefColumns(9); // should this be fixed?
        gridContainer.getChildren().add(tilePane);
        canvasChildren = new Canvas[9][];
        for (int i = 0; i<9; i++) {
            canvasChildren[i] = new Canvas[9];
            for (int j = 0; j<9; j++) {
                canvasChildren[i][j] = new Canvas(canvasSize,canvasSize);
                tilePane.getChildren().add(canvasChildren[i][j]);
            }
        }
    }

    //uses drawTile to iterate over the whole 9x9 grid and draw all the tiles
    private void drawWorld(Position pos) {

        current = pos;
        int startX = current.getX();
        int startY = current.getY();
        updateInventoryLabel(inventLabel);

        for (int i=0; i<9; i++) {
            for (int j=0; j<9; j++) { //iterate over 9x9 iXj
                //convert tile coords to grid coords
                int gridX = startX - 4 + j;
                int gridY = startY - 4 + i;
                //create a new position on the grid
                Position newPos = new Position(gridX, gridY);
                //draw tile at that registered position on grid
                drawTile(canvasChildren[i][j], newMap.getTile(newPos),
                        i==4 && j == 4); //always draws builder at centre
            }
        }
        updateInformation(infoField);
    }

    //draws the a tile onto the canvas at a coordinate on the grid
    private void drawTile(Canvas canvas, Tile tile, boolean drawBuilder) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0,0,canvasSize, canvasSize);

        if (tile != null) {
            // draws the background of each tile
            Paint bgPaint = Color.WHITE;

            try { //draws tile of designated colour
                String tileColour = tile.getTopBlock().getColour();
                if (tileColour.equals("green")) {
                    bgPaint = Color.GREEN;
                } else if (tileColour.equals("black")) {
                    bgPaint = Color.BLACK;
                } else if (tileColour.equals("gray")) {
                    bgPaint = Color.GRAY;
                } else if (tileColour.equals("brown")) {
                    bgPaint = Color.BROWN;
                }
                gc.setFill(bgPaint);
                gc.fillRect(1,1,canvasSize-2,canvasSize-2);

            } catch (TooLowException e) {
            }
            if (drawBuilder) { //draw yellow circle for builder on (4,4)
                gc.setFill(Color.YELLOW);
                gc.fillOval(23,12.5,5,5);
            }

            // draws the arrows indicating an exit is present
            for (String exit: tile.getExits().keySet()) {
                gc.setFill(Color.WHITE);
                gc.setStroke(Color.BLACK);


                switch(exit) {
                    case "north":
                        double[] xP1 = {25.0,28.0,22.0};
                        double[] yP1 = {3.0,6.0,6.0};
                        gc.fillPolygon(xP1, yP1, 3);
                        break;
                    case "east":
                        double[] xP2 = {47.0,44.0,44.0};
                        double[] yP2 = {25.0,28.0,22.0};
                        gc.fillPolygon(xP2, yP2, 3);
                        break;
                    case "south":
                        double[] xP3 = {25.0,22.0,28.0};
                        double[] yP3 = {47.0,44.0,44.0};
                        gc.fillPolygon(xP3, yP3, 3);
                        break;
                    case "west":
                        double[] xP4 = {3.0,6.0,6.0};
                        double[] yP4 = {25.0,28.0,22.0};
                        gc.fillPolygon(xP4, yP4, 3);
                        break;
                }
            }

            // draw number of blocks on tile onto canvas on tile onscreen
            gc.setLineWidth(3.0);
            gc.setStroke(Color.BLACK);
            gc.fillText(Integer.toString(tile.getBlocks().size()), 23,32);

            }

    }

    //checks if the move Builder button is selected
    private boolean isMoveBuilderSelected() {
        //helps moveBuilderOrBlock decide what to do
        if (moveBuilder.isSelected()) {
            return true;
        } else {
            return false;
        }
    }


    //moves the Builder or a Block depending on bool state
    private void moveBuilderOrBlock(int dx, int dy, String direction) {

        Position newPos;
        if (isMoveBuilderSelected()) {
            //move the builder in a direction
            int currentX = current.getX();
            int currentY = current.getY();
            newPos = new Position(currentX + dx, currentY + dy);
            try {
                if (newMap.getTile(newPos) != null) {
                    newMap.getBuilder().moveTo(newMap.getTile(newPos));
                    updatePos(newPos);
                    gameTick();
                } else {
                    newErrorAlert("No Tile Error", "There is no tile to move to!");
                }
            } catch (NoExitException e) {
                newErrorAlert("Exit Error", "Cannot make this move! " +
                        "Either there is no exit to the block, or the next " +
                        "block is too high/low");
            }
        } else {
            // move the block in a direction
            try {
                Tile tile = newMap.getBuilder().getCurrentTile();
                tile.moveBlock(direction);
                gameTick();
            } catch (TooHighException e) {
                newErrorAlert("Too High Error", "The target block is too high to move the block to!");
            } catch (InvalidBlockException e) {
                newErrorAlert("Invalid Block Error", "You cannot move this block!");
            } catch (NoExitException e) {
                newErrorAlert("No Exit Error", "There is nowhere to move the block to!");
            }

        }
    }
    //add all the GUI components together and add to root window
    private void addComponents() {

        int windowSetHeight = 650;
        int rightBoxMinWidth = 250;
        String windowBG = ("-fx-background-color: lightgrey");

        VBox leftBox = new VBox();
        leftBox.setPrefHeight(windowSetHeight);
        leftBox.setMaxHeight(windowSetHeight);
        /* Add padding, colour to the left side */
        leftBox.setPadding(new Insets(10, 10, 10, 10));
        leftBox.setStyle(windowBG);
        addLeftSideComponents(leftBox);

        /* Another layout node for the left side of the GUI */
        VBox rightBox = new VBox();
        rightBox.setPrefHeight(windowSetHeight);
        rightBox.setMaxHeight(windowSetHeight);
        rightBox.setMinWidth(rightBoxMinWidth);
        rightBox.setPrefSize(rightBoxMinWidth, windowSetHeight);

        /* add colour and padding to the right layout */
        rightBox.setSpacing(10);
        rightBox.setPadding(new Insets(10, 20, 10, 20));
        rightBox.setStyle(windowBG);
        rightBox.setAlignment(Pos.CENTER_RIGHT);
        addRightSideComponents(rightBox);

        /* add both layouts to the root HBox layout*/
        rootBox.getChildren().addAll(leftBox, rightBox);
    }

    private void addRightSideComponents(VBox box) {

        directionalButtonSetup();
        rightSideContainerSetup();
        moveButtonSetup();
        digButtonSetup();
        dropSetup();


        box.getChildren().addAll(imageBox, directionsContainer, moveButtonBox,
                digBox, dropBox);

    }

    //adds all the left side GUI together
    private void addLeftSideComponents(VBox box) {

        HBox barBox = new HBox();
        menuBarSetup();
        exitButtonHandler(exitItem);
        loadButtonHandler(loadItem);
        saveButtonHandler(saveItem);

        gridContainer = new HBox();
        gridContainer.setMinSize(450,500.0);
        gridContainer.setStyle("-fx-border-color: black");
        createTilePane();

        createInfoField();
        createInventoryInfo();

        barBox.getChildren().addAll(inventBox, infoField);
        barBox.setPrefHeight(100.0);
        /* add everything to the left VBox (which is passed as argument) */
        box.getChildren().addAll(leftMenuBox, gridContainer, barBox);

    }

    //sets up the GUI elements of the menubar
    private void menuBarSetup() {
        /*Add a file menu with load and save options*/
        leftMenuBox = new HBox();
        MenuBar menu = new MenuBar();
        Menu fileMenu = new Menu("File");
        loadItem = new MenuItem("Load World Map");
        saveItem = new MenuItem("Save World Map");
        exitItem = new MenuItem("Exit World Map");
        fileMenu.getItems().addAll(loadItem, saveItem, exitItem);

        Menu helpMenu = new Menu("Help");
        Menu keyboardShortCuts = new Menu("Keyboard Shortcuts");
        helpMenu.getItems().add(keyboardShortCuts);
        List<String> kbShortCs = new ArrayList();

        kbShortCs.add("North = UP");
        kbShortCs.add("South = DOWN");
        kbShortCs.add("West = LEFT");
        kbShortCs.add("East = RIGHT");
        kbShortCs.add("Toggle Move Buttons = Z");
        kbShortCs.add("Dig = X");
        kbShortCs.add("Drop = C");
        for (String row: kbShortCs) {
            RadioMenuItem shortcut = new RadioMenuItem(row);
            keyboardShortCuts.getItems().add(shortcut);
        }
        menu.getMenus().addAll(fileMenu, helpMenu);
        leftMenuBox.getChildren().add(menu);

    }

    //handler for the exit button action
    private void exitButtonHandler(MenuItem bt) {
        bt.setOnAction(event ->  { //
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure?", ButtonType.YES, ButtonType.CANCEL);
            confirm.showAndWait();
            if (confirm.getResult() == ButtonType.YES) {
                System.exit(1);
            } else {
                confirm.close();
            }
        });
    }

    //information alert helper method
    private void newInfoAlert(String infoTitle) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, infoTitle);
        Image mineCraft = new Image("images/minecraft2.jpg");
        alert.setGraphic(new ImageView(mineCraft));
        alert.showAndWait();
    }

    private void loadButtonHandler(MenuItem bt) {
        //set load handler
        bt.setOnAction(event -> {
            FileChooser loadFile = new FileChooser();
            loadFile.setTitle("Load a new map");
            File file = loadFile.showOpenDialog(primaryStage);
            if (file != null) { // they chose a file
                try{
                    newMap = new WorldMap(file.getAbsolutePath());
                    current = newMap.getStartPosition();
//                    drawWorld(current);
                } catch (WorldMapFormatException e){
                    newMap = null;
                    newErrorAlert("World Map Format Error", "There is something wrong with" +
                            " the format of the map attempting to be loaded.");
                } catch(WorldMapInconsistentException e) {
                    newErrorAlert("World Map Inconsistent Error", "The map attempting to be loaded is inconsistent");
                } catch(FileNotFoundException e) {
                    newErrorAlert("File Not Found Error", "Cannot find this file!");
                }
                if (newMap != null) {
                    drawWorld(current);
                    isMapLoaded = true;
                    gameTick();
                    newInfoAlert("Map Loaded Successfully!");


                }
            }
        });
    }

    private void saveButtonHandler(MenuItem bt) {
        //set save handler
        bt.setOnAction(event -> {
            if (newMap != null) {
                FileChooser saveFile = new FileChooser();
                saveFile.setInitialFileName("NewMap1");
                saveFile.setTitle("Save the world map");
                File file = saveFile.showSaveDialog(primaryStage);
                if (file != null) {
                    try {
                        if (!file.getName().contains(".")) {
                            newMap.saveMap(file.getAbsolutePath()+".txt");
                        } else {
                            newMap.saveMap(file.getAbsolutePath());
                        }

                    } catch (IOException e) {
                        newErrorAlert("Save Error", "Cannot save this file!");
                    }
                }
            } else {
                newErrorAlert("Map Error", "There is no map to save!");
            }


        });

    }

    private void createInfoField() {
        HBox infoBox = new HBox();
        infoBox.setAlignment(Pos.BASELINE_LEFT);
        infoField = new Label();
        infoField.setWrapText(true);
        infoField.setTextAlignment(TextAlignment.LEFT);
        infoField.setPadding(new Insets(10, 10, 10, 10));
        infoField.setAlignment(Pos.CENTER);
        infoField.setText("Information:\nNo Map Loaded");
        infoBox.getChildren().addAll(infoField);
        infoField.setPrefWidth(250.0);
    }


    private void createInventoryInfo() {
        inventBox = new HBox();
        inventBox.setPadding(new Insets(10, 10, 10, 10));
        inventBox.setSpacing(15);
        inventLabel = new Label("Builder Inventory:\n N/A");
        inventBox.setAlignment(Pos.TOP_LEFT);
        inventLabel.setWrapText(true);
        inventLabel.setTextAlignment(TextAlignment.LEFT);
        inventBox.getChildren().add(inventLabel);

    }



    private void rightSideContainerSetup() {
        imageBox = new HBox();
        Image mineCraft = new Image("images/minecraft2.jpg");
        imageBox.getChildren().add(new ImageView(mineCraft));
        directionsContainer = new HBox();
        BorderPane directionsPane = new BorderPane();
        directionsContainer.getChildren().add(directionsPane);

        //set position and padding of buttons
        directionsPane.setTop(top);
        directionsPane.setRight(right);
        directionsPane.setBottom(bottom);
        directionsPane.setLeft(left);

        //create padding around buttons
        Insets ins1 = new Insets(10);
        BorderPane.setMargin(top, ins1);
        BorderPane.setMargin(bottom, ins1);
        BorderPane.setMargin(left, ins1);
        BorderPane.setMargin(right, ins1);

        //align to middle of borderpane
        BorderPane.setAlignment(top, Pos.CENTER);
        BorderPane.setAlignment(bottom, Pos.CENTER);
        BorderPane.setAlignment(left, Pos.CENTER);
        BorderPane.setAlignment(right, Pos.CENTER);

    }

    private void directionalButtonSetup() {
        //create directional button, and set preferred sizes
        top = new Button("North"); //
        top.setOnAction(event ->  {
            moveBuilderOrBlock(0,-1, "north");
        });
        right = new Button("East");
        right.setOnAction(event ->  {
            moveBuilderOrBlock(1,0, "east");
        });
        bottom = new Button("South");
        bottom.setOnAction(event -> {
            moveBuilderOrBlock(0,1, "south");
        });
        left = new Button("West");
        left.setOnAction(event ->  {
            moveBuilderOrBlock(-1,0, "west");
        });
        top.setPrefSize(buttonWidth,buttonHeight);
        bottom.setPrefSize(buttonWidth,buttonHeight);
        left.setPrefSize(buttonWidth,buttonHeight);
        right.setPrefSize(buttonWidth,buttonHeight);

    }

    private void moveButtonSetup() {
        //creating GUI elements of the move buttons
        moveButtonBox = new HBox();
        ToggleGroup group = new ToggleGroup();
        moveButtonBox.setPrefWidth(200.0);
        moveButtonBox.setSpacing(30);

        moveBuilder = new ToggleButton("Move Builder");
        moveBuilder.setAlignment(Pos.CENTER_LEFT);
        moveBuilder.setSelected(true);
        moveBuilder.setFocusTraversable(false);
        moveBuilder.setToggleGroup(group);

        moveBlock = new ToggleButton("Move Block");
        moveBlock.setAlignment(Pos.CENTER_RIGHT);
        moveBlock.setFocusTraversable(false);
        moveBlock.setToggleGroup(group);

        moveButtonBox.getChildren().addAll(moveBuilder, moveBlock);

    }


    //controls the dig button action handler
    private void digButtonHandler() {
        digButton.setOnAction(event -> {
            try {
                newMap.getBuilder().digOnCurrentTile();
                gameTick();
            } catch (TooLowException e) { //if it is too low to dig
                newErrorAlert("Dig Error", "You are too low to dig!");
            } catch (InvalidBlockException e) { //if the block is not diggable
                newErrorAlert("Dig Error", "You cannot dig on this block!");
            }
        });
    }

    private void digButtonSetup() {
        //dig button GUI elements
        digBox = new HBox();
        digButton = new Button("Dig");
        digButton.setFocusTraversable(false);
        digBox.getChildren().add(digButton);
        digButton.setPrefSize(buttonWidth, buttonHeight);

        //add action handler
        digButtonHandler();

    }

    //ensures that a textfield can take integers only
    private void dropIndexIntegersOnly(TextField tf) {
        tf.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*")) {
                    tf.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
    }

    //controls the drop button action handler
    private void dropButtonHandler() {
        dropButton.setOnAction(event -> {
            int inventIndex = Integer.parseInt(dropIndex.getText());
            try {
                newMap.getBuilder().dropFromInventory(inventIndex);
                gameTick();
            } catch (InvalidBlockException e) { //if you cannot drop the block for another reason
                newErrorAlert("Drop Error", "You cannot drop this type of " +
                        "block here");
            } catch (TooHighException e) { //if the current tile is too high to drop a block
                newErrorAlert("Drop Error", "It is too high to drop!");
            }
        });
    }

    //ensure that drop button is disabled when text field is empty
    private void disableDropButtonWhenEmptyTextField() {
        BooleanBinding b = new BooleanBinding() {
            {
                bind(dropIndex.textProperty());
            }
            @Override
            protected boolean computeValue() {
                return (dropIndex.getText().isEmpty());
            }
        }; dropButton.disableProperty().bind(b);
    }

    //pulls together elements of drop button/textfield for integer entry
    private void dropSetup() {
        //create GUI elements of the drop button
        dropButton = new Button("Drop");
        dropButton.setPrefSize(buttonWidth, buttonHeight);
        dropButton.setAlignment(Pos.CENTER);

        dropIndex = new TextField();
        dropIndex.setPromptText("Enter drop index");
        dropIndex.setText("0");
        dropIndex.setPrefColumnCount(10);
        dropIndex.setAlignment(Pos.CENTER);
        dropIndex.setFocusTraversable(false);

        dropBox = new HBox();
        dropBox.setSpacing(5);
        dropBox.getChildren().addAll(dropButton, dropIndex);
        //add helper methods
        ignoreSelectedTextField();
        dropIndexIntegersOnly(dropIndex);
        dropButtonHandler();
        disableDropButtonWhenEmptyTextField();



    }

}