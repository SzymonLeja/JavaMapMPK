package com.example.app;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.geometry.*;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.*;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
public class App extends Application implements EventHandler<javafx.event.ActionEvent> {

    private MapView mapView;
    public ArrayList<String> busParams = new ArrayList<>();
    public ArrayList<DataObject> dataList = new ArrayList<>();
    public String[] busLines = {"A", "C", "D", "K", "N", "100", "101", "102", "103", "104", "105", "106", "107", "108", "109", "110", "111", "112", "113", "114", "115", "116", "118", "119", "120", "121", "122", "124", "125", "126", "127", "128", "129", "130", "131", "132", "133", "134", "136", "140", "142", "143", "144", "145", "146", "147", "148", "149", "150", "151", "206", "240", "241", "242", "243", "244", "245", "246", "247", "248", "249", "250", "251", "253", "255", "257", "259", "315", "319", "602", "607", "700", "703", "731", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "15", "16", "17", "20", "23", "31", "33", "70", "74"};
    private int delayTime = 3;
    private int radiusInt = 1;
    private final GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
    private final Text choosenLinesText = new Text("Aktualna linia: " + busParams);
    private final Text warningText = new Text();
    private Graphic polyGraphic;
    private ArrayList<Boolean> isBusInPolygon = new ArrayList<>();
    private PointCollection pc = new PointCollection(SpatialReferences.getWgs84());
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {
        Image icon = new Image("http://www.mpk.lodz.pl/files/solaris_nu_18_e6.jpg");
        stage.getIcons().add(icon);
        // set the title and size of the stage and show it
//        busParams.add("206");
        stage.setTitle("MPK Tracker");
        stage.setWidth(1600);
        stage.setHeight(920);
        stage.show();

        // create a JavaFX scene with a stack pane as the root node, and add it to the scene
        StackPane stackPane = new StackPane();
        Scene scene = new Scene(stackPane);
        stage.setScene(scene);
        String yourApiKey = "AAPKab5185e54f704bd28520da7280c86c98Kx2K2EWpOaF-L9MxIYp5_9ANbgIOOuxByuOkLiITFjZYIPvxrrYi95NMdqDpqxXd";
        ArcGISRuntimeEnvironment.setApiKey(yourApiKey);
        mapView = new MapView();
        //components creation
        VBox verticallBox = new VBox();
        HBox horizontallBox = new HBox();
        horizontallBox.setSpacing(3);
        Slider slider = createSlider();
        Slider sliderRange = createSlider();
        Text mainSliderLabel = new Text("Refresh time");
        Text radiusLabel = new Text("Radius of area");
        createButtons(verticallBox,horizontallBox);
        verticallBox.getChildren().addAll(mainSliderLabel,slider,radiusLabel, sliderRange);
        stackPane.getChildren().addAll(mapView, verticallBox, warningText, choosenLinesText);

        //styling scene
        styleScene(verticallBox,stackPane,slider);
        //setting map
        ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_NAVIGATION);
        mapView.setMap(map);
        mapView.setViewpoint(new Viewpoint(51.109405517578, 017.047897338867, 110000));
        mapView.getGraphicsOverlays().add(graphicsOverlay);
        mapView.setOnMouseClicked(addArea);
        map.setMaxScale(4000);
        //setting up points loop
        refreshPoints();

        sliderRange.valueProperty().addListener((obs, oldVal, newVal) -> radiusInt = (int) Math.round((Double) newVal));
        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if(delayTime != (int) Math.round((Double) newVal)){
                delayTime = (int) Math.round((Double) newVal);
            }
            scheduler.shutdown();
//            scheduler.shutdown();
            try {
                scheduler.awaitTermination(100,TimeUnit.MILLISECONDS);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
            scheduler = Executors.newScheduledThreadPool(1);
            refreshPoints();

        });
    }

    private void styleScene(VBox verticallBox, StackPane stackPane, Slider slider){
        verticallBox.setSpacing(5);
        verticallBox.setPadding(new Insets(3, 0, 0, 3));
        verticallBox.setPrefHeight(stackPane.getHeight() / 4);
        verticallBox.setMaxHeight(stackPane.getHeight() / 4);
        verticallBox.setPrefWidth(950);
        verticallBox.setMaxWidth(950);
        StackPane.setAlignment(slider, Pos.TOP_CENTER);
        StackPane.setAlignment(mapView, Pos.BOTTOM_CENTER);
        StackPane.setAlignment(verticallBox, Pos.TOP_CENTER);
        StackPane.setAlignment(choosenLinesText, Pos.TOP_CENTER);
        StackPane.setMargin(choosenLinesText,new Insets(5,0,0,0));
        StackPane.setMargin(warningText,new Insets(25,0,0,0));
        StackPane.setMargin(verticallBox, new Insets(65,0,0,0));
        StackPane.setAlignment(warningText, Pos.TOP_CENTER);
        warningText.setStyle("-fx-text-fill: #FFFF0101;-fx-font-weight: bold;-fx-font-size: 16;-fx-background-color: #FFFF0101");
        choosenLinesText.setStyle("-fx-font-weight: bold;-fx-font-size: 14");
    }

    //Creation polygon area on mouse rightclick
    EventHandler<? super MouseEvent> addArea = (EventHandler<MouseEvent>) e -> {
        if (e.getButton() == MouseButton.SECONDARY) {
            Point2D geoViewPoint = new Point2D(e.getX(), e.getY());
            Point geoPoint = mapView.screenToLocation(geoViewPoint);
            double radius = 0.0015*radiusInt;

            int ptCount = 240;
            double slice = 2 * Math.PI / ptCount;
            pc = new PointCollection(SpatialReferences.getWgs84());

            String coordinatesString = CoordinateFormatter.toLatitudeLongitude(geoPoint, CoordinateFormatter
                    .LatitudeLongitudeFormat.DECIMAL_DEGREES, 6);
            String[] splitCoordinates = coordinatesString.split(" ");
            float lat = Float.parseFloat(splitCoordinates[0].replace("N",""));
            float lon = Float.parseFloat((splitCoordinates[1].replace("E","")));


            for (int i = 0; i <= ptCount; i++) {
                double rad = slice * i;
                double px = lon + radius * Math.cos(rad);
                double py = lat + radius*0.62 * Math.sin(rad);
                Point polygonPoint = new Point(px,py);
                pc.add(polygonPoint);
            }
            Polygon polygon = new Polygon(pc);
            SimpleLineSymbol blueOutlineSymbol =
                    new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, 0xFF0063FF, 2);
            SimpleFillSymbol polygonFillSymbol =
                    new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, 0x80FF5733, blueOutlineSymbol);
            polyGraphic = new Graphic(polygon, polygonFillSymbol);
            createPoints();
        }
    };

    //returns slider with certain parametrs
    private Slider createSlider(){
        Slider slider = new Slider();
        slider.setMin(1);
        slider.setMax(10);
        slider.setValue(delayTime);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(3);
        slider.setMinorTickCount(3);
        slider.setBlockIncrement(1);
        slider.setPrefWidth(750);
        slider.setMaxWidth(750);

        return slider;
    }
    // create buttons
    private void createButtons(VBox verticallBox, HBox horizontallBox){
        ArrayList<Button> buttons = new ArrayList<>(busLines.length);
        int i = 0;
        for (String buttonValue : busLines) {
            if (i % 20 == 0) {
                verticallBox.getChildren().add(horizontallBox);
                horizontallBox = new HBox();
                horizontallBox.setSpacing(3);
            }
            buttons.add(new Button(buttonValue));
            horizontallBox.getChildren().add(buttons.get(i));
            buttons.get(i).setStyle("-fx-background-color: gray;-fx-text-fill:black");
            buttons.get(i).setPrefWidth(60);
            buttons.get(i).setOnAction(this);
            i++;
        }
        verticallBox.getChildren().add(horizontallBox);
    }

    //handling buttons when clicked
    @Override
    public void handle(javafx.event.ActionEvent event){
        var line = ((Button) event.getSource()).getText();
        Object buttonEvent = event.getSource();
        if (busParams.contains(line)) {
            busParams.remove(line);
            ((Button) buttonEvent).setStyle("-fx-background-color: gray;-fx-text-fill:black");
        } else {
            busParams.add(line);
            ((Button) buttonEvent).setStyle("-fx-background-color: #9a1818;-fx-text-fill:white");
        }
        choosenLinesText.setText("Aktualne linie: " + busParams.toString());
        dataList = new ArrayList<>();
        if (!busParams.isEmpty()) {
            try {
                ApiConnection.requestAPI(dataList, busParams);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        createPoints();
    }
    //create points from data collected from API + polygon if set
    private void createPoints() {
        graphicsOverlay.getGraphics().clear();
        isBusInPolygon = new ArrayList<>();
        HashSet<String> vehiclesInArea = new HashSet<>();
        warningText.setText("");
        if(polyGraphic != null && !pc.isEmpty()){
            Polygon polygon = new Polygon(pc);
            SimpleLineSymbol blueOutlineSymbol =
                    new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, 0xFF0063FF, 2);
            SimpleFillSymbol polygonFillSymbol =
                    new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, 0x80FF5733, blueOutlineSymbol);
            polyGraphic = new Graphic(polygon, polygonFillSymbol);
            graphicsOverlay.getGraphics().add(polyGraphic);
        }
        int iterator = 0;
        for (DataObject data : dataList) {
            Polygon polygon = new Polygon(pc);
            TextSymbol textSymbol = new TextSymbol();
            textSymbol.setText(data.getName());
            textSymbol.setFontWeight(TextSymbol.FontWeight.BOLD);
            textSymbol.setSize(15);
            Point point = new Point(data.getY(), data.getX(), SpatialReferences.getWgs84());
            if(!pc.isEmpty()){
                isBusInPolygon.add(GeometryEngine.contains(polygon,point));
                if(GeometryEngine.contains(polygon,point)){
                    vehiclesInArea.add(data.getName());
                    if(!warningText.getText().isEmpty()){
                        if(!warningText.getText().contains(data.getName())){
                            warningText.setText(warningText.getText() + " " + data.getName());
                        }
                    } else {
                        warningText.setText("W zaznaczonym obszarze wystepuja autobusy lini: " + data.getName());
                    }
                }
            }
            SimpleMarkerSymbol simpleMarkerSymbol =
                    new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.SQUARE, 0xFFFF5733, 25);
            SimpleLineSymbol blueOutlineSymbol =
                    new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, 0xFF0063FF, 2);
            simpleMarkerSymbol.setOutline(blueOutlineSymbol);
            Graphic pointGraphic = new Graphic(point, simpleMarkerSymbol);
            Graphic pointText = new Graphic(point, textSymbol);
            pointGraphic.setZIndex(iterator+2);
            pointText.setZIndex(iterator+3);
            graphicsOverlay.getGraphics().add(pointGraphic);
            graphicsOverlay.getGraphics().add(pointText);
            iterator++;
        }
        if(vehiclesInArea.isEmpty()){
            warningText.setText("");
        }

    }
    //scheduler that refresh points every @delayTime in seconds
    private void refreshPoints() {
        final Runnable task = () -> {
            if (!(busParams.isEmpty())) {
                dataList = new ArrayList<>();
                try {
                    ApiConnection.requestAPI(dataList, busParams);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                createPoints();
            } else {
                dataList = new ArrayList<>();
                createPoints();
            }
            if(!isBusInPolygon.contains(true)){
                warningText.setText("");
            }

        };
        scheduler.scheduleAtFixedRate(task, 10, delayTime, TimeUnit.SECONDS);
    }


    @Override
    public void stop() {
        if (mapView != null) {
            mapView.dispose();
        }
    }
}