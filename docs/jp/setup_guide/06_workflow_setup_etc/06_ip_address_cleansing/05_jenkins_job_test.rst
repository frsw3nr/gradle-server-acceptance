Jenkins ジョブ動作確認
======================

* Jenkins ダッシュボードに移動します。
* 画面右側のジョブリストから作成した「getconfig」を選択します。
* リポジトリリストから、「ip_address_cleansing」を選択し、次の画面から「master」
  を選択します。
* 実行中のビルドを選択し、「コンソール出力」を選択します。
* 出力メッセージの一番下の「Input requested」を選択し、以下を入力します。

   - 「inventory」に「net1」を選択
   - 「project」に「TokyoDC」を選択
   - 「レポート実行」をクリック

* 実行後、最新のビルドが、グリーンになっていれば OK です。

