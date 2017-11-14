GroovyFX調査
============

コントローラ調査
----------------

* コンボボックス
* パスワード
* リンク
* パン、スプリットパン
* ボーダーボタン
* メニュー
* ポップアップ
* コンソールログ

コーディング調査
----------------

* クロージャ(ステージ、シーン、パン)
* バイディング
* バリデーション
* css

プロトタイピング
----------------

* グループ追加
* ノードリスト
* ノード編集
* 検証

調査詳細
========

コンボボックス
--------------

「Combo-box key value pair in JavaFX 2」を参考に試すが、
キャストエラーになるため保留

    java.lang.ClassCastException: GroupTest cannot be cast to java.lang.String

シンプルなリストによるコンボボックス例

コントローラの配置

    label 'Group', row: 5, column: 0
    choiceBox id: 'group', row: 5, column: 1, items: ["System01", "System02", "System03"]

バインド設定

    node.groupProperty().bind group.getSelectionModel().selectedItemProperty()

パスワード
----------

コントローラ配置

    label 'SpecificPassword', row: 4, column: 0
    passwordField id: 'specific_password', row: 4, column: 1

バインド。カラム名だと'_'が入ってメソッド見づらい。

    node.specific_passwordProperty().bind specific_password.textProperty()

モデル定義でキャメル表記に変えて宣言する。

class NodeTest {
    @FXBindable String node_name, alias_name, ip, group, specificPassword
    ↓
    @FXBindable String nodeName, aliasName, ip, group, specificPassword

リンク
------

hyperlink
javafx.scene.control.Hyperlink
registerFactory 'hyperlink', new LabeledFactory(Hyperlink)

onAction
Hyperlink link = new Hyperlink();
link.setText("http://example.com");
link.setOnAction(new EventHandler<ActionEvent>() {
    @Override
    public void handle(ActionEvent e) {
        System.out.println("This link is clicked");
    }
});

コントローラの配置。 onAction でクリック時の動作記述

label 'Platform', row: index, column: 0
hyperlink 'Linux', id: 'platform', row: index, column: 1,
  onAction: { println "Link 'Linux'" }

パン、スプリットパン
--------------------

2画面テスト

    stage(title: "GroovyfX, SplitPane Demo", width: 800, height: 400, visible: true) {
        scene(fill: GROOVYBLUE) {
            splitPane(orientation: HORIZONTAL) {
                anchorPane {
                    button("ONE", leftAnchor: 10)
                    button("TWO", rightAnchor: 10, topAnchor: 10)
                }
                anchorPane {
                    label("Label 1") // left or top
                    label("Label 2") // right or bottom
                    label("Label 3")
                    label("Label 4")
                }
            }
        }
    }

def layoutFrame(SceneGraphBuilder sgb) {
    sgb.anchorPane {
        button("ONE", leftAnchor: 10)
        button("TWO", rightAnchor: 10, topAnchor: 10)
    }
}

