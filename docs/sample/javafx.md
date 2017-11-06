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

ボーダーボタン
---------------

メニュー
---------------

ポップアップ
---------------

コンソールログ
---------------

