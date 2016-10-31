開発者ガイド
============

Groovy 言語で検査コードの記述することで検査項目のカスタマイズが可能です。
ここでは以下2パターンの検査シナリオのコーディング手順を説明します。

1. SSH経由の検査シナリオ

    検査対象サーバに SSH接続し、実行ログをダウンロードして、ダウンロードした結果を検証します。
    Linux の検査で使用します。

2. PowerShell を使用した検査シナリオ

    Get-WmiObject、Get-VM コマンド等、PowerShell コマンドを用いて検査対象サーバの情報を採取し、
    採取結果を検証します。
    Windows, vCenter の検査で使用します。

ファイル構成
============

アーカイブファイル解凍後の構成で、主にカスタマイズで使用するファイルは以下となります。

```
C:.
├チェックシート.xlsx                  # 1.チェックシート.xlsx
├─config                             # 2.設定ファイル
│  └─config.groovy
└─lib
    └─InfraTestSpec                  # 3.Groovy 検査スクリプト
        ├─LinuxSpec.groovy
        ├─vCenterSpec.groovy
        └─WindowsSpec.groovy
```

1. チェックシート.xlsx

    検査項目、検査対象サーバ、検査ルールの定義シートとなります。
    各項目の設定方法は[使用方法](Usage.md)を参照してください。
    ここではカスタマイズで使用する項目を説明します。

    *シート : "チェック対象VM"*

    12行目以降の空欄の行は独自のサーバ情報を追加するフィールドとなり、検査スクリプト内で**server_info**メンバー変数から値を参照します。

    * 新規に項目ID 'java\_version' の行を追加した場合、検査コード内で server_info['java\_version'] とすることで値を参照できます。
    * 既存の項目の参照も可能です。server_info['ip'] とすることで、 項目ID 'ip' のIPアドレスの値を参照できます。

    *シート : "検査ルール"*

    検査コード実行後の値の評価ルールを記述します。
    評価は、テンプレートファイル **lib/template/VerifyRule.template**
    をテンプレートとして、Groovy の評価スクリプトを生成し、
    生成したスクリプトを用いて評価をします。
    評価スクリプトの変換ルールは、変数 x を入力パラメータとして、以下形式で記入します。

    * 不等式の評価

        **"値 == x"、"x < 値"、"値 <= x && x < 値"** などの不等号を条件式に記入します

    * 正規表現の評価

        **"x =~ /正規表現/"** で条件式を記入します

2. config/config.groovy

    新たな検査シートを追加する場合、**evidence.sheet_name_spec** パラメータに、
    シート'チェック対象VM'の、platform をキーにしたシート名を追加してください。
    既定の検査シート定義は以下の通りです。

    ```
    evidence.sheet_name_spec = [
        'Linux':   'チェックシート(Linux)',
        'Windows': 'チェックシート(Windows)',
    ]
    ```

