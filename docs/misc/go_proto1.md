Go �{�C���[�v���[�g
===================

* ���b�N�e�X�g����
* JSON 
* �f�U�C���p�^�[��
* K-Means�@
* Excel���|�[�g
* YAML 
* Prometheus

���b�N�e�X�g����
----------------

https://qiita.com/minamijoyo/items/cfd22e9e6d3581c5d81f

�����Ɠ��e�m�F�B���ׂ������e�͎��̒����ŋ�̉�����

�{�C���[�v���[�g����
----------------

���t�@�����X����

    https://github.com/moul/golang-boilerplate.git

Glide

### dep

    go get -u github.com/golang/dep/cmd/dep
    mkdir $GOPATH/src/github.com/trydep
    dep init

main.go ����

    package main

    import "github.com/fatih/color"

    func main() {
        color.Red("��")
        color.Cyan("��")
        color.Blue("��")
        color.Magenta("��")
    }

dep ensure
dep status

    PROJECT                        CONSTRAINT     VERSION        REVISION  LATEST   PKGS USED
    github.com/fatih/color         v1.7.0         v1.7.0         5b77d2a   v1.7.0   1
    github.com/mattn/go-colorable  v0.0.9         v0.0.9         167de6b   v0.0.9   1
    github.com/mattn/go-isatty     v0.0.9         v0.0.9         e1f7b56   v0.0.9   1
    golang.org/x/sys               branch master  branch master  749cb33   749cb33  1

go run main.go

https://github.com/tmrts/boilr.git

https://github.com/robtec/cli-boilerplate.git

urfave/cli ���C���|�[�g���Ă���

go run cli1_urfave_cli.go

kingpin ���Aclif ���Acobra

cobra ���Ɛݒ�t�@�C���Ƃ̘A�g���ł��遨cobra�ɂ���

### clig

https://github.com/izumin5210/clig.git

go get github.com/izumin5210/clig/cmd/clig

�C���|�[�g���郉�C�u�����������B���������y�ʂł��悢����

git clone https://github.com/moul/golang-boilerplate.git

JSON ����
--------------------

���t�@�����X

https://qiita.com/nayuneko/items/2ec20ba69804e8bf7ca3

    mkdir json
    dep ensure

twitter �̉ӏ��������Ȃ�A���I�Ƀl�X�g����JSON�̉��

������x�A�������ق����ǂ�

���b�v�A�b�v
-------------

* clig �Ńv���g�^�C�v�����
* JSON �ǂݍ��݃T���v��
* �f�U�C���p�^�[���T���v��(Visitor, Composite)
* K-Means �@�T���v��
* Excel ���|�[�g�T���v��
* YAML, Prometheus �Ȃ�


