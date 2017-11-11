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

CSS
---



ポップアップ
---------------

コンソールログ
---------------