3. Groovy検査スクリプト

    検査コードの本体となり、各検査シナリオの初期化、検査手順、各検査項目のコードを記述します。
    コーディングの詳細手順は次節で説明します。
    各検査コードは**本体のソースコード**内の以下の Groovy スクリプトを継承しています。

    **注意事項 : **

    以下コードはアーカイブファイル内のJarファイルに埋め込んでいるため、ソースコードを
    参照する場合は GitHub リポジトリから参照してください。

    *ROOTクラスコード*

    main/src/main/groovy/jp/cp/toshiba/ITInfra/acceptance/**InfraTestSpec.groovy**

    全ての検査スクリプトが継承する基底の検査スクリプトとなり、各シナリオの検査コードを除く、共通で使用するメンバー変数、メソッドを記述しています。

    *ドメインベースクラスコード*

    main/src/main/groovy/jp/cp/toshiba/ITInfra/acceptance/InfraTestSpec/**{Domain}SpecBase.groovy**

    Linux,Windows,vCenter 検査シナリオの**ベースとなる検査メソッド**のコードを記述しています。
    lib/InfraTestSpec/下のカスタマイズ用検査スクリプトは同様の検査コードを記述していますが、
    コメントアウトしています。

SSH検査シナリオのカスタマイズ
=============================

Linux検査シナリオで使用するスクリプトとなり、**lib/InfraTestSpec/LinuxSpec.groovy**
 を編集します。
検査IDとメソッドが1対1で紐づいており、メソッドは
**検査ID(session, test_item)** の形式で記述します。
実際のメソッド定義を以下に記します。
コメントアウトされているメソッドはベースの検査コードで、前述のドメインベースクラスで
コーディングされた検査メソッドとなります。

```
def vncserver(session, test_item) { ... }
def packages(session, test_item) { ... }
def oracle_module(session, test_item) { ... }
// def hostname(session, test_item) { ... }
// def hostname_fqdn(session, test_item) { ... }
// def uname(session, test_item) { ... }
...
```

これらメソッドをコーディングする事で、検査項目をカスタマイズします。

* 第1引数の **session** は検査対象サーバのsshセッションで、[Groovy-SSH API](https://gradle-ssh-plugin.github.io/docs/)で使用します。
* 第2引数の **test_item** は検査結果オブジェクトで検査項目の参照や登録で使用します。
  これらAPIの詳細は、後述のAPIリファレンスで説明します。

SSH検査処理フロー
-----------------

SSH検査スクリプト LinuxSpec.groovy の処理フローは以下となります。

1. チェックシートを読込みます。
2. 初期化処理( **init()** メソッド)

    config.groovy から検査対象サーバの接続情報(ip, user, passwordなど)を取得します。

3. 検査実行( **setup_exec()** メソッド)

    sshセッション接続をして、検査メソッドを順に実行します。
    検査メソッドは、**"検査ID()"の名前のメソッド**で、
    検査対象サーバに対して採取コマンドの実行、採取ファイルの解析、結果の登録をします。
    本メソッドがカスタマイズするコードとなります。

4. Excelシートに結果を更新します。

**注意事項:** これら処理は全て、親クラスの
main/src/main/groovy/jp/cp/toshiba/ITInfra/acceptance/LinuxInfraTestSpec.groovy
スクリプトから継承されているため、実際のコードは親クラスのスクリプトに記述されています。

SSH検査コードの記述方法
-----------------------

検査コードの基本構成をLinuxの例を用いて説明します。

```
    def hostname(session, test_item) {
        def lines = exec('hostname') {
            run_ssh_command(session, 'hostname -s', 'hostname')
        }
        lines = lines.replaceAll(/(\r|\n)/, "")
        test_item.results(lines)
    }
```

* 1行目の **def hostname(session, test_item)** は、検査ID が **hostname** の検査メソッドの定義となります。
* **exec(検査ID)** から始まるコードは、SSH経由でリモート実行するコードを記載し、
  コードブロック内で第1引数の session オブジェクトを用いて、
  [Groovy SSH API]((https://gradle-ssh-plugin.github.io/docs/)) をコールします。
* exec コードはDryRun のモードにより動作が変わり、
  **dry_run が true の場合**、-r オプションで指定したテストログディレクトリから
  **ログファイルを読み込み、コードブロック内処理は実行しません**。
* dry_run が false の場合、コードブロック内の処理を実行します。
* exec コードブロック内の **run_ssh_command(session, 'コマンド', '検査ID')** は
  ヘルパーメソッドで以下を実行します。
    * 第2引数で指定したコマンドを実行
    * 実行結果を'検査ID'というファイル名でワークディレクトリに保存
    * 保存したログファイルをダウンロード
    * ダウンロードしたログファイルをバッファに読み込み戻り値に返す
* **test_item.results(lines)** は exec(検査ID) の結果を検査結果としてセットします。
* 本検査メソッド実行後、実行結果を以下の通り更新します。
    * ログファイル **build/log.{日時}/{プラットフォーム}/{サーバ}/{ドメイン}/{検査ID}** に採取結果を保存
    * Excel 検査シートの**{検査ID}:{サーバ列}**のセルに検査結果を更新

検査コードの基本構成は以上となりますが、他にも親クラスから継承したメンバー変数、メソッドが多数あります。これらAPIの利用方法は後述のAPIリファレンスで記します。

PowerShell検査シナリオのカスタマイズ
====================================

Windowsや、vCenter の検査シナリオとなり、PowerShellでリモートコマンド
を実行して実行結果を解析します。

PowerShell検査処理フロー
------------------------

PowerShell検査の処理フローはLinuxに比べて少し複雑です。
LinuxのSSH処理と比較して変更点をコメントします。
初めのExcelチェックシートの読込や、検査実行後の結果の更新は同じで、
以下の2-3の箇所が異なります。

1. チェックシートを読込みます。
2. 初期化処理( **init()** メソッド)

    Linux と同様に config.groovy から検査対象サーバの接続情報(ip, user, passwordなど)を取得します。
    **Windows の場合、os\_account** パラメータから、 **vCenter の場合、
    remote\_account** パラメータから情報取得します。

3. 検査実行( **setup_exec()** メソッド)

    Linux と同様にチェックシートの検査IDリストの順に検査メソッドを実行します。
    Linux との違いとして、検査メソッドは以下の前処理の **PowerShell スクリプトの組み立て**と、
    後処理の **PowerShell実行結果の解析** の2つに分かれます。

    * PowerShellスクリプトの組み立て

        run_script(command) メソッド引数の command を PowerShellスクリプトに追加して
        採取コマンドをまとめたバッチスクリプトを作成します。
        全ての採取コマンドの追加をした後に、PowerShell バッチスクリプトを実行します。

    * PowerShell実行結果の解析

        PowerShellスクリプトの実行で生成されたログファイルを参照して実行結果を解析します。
        その後の処理はLinuxと同様です。

4. Excelシートに結果を更新します。

PowerShell検査コードの記述方法
------------------------------

検査コードの基本構成をWindowsの例を用いて説明します。

```
    def cpu(TestItem test_item) {
        def command = '''\
            |Get-WmiObject -Credential $cred -ComputerName $ip Win32_Processor
            |'''.stripMargin()

        run_script(command) {
            def lines = exec('cpu') {
                new File("${local_dir}/cpu")
            }

            def cpuinfo    = [:].withDefault{0}
            def cpu_number = 0
            lines.eachLine {
                (it =~ /DeviceID\s+:\s(.+)/).each {m0, m1->
                    cpu_number += 1
                }
                (it =~ /Name\s+:\s(.+)/).each {m0, m1->
                    cpuinfo["model_name"] = m1
                }
                (it =~ /MaxClockSpeed\s+:\s(.+)/).each {m0, m1->
                    cpuinfo["mhz"] = m1
                }
            }
            cpuinfo["cpu_total"] = cpu_number
            test_item.results(cpuinfo)
        }
    }
```

* 1行目の **def cpu(test_item)** は、検査ID が **cpu** の検査メソッドの定義となります。
* 2行目の command 変数のセットが PowerShell スクリプトに追加するコマンド定義となります。
* セット値内テキストの **$cred, $ip** は予約語で親クラスから継承した、検査対象サーバの
  プロパティ変数となります。
* **run_script(command)** が、PowerShell スクリプトを実行するコードとなり、引数に指定した
  command 値を PowerShell スクリプトに登録します。
* PowerShell スクリプトは command を実行し、実行結果を **"${local_dir}/{検査ID}"**
  のパスを指定してログに保存します。
* run_script(command) コードブロック内処理がPowerShell が実行後のログ解析処理となります。
* 基本の処理構造は Linux の検査スクリプトと同じですが、注意点として、 exec('cpu') の後のコードブロックは **new File("${local_dir}/{検査ID}")**
  として、ログの読み込み処理のみ記述します。


APIリファレンス
===============

メンバー変数
------------

検査コードから以下のメンバー変数のアクセスが可能です。

**基底クラスからの継承**

* server_name : String 型

    検査対象のサーバ名。Excelの検査対象 VM から検索。

* platform : String 型

    検査対象のOSプラットフォームで、Linux または、Windows を選択。
    Excelの検査対象 VM から検索。

* domain : String 型

    検査シナリオのスクリプトIDで、"${domain}Spec.groovy" が検査スクリプトとなる。
    標準の検査スクリプトは、LinuxSpec.groovy , WindowsSpec.groovy, vCenterSpec.groovy
    の3種類。
    Excelの各検査シートの"分類" から検索。

* title : String 型

    検査対象のタイトル名。
    "${ドメイン名} (${サーバ名} - ${IP})"の形式で検査シナリオのタイトルをセット。

* timeout : int 型

    検査コマンドのタイムアウト秒。
    Linux のSSH検査シナリオの場合はコマンドのタイムアウト。
    Windows, vCenter の PowerShell検査シナリオの場合は、バッチスクリプトのタイムアウト。

* dry_run : Boolean 型

    DryRun(予行演習)モードが有効か。

* dry\_run\_staging_dir : String 型

    DryRunモードのログファイル保存先。

* String local_dir

    採取結果ログの保存先。検査PCのローカルディスクの保存先。
    "./build/log.{日時}/{プラットフォーム名}/{サーバ名}/{ドメイン名}" をセット。

**ドメインクラスからの継承**

以下メンバ変数はLinux,Windows,vCenterの各検査スクリプトで使用可能です。

* ip : String 型

    検査対象サーバのSSH接続先 IP アドレス。
    Excel の検査対象 VM から検索。
    Linux, Windows サーバのダイレクトに情報採取をする場合に使用。
    vCenter のリモート経由で採取をする場合は使用不可。

* os_user : String 型

    検査対象サーバの SSH 接続ユーザ名。
    Linux は config.groovy 内の account.Linux.{id}.user パラメータから検索。
    Windows は config.groovy 内の account.Windows.{id}.user パラメータから検索。
    vCenter の場合は使用不可。

* os_password : String 型

    検査対象サーバの SSH パスワード。
    Linux は config.groovy 内の account.Linux.{id}.password パラメータから検索。
    Windows は config.groovy 内の account.Windows.{id}.password パラメータから検索。
    vCenter の場合は使用不可。

* work_dir : String 型

    検査対象サーバの採取コマンド結果一時保存先。
    Linux のみで使用します。
    ディレクトリは検査スクリプト初期化処理で作成され、スクリプト終了時に削除します。
    config.groovy 内の account.Linux.{id}.work_dir パラメータから検索。

* script_path : String 型

    検査対象サーバの採取スクリプトパス名。
    PowerShell 検査スクリプトで使用します。
    Windows の場合は、 "${local_dir}/get\_Windows\_spec.ps1"、
    vCenter の場合は、 "${local_dir}/get\_vCenter\_spec.ps1" となります。

* vcenter\_ip 、vcenter\_user 、vcenter\_password 、vm : String 型

    vCenter 検査シナリオのみで使用します。
    vcenter\_ip 、vcenter\_user 、vcenter\_password　は、vCenter の SSH 接続情報で、
    config.groovy 内の account.Remote.{id} パラメータから検索します。
    vm は vCenter 内定義の検査対象サーバ名エイリアスで、
    Excel の検査対象 VMから検索します。

メソッド・クロージャ―
----------------------

**親クラスから継承されるメソッド**

以下は検査スクリプトで親クラスから継承されるメソッドです。

* init()

    検査シナリオの初期化処理。
    検査対象サーバの検査スクリプト起動時に最初に実行するメソッドで、
    サーバ接続情報等の各種メンバー変数の初期化で使用します。

* setup\_exec(TestItem[] test_items)

    検査シナリオのメイン処理。
    引数に指定された、test_items 検査項目を順に実行します。
    処理は Linux の SSH検査と Windows,vCenter の PowerShell 検査の2パターンに分かれます。
    原則は、親クラスのドメインベーススクリプトのコードをそのまま利用し、
    本メソッドをカスタマイズするこは有りません。

* cleanup\_exec()

    検査シナリオの終了処理。
    検査対象サーバの検査スクリプトの終了時に実行するメソッドで、
    ワークディレクトリの削除などで使用します。

* finish()

    検査シナリオの終了処理。
    cleanup\_exec()との違いは、cleanup\_exec()は各検査対象サーバの度に実行しますが、
    finish()は最後の検査対象サーバ終了時の1度だけ実行します。

**検査メソッド内で使用するメソッド・クロージャ**

以下は検査メソッド内で使用するメソッド・クロージャとなります。
検査スクリプト内のコメントアウトされたベースの検査メソッド内のコードが、
使用例として参考になります。

* exec(String test_id) { コードブロック }

    引数の test_id は検査IDとなり、検査メソッド名と同一にする必要が有ります。
    コードブロック内に検査対象サーバの情報採取コードを記述します。
    DryRunモードのためのクロージャでDryRun のモードにより動作が以下の通り変わります。

    * dry_run==true の場合

        コードブロック内コードは実行せずに、
        テストリソース下のログファイル'log/{platform}/{server_name}/{domain}/{test_id}'を
        読み込み、戻り値に読み込んだ結果を返します。

    * dry_run==false の場合

        コードブロック内の検査コード実行し、戻り値に採取結果を返します。

* run\_ssh\_command(Session session, String command, String test_id)

    Linux SSH コマンド実行で使用します。以下の処理を行います。

    * 検査対象側

        command で指定した SSH コマンドを実行し、test_id の検査項目ログに実行結果を保存。

    * 検査用PC側

        実行結果ログをダウンロードして、ログを読み込み、読み込んだ結果を戻り値として返します。

    **注意事項 : ** command 引数は 1行での記述が必要となり、最後のリダイレクションをログとして保存します。

* run_script(String command) { コードブロック }

    PowerShell コマンド実行で使用します。
    前処理で検査用の PowerShell スクリプトを作成します。
    検査メソッドを順に実行し、 引数に指定した command をスクリプトに埋め込みます。
    埋め込みは command の最後のリダイレクションでログファイルを指定したコードを追加します。
    生成した スクリプトは、log.{日時}/{platform}/{server_name}/{domain}/の下に保存し、
    スクリプトを実行します。
    スクリプト実行後、コードブロック内のコードを実行します。

**検査結果を登録するメソッド**

以下は検査メソッド内で検査結果を登録するメソッドとなります。
TestItem クラスのメソッドとなり、検査メソッド引数の test_item オブジェクトのメソッド
として呼び出します。

* test\_item.results(String value)

    引数に検査結果を指定して、Excelの検査シートのセルを更新します。
    セルは検査IDを行、検査対象サーバを列にして指定します。
    文字列が数値の場合は数値型に変換してセルを更新します。

* test\_item.results(Map String values[])

    検査IDをキーにした複数の検査結果をセルに更新します。

* test\_item.verify_status(Boolean)

    引数に Boolean 値の合否結果を指定して、Excelの検査シートのセルの配色を変更します。
    引数が true の場合は、セルの背景をライトグリーンに変更します。
    false の場合は、セルの背景をローズに変更します。

* test\_item.verify_status(Map Boolean values[])

    検査IDをキーにした複数の合否結果をセルに更新します。

* test\_item.device(List csv, List header)

    デバイス結果を登録します。
    デバイス結果はネットワークデバイス名など検査項目が複数になる場合に使用します。
    デバイスの結果はシート名 "{ドメイン}_{検査ID}"として、Excel の新規シートに登録します。
