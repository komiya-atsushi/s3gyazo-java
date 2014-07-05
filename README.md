s3gyazo-java
============

Java / Spring Boot による Gyazo サーバのクローン実装です。
Amazon S3 を、Gyazo によってキャプチャされた画像の保存と HTTP での配信に用いています。


使い方
------

大まかな流れを以下に示します。詳細は各セクションをご参照ください。

1. [必要なもの](#必要なもの) を参考に、AWS の各種サービスやサーバの事前準備をします。
2. アプリケーションを実行するサーバ上にて、 ``git clone https://github.com/komiya-atsushi/s3gyazo-java.git`` します。
3. [アプリケーション設定](#アプリケーション設定) を参考に、 ``application.yml`` ファイルを作成して設定を記述します。
4. [起動方法](#起動方法) を参考に、Gyazo サーバのアプリケーションを起動します。


### 必要なもの

- 本アプリケーションを実行するためのサーバ
    - 1.7 以上の Oracle JDK もしくは OpenJDK がインストールする必要があります。
    - 外部ネットワークとの疎通ができる状態にしておきます。
- S3 バケット
    - キャプチャ画像の保存と HTTP での配信に用います。
    - Web サイトホスティングができるよう、設定をする必要があります。
      [例: 静的ウェブサイトをセットアップする](http://docs.aws.amazon.com/ja_jp/AmazonS3/latest/dev/HostingWebsiteOnS3Setup.html)
      などを参考に、必要な設定を済ませてください。
- S3 バケットにアクセス可能なアクセスキー／シークレットキー
    - 当該認証情報は最低でも、 S3 バケットへの PutObject 権限を保持している必要があります。
    - [ユーザーに対し、Amazon S3 の特定のバケットにアクセスすることを許可する](http://docs.aws.amazon.com/ja_jp/IAM/latest/UserGuide/ExampleIAMPolicies.html#iampolicy-example-s3)
      などを参考に、IAM を用いて事前に準備を済ませてください。


### アプリケーション設定

S3 バケット名や IAM のアクセスキー／シークレットキーなど、環境に依存する設定は ``application.yml`` に記述します。
リポジトリ内にある ``application.yml.template`` を参考に、 ``application.yml`` を用意してください。

- s3
    - bucket
        - S3 のバケット名を指定します。
    - region
        - S3 バケットを作成したリージョン名を指定します。
- iam
    - accessKey, secretKey
        - IAM のアクセスキー／シークレットキーを指定します。
- app
    - url
        - prefix
            - アップロードされた画像に HTTP(S) でアクセスする際の、(拡張子含むハッシュ部分以降を除いた) URL
              のプレフィックスを指定します。
            - 特にこだわりがなければ、 ``http://${s3.bucket}.s3-website-${s3.region}.amazonaws.com/`` と指定します。
              (プレースホルダはアプリケーションによって、自動的にバケット名・リージョン名に置換されます)
- server
    - port
        - 本アプリケーションが HTTP リクエストを待ち受けするポート番号を指定します


### 起動方法

とりあえず起動してみるだけなら、以下のコマンドで OK です。

    $ ./gradlew clean build && java -jar build/libs/s3gyazo-java-0.1.0.jar

サービスとして動かす場合は、シェルスクリプトなどを準備する必要があります (準備中)。


Gyazo クライアントについて
--------------------------

以下のサイトなどを参考に、Gyazo クライアントの設定にてアプリケーションが稼動しているホスト・ポート番号を指定してください。

- [gyazowin+を最新のgyazowinのソースとマージした - tyoro.exe](http://exe.tyo.ro/2012/02/gyazowingyazowin.html)
- [Gyazowinをforkして野良Gyazoサーバにポストできるようにホスト名,ポート,パスを指定できるようにした - 今日もスミマセン](http://d.hatena.ne.jp/snaka72/20100713/1279029541)


Copyright / ライセンス
----------------------

Copyright (C) 2014 KOMIYA Atsushi

MIT License です。詳しくは LICENSE.txt をご覧ください。
