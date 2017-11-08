import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

public class JavaFX2Demo extends Application {


    public static void main(String[] args)
    {
        launch( args );
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        // シーングラフの作成
        // ルートノードを作成
        VBox    root        = new VBox( 2 );

        // VBoxを追加
        VBox        vbox        = new VBox();
        Text        vboxNode1   = new Text("【その１】");
        Text        vboxNode2   = new Text("【その２】");
        vbox.getChildren().addAll( vboxNode1 , vboxNode2 );
        root.getChildren().add( createNameAndPane( "VBox\t\t\t" , vbox ) );

        // HBoxを追加
        HBox        hbox    = new HBox();
        Text        hboxNode1   = new Text("【その１abc】");
        Text        hboxNode2   = new Text("【その２】");
        hbox.getChildren().addAll( hboxNode1 , hboxNode2 );
        root.getChildren().add( createNameAndPane( "HBox\t\t" , hbox ) );

        // FlowPane(横)を追加
        // ノード間の幅を設定
        FlowPane    hflow       = new FlowPane();
        Text        hflowNode1  = new Text("【その１123456789012345678901234567890】") ;
        Text        hflowNode2  = new Text("【その２】") ;
        Text        hflowNode3  = new Text("【その３】") ;
        hflow.setOrientation( Orientation.HORIZONTAL );
        hflow.setHgap( 5 );
        hflow.getChildren().addAll( hflowNode1 , hflowNode2 , hflowNode3 );
        root.getChildren().add( createNameAndPane( "FlowPane\t\t" , hflow ) );

        // FlowPane(縦)を追加
        // ノード間の幅を設定
        FlowPane    vflow       = new FlowPane();
        Text        vflowNode1  = new Text("【その１123456789012345678901234567890】") ;
        Text        vflowNode2  = new Text("【その２】") ;
        Text        vflowNode3  = new Text("【その３】") ;
        vflow.setOrientation( Orientation.VERTICAL );
        vflow.setVgap( 5 );
        vflow.getChildren().addAll( vflowNode1 , vflowNode2 , vflowNode3 );
        root.getChildren().add( createNameAndPane( "\t\t\t" , vflow ) );

        // TextFlowを追加
        TextFlow    text        = new TextFlow();
        Text        textNode1   = new Text("【その１123456789012345678901234567890】") ;
        Text        textNode2   = new Text("【その２】") ;
        Text        textNode3   = new Text("【その３】") ;
        text.getChildren().addAll( textNode1 , textNode2 , textNode3  );
        root.getChildren().add( createNameAndPane( "TextFlow\t\t" , text ) );

        // AnchorPaneを追加
        AnchorPane  anchor      = new AnchorPane();
        Text        anchorNode1 = new Text("【その１abc】") ;
        Text        anchorNode2 = new Text("【その２】") ;
        anchorNode1.setLayoutX( 20 );
        anchorNode1.setLayoutY( 50 );
        anchorNode2.setLayoutX( 50 );
        anchorNode2.setLayoutY( 20 );
        anchor.getChildren().addAll( anchorNode1 , anchorNode2 );
        root.getChildren().add( createNameAndPane( "AnchorPane\t" , anchor ) );

        // BorderPaneを追加
        // Alignの設定
        // マージンの設定
        BorderPane  border      = new BorderPane();
        Text        borderNode1 = new Text("【その１abc】") ;
        Text        borderNode2 = new Text("【その２】") ;
        Text        borderNode3 = new Text("【その３】") ;
        Text        borderNode4 = new Text("【その４】") ;
        Text        borderNode5 = new Text("【その５】") ;
        BorderPane.setAlignment( borderNode1 , Pos.TOP_CENTER );
        BorderPane.setAlignment( borderNode2 , Pos.TOP_CENTER );
        BorderPane.setAlignment( borderNode3 , Pos.TOP_CENTER );
        BorderPane.setAlignment( borderNode4 , Pos.TOP_CENTER );
        BorderPane.setAlignment( borderNode5 , Pos.TOP_CENTER );
        BorderPane.setMargin( borderNode1 , new Insets( 10,10,10,10 ) );
        BorderPane.setMargin( borderNode2 , new Insets( 10,10,10,10 ) );
        BorderPane.setMargin( borderNode3 , new Insets( 10,10,10,10 ) );
        BorderPane.setMargin( borderNode4 , new Insets( 10,10,10,10 ) );
        BorderPane.setMargin( borderNode5 , new Insets( 10,10,10,10 ) );
        border.setTop( borderNode1 );
        border.setLeft( borderNode2 );
        border.setCenter( borderNode3 );
        border.setRight( borderNode4 );
        border.setBottom( borderNode5 );
        root.getChildren().add( createNameAndPane( "BorderPane\t" , border ) );

        // GridPaneを追加
        GridPane    grid        = new GridPane();
        Text        gridNode1   = new Text("【その１abc】") ;
        Text        gridNode2   = new Text("【その２】") ;
        Text        gridNode3   = new Text("【その３】") ;
        Text        gridNode4   = new Text("【その４】") ;
        Text        gridNode5   = new Text("【その５】") ;
        grid.add( gridNode1 , 0 , 0 , 1 , 1 );
        grid.add( gridNode2 , 1 , 0 , 1 , 1 );
        grid.add( gridNode3 , 0 , 1 , 2 , 1 );
        grid.add( gridNode4 , 2 , 1 , 1 , 2 );
        grid.add( gridNode5 , 0 , 2 , 2 , 1 );
        root.getChildren().add( createNameAndPane( "GridPane\t\t" , grid ) );

        // StackPaneを追加
        StackPane   stack       = new StackPane();
        Text        stackNode1  = new Text("【その１abc】") ;
        Text        stackNode2  = new Text("【その２】") ;
        Text        stackNode3  = new Text("【その３】") ;
        stack.getChildren().addAll( stackNode1 , stackNode2 , stackNode3  );
        root.getChildren().add( createNameAndPane( "StackPane\t\t" , stack ) );

        // シーンの作成(文字列で指定)
        Scene   scene       = new Scene( root , 300 , 450 , Color.web( "9FCC7F" ) );

        // ウィンドウ表示
        primaryStage.setScene( scene );
        primaryStage.show();

    }

    /**
     * レイアウトと、レイアウトの名前を出力するイメージを作成する
     * @param name
     * @param pane
     * @return
     */
    public Node createNameAndPane( String name , Pane pane )
    {
        // ルートノードを作成
        HBox    root        = new HBox( 5.0 );

        // 名前を追加
        Text    text        = new Text( name );

        // レイアウトの位置をそろえ、
        // 背景色を白、境界線を黒で出力
        Background  back    = new Background( new BackgroundFill( Color.WHITE , null , null ) );
        Border      border  = new Border( new BorderStroke( Color.BLACK ,
                                                            BorderStrokeStyle.SOLID,
                                                            CornerRadii.EMPTY,
                                                            null
                                                            ) );
        pane.setLayoutX( 200 );
        pane.setBackground( back );
        pane.setBorder( border );

        // ノードを追加
        root.getChildren().addAll( text , pane );

        return root;
    }

}
