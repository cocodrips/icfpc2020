# ICFPC2020

## [portal](https://portal-prtffs3u5a-uw.a.run.app/) 
- [https://portal-prtffs3u5a-uw.a.run.app/](https://portal-prtffs3u5a-uw.a.run.app/)
- visualizer, modem

## 手順

### クライアントの変更の場合
好きなブランチ名でPRを作ってCIが通ればマージ

### サーバサイドの変更の場合
server から始まるブランチを切ってPRを作成
CIが通ればマージ

## CI
### cloudbuild_client_test.yaml
PRを作ると実行される．
クライアントのコードをデプロイ済みのdummyサーバでテスト

### cloudbuild_server_test.yaml
server から始まるブランチ名でPRを作ると実行される．
サーバのコードをデプロイせずにクライアントを用いてテスト

### cloudbuild_master.yaml
PRがmaster, submission にマージされると実行される．
クライアント，サーバのdocker imageをpush, サーバをデプロイ

