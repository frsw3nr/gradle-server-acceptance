# Getconfig Cleansing

Getconfig data cleansing


# Installation


下記のパッケージインストールをすると、Python ライブラリパスのカレントパス指定が効かない現象が発生する。
ImportError: No module named ... エラーが発生するため、以下の必須パッケージのみのインストールをする。

    $ pip install -r requirements.txt

If you don't use `pipsi`, you're missing out.
Here are [installation instructions](https://github.com/mitsuhiko/pipsi#readme).

Simply run:

    $ pipsi install .

# 開発環境のインストール

    $ pip install --force-reinstall --editable .

# Usage

To use it:

    $ gctool --help

