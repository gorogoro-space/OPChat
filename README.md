# OPChat
[![Spigot 1.19.1](https://img.shields.io/badge/Spigot-1.19.1-brightgreen.svg)](https://www.spigotmc.org/)
[![GitHub release](https://img.shields.io/github/release/gorogoro-space/OPChat.svg)](https://github.com/gorogoro-space/OPChat/releases)
[![contributions welcome](https://img.shields.io/badge/contributions-welcome-brightgreen.svg?style=flat)](https://github.com/gorogoro-space/OPChat/issues)
[![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](https://github.com/gorogoro-space/OPChat/blob/main/LICENSE)

Operator chat system.

OP(オペレーター)どうしだけで会話できるチャットと、OP から全員へメッセージを流すコマンドを追加するプラグインです。

# Requirements
- Spigot / Paper 1.19.1 以降(`api-version: 1.15`)
- Java 8 以降

# Installation method
Please place the .jar file in the plugins folder.

.jar ファイルをサーバーの plugins フォルダに置き、サーバーを再起動してください。

# Usage
```
/o <message>     OP 全員にだけメッセージを送ります
/url <message>   OP が全員にメッセージ(URL など)を送ります
```
- どちらも OP だけが使えます。OP 以外が実行しても何も起きません
- `/o` のメッセージは `[OP] 名前: メッセージ` の形で、オンラインの OP 全員に届きます
- `/url` のメッセージは `名前: メッセージ` の形で、オンラインの全員に届きます

# Data
何も保存しません(設定ファイルもありません)。

# Disclaimer
Do not assume any responsibility by use. Please use it at your own risk.

## IntelliJ IDEA でのビルド手順

本プロジェクトはビルドツールに Maven を使用しています。
IntelliJ IDEA 上で正しくプラグイン（JARファイル）を生成するには、以下の手順を実行してください。

### 🚨 注意：Plugins ではなく Lifecycle を使用してください
Maven ツールウィンドウ内の `Plugins` -> `jar:jar` を直接実行すると、コンパイルが行われず中身が空の JAR ファイルが生成されてしまいます。
必ず以下の手順通り **`Lifecycle`** を使用してください。
「ビルド → アーティファクトのビルド」もクラスファイルが入らないことがあるので使わないでください。

### 🛠️ ビルド手順

1. IntelliJ IDEA の画面右端にある **「Maven」タブ** をクリックして開きます。
2. プロジェクト名（OPChat）を展開し、 **`Lifecycle`（ライフサイクル）** ツリーを開きます。
3. リスト内にある **`clean`** をダブルクリックして実行します（古いビルドキャッシュを削除します）。
4. 続けてリスト内にある **`package`** をダブルクリックして実行します。

### 📦 生成されたファイルの場所
ビルドが成功すると、プロジェクトのルート直下に `target` フォルダが作成（または更新）され、その中に中身の詰まった正しい JAR ファイルが生成されます。

* **生成先:** `target/OPChat-1.0.jar`

この JAR ファイルを Minecraft サーバーの `plugins` フォルダに配置してください。

## 開発(IntelliJ IDEA で Claude Code を使う)

このリポジトリには、AI コーディングツール [Claude Code](https://docs.claude.com/ja/docs/claude-code/overview) 向けの作業ルールを書いた `CLAUDE.md` があります。IntelliJ IDEA で使う手順は次のとおりです。

1. Claude の有料プラン(Pro / Max)か、Anthropic API のアカウントを用意します。
2. Windows では、先に [Git for Windows](https://git-scm.com/downloads/win) をインストールします。
3. Claude Code 本体をインストールします。Windows では PowerShell で次を実行します。
   ```
   irm https://claude.ai/install.ps1 | iex
   ```
4. IntelliJ IDEA の「設定 → プラグイン → Marketplace」で「Claude Code」を検索してインストールし、IDE を再起動します。検索結果には Anthropic 以外が作った似た名前のプラグインも表示されます。プラグイン名の下に書かれた提供元が「Anthropic」になっているものを選んでください。
5. プロジェクトを開いて、ターミナルで `claude` を実行します(`Ctrl+Esc` でも起動できます)。初回はブラウザでログインします。

起動すると `CLAUDE.md` が自動で読み込まれ、このプロジェクトの設計方針に沿って作業します。

## ライセンス

[LICENSE](LICENSE) を参照してください。

## kubotan へのメモ

リポジトリを新規作成したら、**必ず Watch を設定すること**(忘れない!)。
GitHub の自動 Watch 機能は 2025 年 5 月に廃止されたため、設定しないと他の人が立てた issue や PR の通知が届かない。

1. リポジトリのページ右上の **「Watch」** を押す
2. **「Custom」** を選び、**Issues** と **Pull requests** にチェックを入れる