     <SplitPane dividerPositions="0.4126984126984127" layoutX="153.0" layoutY="70.0" prefHeight="300.0" prefWidth="600.0" AnchorPane.bottomAnchor="0.0" AnchorPane.leftAnchor="0.0" AnchorPane.rightAnchor="0.0" AnchorPane.topAnchor="0.0">
 
リスト、編集画面との結合

org.codehaus.groovy.runtime.typehandling.GroovyCastException: 
Cannot cast object 'SplitPane@1d3b8ab1[styleClass=root split-pane]' with class 'javafx.scene.control.SplitPane' to class 'javafx.scene.layout.GridPane'

SceneGraphBuilder を delegate するとよい。

start {
    stage(title: "GroovyfX, SplitPane Demo", width: 800, height: 400, visible: true) {
        scene(fill: GROOVYBLUE) {
            def left_pane = delegate
            layoutFrame(left_pane)
        }
    }
}

def layoutFrame(SceneGraphBuilder sgb) {
    sgb.anchorPane(width: 800, height: 400) {
        splitPane(orientation: HORIZONTAL, anchor: [5, 5, 5, 5]) {

gradle SplitPane

    stage(title: "GroovyfX, SplitPane Demo", width: 800, height: 400, visible: true) {
        scene(fill: GROOVYBLUE) {
            def left_pane = delegate
            layoutFrame(left_pane)
        }
    }

AnchorPane から GridPane のキャストができない
org.codehaus.groovy.runtime.typehandling.GroovyCastException: 
Cannot cast object 'AnchorPane@3b390684[styleClass=root]' 
Cannot cast object with class 'javafx.scene.layout.AnchorPane' to class 'javafx.scene.layout.GridPane'

println '[methods]'
obj.metaClass.methods.each { method ->
    println method
}

    stage title: "GroovyFX Logo", x: 10, y: 10, visible: true, {
        scene(fill: GROOVYBLUE, width: 300, height: 300) {
            stackPane {
                rectangle x: 0, y: 0, width: 120, height: 120, opacity: 0d
                borderPane id: 'parent', {
                    group id: 'logo', {
                        transitions = parallelTransition()
                        star delegate, 12, [LIGHTGREEN, GREEN]*.brighter()
                        star delegate, 6, [LIGHTBLUE, BLUE]*.brighter()
                        star delegate, 0, [YELLOW, ORANGE]
                        fxLabel delegate
                        onMouseClicked { transitions.playFromStart() }
                    }
                }
            }
        }

            borderPane(padding: 10) {
                text = textArea(prefRowCount: 10, prefColumnCount: 80)
                bottom(align: "center", margin: [10, 0]) {
                    button("Print Text", onAction: { println text.text })
                }
            }

    }

中間まとめ

builder.group() { } にすると、キャストエラーは減る。group()の引数が不明
リサイズ時の自動調整が利かない。以下で強制的にサイズ指定してる
	splitPane(orientation: HORIZONTAL, prefWidth:800, prefHeight:350)
	もう一度 JavaFX チュートリアルの CSS の記事を再読した方が良いかも


Anchor Pane "Fit to Parent"

AddressApp調査
==============

モデル
------

Person
    private final StringProperty firstName;
    private final StringProperty lastName;
    private final StringProperty street;
    private final IntegerProperty postalCode;
    private final StringProperty city;
    private final ObjectProperty<LocalDate> birthday;

    public Person(String firstName, String lastName)

PersonListWrapper
    private List<Person> persons;
    public List<Person> getPersons()
    public void setPersons(List<Person> persons)

ビュー
------

BirthdayStatistics.fxml
PersonEditDialog.fxml
PersonOverview.fxml
RootLayout.fxml
DarkTheme.css

BirthdayStatisticsController.java
    private BarChart<String, Integer> barChart;
    private ObservableList<String> monthNames = FXCollections.observableArrayList();
    public void setPersonData(List<Person> persons)

PersonEditDialogController.java
Person編集画面
    @FXML    private TextField firstNameField;
    @FXML    private TextField lastNameField;
    @FXML    private TextField streetField;
    @FXML    private TextField postalCodeField;
    @FXML    private TextField cityField;
    @FXML    private TextField birthdayField;

    public boolean isOkClicked() {
    public void setPerson(Person person)
        モデルセット
    private void handleOk() {
        モデル更新
    private void handleCancel()
        dialogStage.close();
    private boolean isInputValid() {
        バリデーション

PersonOverviewController.java
    @FXML    private TableView<Person> personTable;
    @FXML    private TableColumn<Person, String> firstNameColumn;
    @FXML    private TableColumn<Person, String> lastNameColumn;

    @FXML    private Label firstNameLabel;
    @FXML    private Label lastNameLabel;
    @FXML    private Label streetLabel;
    @FXML    private Label postalCodeLabel;
    @FXML    private Label cityLabel;
    @FXML    private Label birthdayLabel;

     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
    private void initialize()
     * Fills all text fields to show details about the person.
     * If the specified person is null, all text fields are cleared.
    private void showPersonDetails(Person person)
     * Called when the user clicks on the delete button.
    private void handleDeletePerson()    
     * Called when the user clicks the new button. Opens a dialog to edit
     * details for a new person.
    private void handleNewPerson()
     * Called when the user clicks the edit button. Opens a dialog to edit
     * details for the selected person.
    private void handleEditPerson()
        Person selectedPerson = personTable.getSelectionModel().getSelectedItem();
        if (selectedPerson != null) {
            boolean okClicked = mainApp.showPersonEditDialog(selectedPerson);
            if (okClicked) {
                showPersonDetails(selectedPerson);
            }
     * Called when the user clicks the edit button. Opens a dialog to edit
     * details for the selected person.
    private void handleEditPerson()

RootLayoutController.java

@ThreadInterrupt

Gaelyk

=======
tableView

class Node {
    @FXBindable String nodeName, aliasName, ip, group, specificPassword

    String toString() {
        "name: $nodeName, alias: $aliasName,  group: $group, ip:$ip, pass:$specificPassword"
    }
}

def nodes = [
    new Node(nodeName: "ostrich", ip: '192.168.10.1', group: 'System01'),
    new Node(nodeName: "win2012", ip: '192.168.10.2', group: 'System01'),
    new Node(nodeName: "centos6", ip: '192.168.10.3', group: 'System01')
]

:ImprovedDemogroovy.lang.MissingPropertyException: No such property: nodes for class: ImprovedDemo
        at org.codehaus.groovy.runtime.ScriptBytecodeAdapter.unwrap(ScriptBytecodeAdapter.java:53)
        at org.codehaus.groovy.runtime.callsite.PogoGetPropertySite.getProperty(PogoGetPropertySite.java:52)
        at org.codehaus.groovy.runtime.callsite.AbstractCallSite.callGroovyObjectGetProperty(AbstractCallSite.java:307)
        at ImprovedDemo$_mainFrame_closure2$_closure11$_closure12.doCall(ImprovedDemo.groovy:90)

ボーダーボタン
---------------

New Edit Copy 
=======

テーブルのプロパティは以下の通り定義する

def nodes = FXCollections.observableList([
    new Node(nodeName: "ostrich", ip: '192.168.10.1', platforms: 'RedHat6',    group: 'System01'),
    new Node(nodeName: "win2012", ip: '192.168.10.2', platforms: 'Windows',    group: 'System01'),
    new Node(nodeName: "centos6", ip: '192.168.10.3', platforms: 'RedHat6,vCenter', group: 'System01')
])

アクション

button("New", onAction: {nodes << new Node(nodeName: "node${nodes.size()}")})


メニュー
---------------

イベント通信
------------

セルを選択したら、入力フィールドのパラメータ設定

tableView   javafx.scene.control.TableView(args, body)
tableColumn javafx.scene.control.TableColumn(args)
tableRow    javafx.scene.control.TableRow(args)

[shortcutDown:false, stillSincePress:true, 
target:Text
[text="win2012b", x=0.0, y=0.0, alignment=LEFT, origin=BASELINE, boundsType=LOGICAL_VERTICAL_CENTER, font=Font[name=System Regular, family=System, style=Regular, size=12.0], fontSmoothingType=LCD, fill=0xffffffff], middleButtonDown:false, popupTrigger:false, sceneX:92.0, clickCount:1, button:PRIMARY, controlDown:false, synthesized:false, metaDown:false, z:0.0, primaryButtonDown:false, screenX:337.0, shiftDown:false, dragDetect:true, class:class javafx.scene.input.MouseEvent, pickResult:PickResult [node = Text[text="win2012b", x=0.0, y=0.0, alignment=LEFT, origin=BASELINE, boundsType=LOGICAL_VERTICAL_CENTER, font=Font[name=System Regular, family=System, style=Regular, size=12.0], fontSmoothingType=LCD, fill=0xffffffff], point = Point3D [x = 45.0, y = 1.0, z = 0.0], distance = 673.6351707661823, sceneY:117.0, consumed:false, altDown:false, y:66.0, source:TableView@15db5574[styleClass=table-view], secondaryButtonDown:false, eventType:MOUSE_CLICKED, x:49.0, screenY:594.0]
メソッド
[clone, consume, copyFor, copyForMouseDragEvent, equals, fireEvent, getButton, getClass, getClickCount, getEventType, getPickResult, getSceneX, getSceneY, getScreenX, getScreenY, getSource, getTarget, getX, getY, getZ, hashCode, isAltDown, isConsumed, isControlDown, isDragDetect, isMetaDown, isMiddleButtonDown, isPopupTrigger, isPrimaryButtonDown, isSecondaryButtonDown, isShiftDown, isShortcutDown, isStillSincePress, isSynthesized, notify, notifyAll, setDragDetect, toString, wait]

PersonOverviewController.java

@FXML
private void initialize() {
    // Initialize the person table with the two columns.
    firstNameColumn.setCellValueFactory(
            cellData -> cellData.getValue().firstNameProperty());
    lastNameColumn.setCellValueFactory(
            cellData -> cellData.getValue().lastNameProperty());

    // Clear person details.
    showPersonDetails(null);

    // Listen for selection changes and show the person details when changed.
    personTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> showPersonDetails(newValue));
}

TableFactory.groovy

onMouseEntered { e -> e.source.parent.gridLinesVisible = true }

getSelectionModel

    boolean onHandleNodeAttributes(FactoryBuilderSupport builder, Object node, Map attributes) {
        if(node instanceof TableView) {

onSelectionChanged
onSelect
onAction

[clone, consume, copyFor, copyForMouseDragEvent, equals, fireEvent, 
getButton, getClass, getClickCount, getEventType, getPickResult, 
getSceneX, getSceneY, getScreenX, getScreenY, getSource, getTarget, 
getX, getY, getZ, hashCode, isAltDown, isConsumed, isControlDown, isDragDetect, isMetaDown, isMiddleButtonDown, isPopupTrigger, isPrimaryButtonDown, isSecondaryButtonDown, isShiftDown, isShortcutDown, isStillSincePress, isSynthesized, notify, notifyAll, setDragDetect, toString, wait]

https://github.com/dmpe/JavaFX.git

groovyfx

ActionMenuDemo.groovy

レイアウト
menuItem(openAction) {rectangle(width: 16, height: 16, fill: RED)}
アクション
bean({ID}, プロパティ名： bind({ID}.selectedPrpperty()) で制御している
center {
    vbox(spacing: 20, padding: 10) {
        checkBox("Enable 'Open' menu", id: 'cb')
        actions {
            bean(openAction, enabled: bind(cb.selectedProperty()))
        }
    }
}

AnalogClockDemo.groovy

バインド。度数計算して、xxxAnglePropertyにバインドしている
// bind the angle properties to the clock time
hourAngleProperty.bind((hours() * 30.0) + (minutes() * 0.5))
minuteAngleProperty.bind(minutes() * 6.0)
secondAngleProperty.bind(seconds() * 6.0)

BindDemo.groovy

bindで色々くっつける。処理の追加は不要

textField(id: "tf2", promptText: 'Change Me!')
textField(text: bind(tf2.textProperty()))   // tf2 のフィールドの変更をバインド

bindの書き方色々
    label(text: bind(tf.textProperty()))  // これが一番しっくりくる
    label(text: bind({tf.text}))
    label(text: bind(tf.text()))

FXBindable 宣言

class QuickTest {
    @FXBindable String qtText = "Quick Test"    // String形でバインドする
    def onClick = {
        qtText = "Quick Test ${++clickCount}"
    }

ChartDemo.groovy

FXCollections.observableArrayList で配列宣言

final pieData = FXCollections.observableArrayList([new PieChart.Data("Yours", 42), new PieChart.Data("Mine", 58)])

バインド値はコントローラのdata型と合わせる必要がある

stackPane(alignment: TOP_RIGHT) {
    pieChart(data: pieData, animated: true)
    button('Add Slice') {
        onAction {
            pieData.add(new PieChart.Data('Other', 25))
        }
    }
}

ChoiceBoxDemo.groovy

choiceBoxコントローラのバインド。リスト形式でマップはない？

ChooserDemo.groovy

onAction でダイアログ開いて、
button("Open file", onAction: {
    selectedfile = fileChooser.showOpenDialog(primaryStage)
    selectedProperty.set(selectedfile ? selectedfile.toString() : "")

結果のバインド。id でラベル定義して、selectedProperty にバインドしている
    label(id: 'selected')
    selected.textProperty().bind(selectedProperty)

CustomFieldDemo.groovy

class RejectField extends TextField

    Closure onReject

バリデーションの仕方。クロージャの使い方が難しい。モジュール化しずらい？

DerksCodeStyleDemo.groovy

accordion、titledPane コンポーネントを使った例
シンプルな指定でレイアウトしやすい。アクションやプロパティが不明。要再調査

FXBindableDemo.groovy

@FXBindable
class FXPerson {
    String firstName;
    String lastName;
    int age;
    ObservableList likes = []; 
    ObservableMap attributes = [:];
    ObservableSet aSet = [] as Set;
    
}

リストを登録する場合
person.aSet = [0] as ObservableSet
person.aSet << 1
person.aSet << 2

マップを登録する場合
person.attributes = ['one':'two'] as ObservableMap
person.attributes.put('foo', 'bar');

FXMLDemo.groovy 
動作しない。プロローグにはコンテンツを指定できません。

GroovyFxPad
-----------

C:\usr\opt\Griffon\griffon-1.0.0\samples\GroovyFxPad>set java
JAVAFX_HOME=c:\usr\opt\JavaFX\javaFXSDK2.1
JAVA_HOME=C:\usr\opt\java\jdk1.7.0_04
GroovyFxPadの実行

[C:\usr\opt\Griffon\griffon-1.0.0\samples\GroovyFxPad]griffon run-app
Welcome to Griffon 1.0.0 - http://griffon-framework.org/
Licensed under Apache Standard License 2.0
Griffon home is set to: C:\usr\opt\Griffon\griffon-1.0.0

git clone -b version8 https://github.com/groovyfx-project/groovyfx.git

ObservableList<E> observableList = FXCollections.observableList(list);

SwingDemo.groovy

:SwingDemoException in thread "main" java.lang.ExceptionInInitializerError
        at javafx.scene.web.WebEngine.<clinit>(WebEngine.java:315)

def sg = new SceneGraphBuilder();

WebEngineの互換性エラー？

TabPaneDemo.groovy

ap.getChildren().remove(btn);
No signature of method: javafx.scene.layout.VBox.group()

Table2Demo.groovy

people = FXCollections.observableList([])
tableView(items: people, selectionMode: "single", cellSelectionEnabled: true, editable: true, row: 2, column: 0) {
  tableColumn(editable: true, property: "name", text: "Name", prefWidth: 150,
    onEditCommit: { event ->
      Person item = event.tableView.items.get(event.tablePosition.row)
      item.name = event.newValue
    }
  )

:SpirographDemojava.lang.ClassCastException: 
java.util.ArrayList cannot be cast to javafx.collections.ObservableList
person.likes = ["GroovyFX"] as ObservableList

TextAreaDemo.groovy

text = textArea() で部品を作成して、 onAction: { println text.text} で入力値を出力

TextFlowDemo.groovy

テキストの回り込みの例

TimelineDemo.groovy

タイムラインの記述。難しい

    timeline(cycleCount: 1, autoReverse: true) {
        onFinished { x = 200; y = 200; println "F: ${this.x}, ${this.y}" }
        at(10.s) {
            change(this, "x") to 400.0 tween EASE_BOTH
            change(this, "y") to 400
            onFinished { println "10 seconds elapsed"}
        }
    }.play()

TitledPaneDemo.groovy

アコーディオンと同様。レイアウトや動作が変わる。titledPaneの方が雑多な感じがある。

ToggleButtonDemo.groovy

グループ内でボタンのONを排他。既定のボタンだと陰影が分かり難い。
selectBoxの方が見やすい

ToolBarDemo.groovy

見やすい。ボーダー線を引いてくれる

TransitionDemo.groovy

変換。アニメーション

TreeViewDemo.groovy

開閉イベントはonBranchXXXでハンドルする

treeItem(value: "one") {
    onBranchCollapse { popup.show(primaryStage, 150, 150) }
    onBranchExpand {println "one expand"}
    treeItem(value: "one.one")
    treeItem(value: "one.two")
    graphic {
        rectangle(width: 20, height: 20, fill: RED)
    }
}
ポップアップは以下で、popup.show(primaryStage, 150, 150)とすれば良い

    popup = popup(autoHide: true) {
        stackPane() {
            rectangle(width: 200, height: 200, fill: LIGHTGRAY)
            button("Dismiss", layoutX: 10, layoutY: 20, onAction: {popup.hide()})
            effect dropShadow()
        }
    }

TriggerDemo.groovy

ホバーイベントに変更があったら・・・するは以下の様に書く。

    rect.hover().onInvalidate { rect.fill = rect.isHover() ? Color.GREEN : Color.RED }

TwoStagesDemo.groovy

primary:false とすれば複数画面表示できる

    stage(primary: false, x: 100, y: 400, visible: true)

TableView1.groovy

以下リファレンスでtablePositionを理解

http://d.hatena.ne.jp/eerga/touch/searchdiary?word=%2A%5BJavaFX%5D

properties:
[anchor:TablePosition 
    [ row: 1, 
      column: javafx.scene.control.TableColumn@4a9a4436, 
      tableView: TableView@5377cd66[
        styleClass=root table-view
      ]
    ]
]

    public static String getSelection(TableView<?> table) {
        StringBuilder clipboardString = new StringBuilder();
        ObservableList<TablePosition> positionList = 
            table.getSelectionModel().getSelectedCells();
        int prevRow = -1;
        for (TablePosition position : positionList) {



ポップアップ
---------------

コンソールログ
---------------

