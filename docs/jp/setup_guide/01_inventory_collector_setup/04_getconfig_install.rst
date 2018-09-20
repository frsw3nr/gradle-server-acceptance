Getconfigインストール
=====================

gradle-server-acceptanceインストール
------------------------------------

GitHub サイトから最新のバイナリモジュールダウンロードします。

::

   https://github.com/frsw3nr/gradle-server-acceptance/releases/download

ダウンロードしたファイルを、c:\\ の直下にコピーします。
エクスプローラから、ダウンロードしたファイルを選択し、右クリックで 
7-zip メニューを開いて「ここに展開」を選択します。

c:\server-acceptance ディレクトリが作成されます。

.. note::

   バイナリモジュール の作成手順については、開発ガイドを参照してください。


実行パス環境変数の設定
----------------------

実行パス環境変数に本ディレクトリを追加します。

コントロールパネルを開いて、「システム」、「システムの詳細設定」を選択します。
「環境変数」をクリックします。

システムの環境変数のリストから、Path を選択して、「編集」をクリックします。
値の最後に ;c:\server-acceptance を追加して、パスを追加します。

設定を反映するため、PowerShell　を一旦閉じて、再度、起動します。
PowerShell コンソールから、 getconfig -h コマンドを実行して、
以下ヘルプメッセージが出力されることを確認します。


::

   getconfig -h
   Getconfig Inventory collector v0.1.29
   usage: getconfig -c ./config/config.groovy
   <中略>
