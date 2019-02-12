ベーステンプレートのカスタマイズ
================================

既定アカウントの接続情報をあらかじめGetconfig ホームディレクトリ
(C:\server-acceptance)下の設定ファイルに追加します。
Getconfig インベントリ収集プロジェクト作成時に、本設定ファイルがコピーされます。

検査シートの接続情報には設定したユーザIDを指定します。
以下プラットフォームの設定ファイルに、既定アカウントを設定。

* Linux
* IAサーバHW(HP iLO, Cisco UCS)
* SPARCサーバ(xscf, Solaris)
* vCenter(VM, ESXi)
* Zabbix 監視設定

Windows はadministrator ユーザでの接続が必要のため、個別にパスワード設定が必要です。
また、既定アカウントがない、接続情報を変更している場合は、個別に設定が必要です。

事前準備作業として、Getconfig ホームディレクトリ(C:\server-acceptance)
下の設定ファイルにアカウント接続情報の追加が必要。

* config/config.groovy
* template ディレクトリ下のconfig設定ファイル

