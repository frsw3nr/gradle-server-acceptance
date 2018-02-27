GroovyFX調査
============

コンポーネント,コントローラ調査
-------------------------------

**リスト項目**

コンボボックス

    choiceBox id: 'group', row: 4, column: 1,items: ["one", "two", "three"]

バインドはxxx().bind xxx.getSelectionModel().selectedItemProperty() で、選択名称が取得できる

    email.groupProperty().bind group.getSelectionModel().selectedItemProperty()

**パスワード**

**リンク**

**コンソールログ**

**パン**

**スプリットパン**

**ボーダーボタン**

**メニュー**

**ポップアップ**

**テーブルビュー,複数のリンク**

コーディング調査
----------------

*クロージャの設定*

*バイディング*

    直接バインド
    @FXBindableアノテーションの宣言
    bidirectional bind?
    モデルに @FXBindable で変数を宣言すると、xxxPropertiy().bind xxx.textProperty() が使える

*GirdPane宣言までの基本部*

    ScreenGraphBuilder sgb
    Stage frame = sgb.primaryStage
    Scene scene = frame.scene
    GridPane grid = scene.root

プロトタイピング
----------------

    ノードリスト
    ノード編集
        プラットフォーム毎入力フィールド
    検査実行

