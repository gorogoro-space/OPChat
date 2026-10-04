# OPChat

OP(オペレーター)だけが読めるチャット `/o` と、OP が全員にメッセージを流す `/url` を提供する Paper 用プラグイン(Paper 1.21.11 以降・Java 21 必須)。
リポジトリ: https://github.com/gorogoro-space/OPChat (GPL-3.0)

## 作業の進め方(必ず守ること)

- **設計が確定するまで実装しない。** 機能追加や仕様変更は、まず設計案(何を・なぜ・どう変えるか、影響範囲)を提示し、承認を得てからコードを書く。
- 判断が必要な点は、選択肢を示して質問する。勝手に決めない。
- やり取りは日本語で行う。新しく書くコードのコメントは日本語でよい。既存の英語のコメントは、頼まれない限り書き換えない。
- 変更は必要最小限にする。頼まれていないリファクタリングや機能追加はしない。
- 作業後は、変更・追加・削除したファイルの一覧と変更内容を報告する。
- 仕様を変えたら README.md と CLAUDE.md も合わせて更新する。
- `git push` の前には必ず確認を取る。コミットは意味のある単位で分ける。
- リポジトリへの初回の `git push` の前には、「GitHub で Watch 設定(Custom → Issues と Pull requests)はしましたか?」と日本語で確認する。自動 Watch は廃止されており、設定しないと issue や PR の通知が届かない。
- コミットメッセージや PR に `Co-Authored-By: Claude` などの署名を付けない。`.claude/` は Git に入れない(`.git/info/exclude` で除外済み)。
- 実装中に設計の抜けや穴に気づいたら、黙って対処せず報告して相談する。
- プルリクエストをチェックするときは、次の観点で確認して報告する。
  - 変更概要(何を・なぜ変えているか)
  - 脆弱性(権限チェックの漏れ、入力値の検証、パスの扱いなど)
  - 安全面(データの破損・消失、再読み込み時の後始末、他プラグインの妨げなど)
  - 性能(TPS など)の低下(高頻度イベントでの重い処理、メインスレッドでの同期 I/O、定期タスクの追加など。「最重要の設計方針」に沿っているか)

## 環境

- Paper 1.21.11(`io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT`、`compileOnly`)/ `plugin.yml` の `api-version: '1.21.11'`(1.21.11 未満のサーバーでは読み込まれない)
- **Java 21 必須。** `build.gradle.kts` の toolchain で JDK 21 を使い、Java 21 の class ファイルを出力する。勝手に変えない
- ビルド: Gradle(Kotlin DSL、wrapper 9.7.1)。`gradlew.bat clean build`(Windows)。**JDK 21 で実行する**(`JAVA_HOME` を `C:\Program Files\Java\jdk-21` にする。既定の `java` は JDK 27)。「ビルドして」と言われたら常にクリーンビルドする。成果物は `build/libs/OPChat-<version>.jar`
  - IntelliJ の「アーティファクトのビルド」はクラスファイルが入らないことがある。必ず Gradle でビルドする
- バージョンは `gradle.properties` の `version` だけで管理する。`plugin.yml` の `version: '${version}'` は `processResources` でビルド時に置き換わる
- `ChatColor` など非推奨の API を使っているため、コンパイル時に deprecation の警告が出る(Maven から Gradle に移したときからそのまま。直すのは頼まれたとき)
- パッケージ: `space.gorogoro.opchat`(**すべて小文字**。大文字が混ざると plugin.yml の main と一致せず起動しない)
- 動作確認はサーバーを再起動して行う。PlugManX での読み込みは権限やコマンドの登録が不完全になることがある

## 最重要の設計方針

### TPS に影響させない
- 高頻度イベント(PlayerInteractEvent、PlayerMoveEvent、AsyncPlayerChatEvent など)を使うときは、安い判定を先に行い、対象外なら即座に抜ける
- BlockPhysicsEvent、VehicleMoveEvent など発生頻度が極端に高いイベントは使わない
- メインスレッドでファイルや DB の同期 I/O をしない(起動時・リロード時に一度だけ行う小さなファイルの読み込みは除く)
- 設定ファイルを追加する場合は、起動時・リロード時に一度だけ解析して保持する
- 定期タスクは最小限にし、追加するときは頻度と理由を設計案に書く

### データの保存
- `plugins/OPChat/config.yml` に `off-players`(OFF の人)と `notified-players`(OFF の案内を表示済みの人)を UUID のリストで保存する。jar に config.yml は同梱していない(無ければ空として扱う)
- `getConfig()` の更新と YAML 文字列への変換はメインスレッドで行い、ファイルへの書き込みだけを専用スレッド `OPChat-Save`(1 本)で行う。書き込み待ちが残っている間の変更は、最新の内容で 1 回にまとめて書き込む。停止時は書き込み待ちが終わるまで最大 10 秒待つ
- プラグインフォルダ以外には何も書き込まない

### 他プラグインとの関係
- 既存の機能(例: GSit、看板の click_event など)を妨げないこと。イベントをキャンセルする範囲は必要最小限にする

## 機能仕様

(現在の実装の動作。仕様を変えたらここを更新する)

- **`/o <message>`**: オンラインの OP 全員(OFF の人を除く)に `[OP] <送信者名>: <メッセージ>` を送る(`[OP]` は水色、`:` は緑)。OP 以外が実行しても何も起きない(エラーも出さない)。OFF の人が実行すると送信せず、本人に案内を出す
- **`/opchatoff`**: OP チャットの ON / OFF を切り替える(配信時に OP チャットを映さないため)。OP のみ、プレイヤーのみ(コンソールからは不可)。引数なし
  - OFF にすると `off-players` に追加。ON に戻すと `off-players` と `notified-players` の両方から消す
  - ログイン時(`PlayerJoinEvent`)、OP で、OFF で、`notified-players` に入っていない本人にだけ「OPチャットがOFFです」を 1 回表示し、`notified-players` に追加する(以降は再ログインしても出さない。ON → OFF にし直すとまた 1 回出る)。OP を外された人には出さず、印も付けない(OFF の設定は残るので、OP に戻ったときに 1 回出る)
  - `/url` は OFF でも届く
- **`/url <message>`**: OP が実行すると、オンラインの全員に `<送信者名>: <メッセージ>` を送る(`:` は緑)。OP 以外が実行しても何も起きない
- 共通: `/o`・`/url` は引数がなければ何もしない(usage も出さない。`onCommand` は常に true を返す)。権限ノードは使わず、`isOp()` で判定している(plugin.yml に permission の定義はない)
- 例外は `logStackTrace` でスタックトレースを警告ログに出す

## 過去にハマった点(他プロジェクトでの経験)

- `config.getString(path, "")` のように既定値を渡すと、jar 内 config.yml の既定値が参照されない。既定値なしで取得して null を判定すること
- plugin.yml で `default: true` にした権限でも、登録されないと Bukkit は「OP のみ」として扱う。全員向けの機能を権限で縛らない
- 乗客などのエンティティを `remove()` すると内部で降車イベント(`EntityDismountEvent`)が出る。これをキャンセルすると、消えたエンティティが乗ったまま残り、毎 tick 降ろそうとして重くなる

## ファイル構成

- `src/main/java/space/gorogoro/opchat/OPChat.java` — メインクラス。コマンド処理、ログイン時の案内、config.yml の保存をすべて持つ
- `src/main/resources/plugin.yml` — プラグイン定義、コマンド定義(`o`、`url`、`opchatoff`)
- `build.gradle.kts` / `settings.gradle.kts` / `gradle.properties` — Gradle の設定(バージョンは `gradle.properties`)
