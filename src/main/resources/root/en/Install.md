
事前準備
--------------

**zipファイル解凍**


**社内ルート証明書インストール**

以下、URLから東芝ルート証明書をダウンロード

インストールしたJDK8環境に証明書をインポート

    set PATH=C:\Program Files\Java\jre1.8.0_101\bin;%PATH%
    keytool -list --keystore "C:\Program Files\Java\jre1.8.0_101\lib\security\cacerts"
    keytool -importcert -v -trustcacerts -file .\cacert.cer -keystore "C:\Program Files\Java\jre1.8.0_101\lib\security\cacerts"

解凍したディレクトリで gradlew.bat 実行

