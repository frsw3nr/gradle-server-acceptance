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


ボーダーボタン
---------------

メニュー
---------------

ポップアップ
---------------

コンソールログ
---------------

