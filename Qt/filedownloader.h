#ifndef FILEDOWNLOADER_H
#define FILEDOWNLOADER_H

#include <QObject>
#include <QUrl>
#include <QMap>
#include <QFile>
#include <QPixmap>
#include <QEventLoop>
#include <QNetworkAccessManager>
#include <QNetworkRequest>
#include <QNetworkReply>

#include "base.h"

class FileDownloader : public QObject
{
    Q_OBJECT
public:
    explicit FileDownloader(QObject *parent = 0);

    //同步下载方法，静态方法,适合下载小文件
    static QByteArray download(QUrl url);
    static bool download(QUrl url, QString downloadDirectory);
    static QPixmap downloadPixmap(QUrl url);

//    //异步下载方法,非静态方法
//    void download(QUrl url,uint taskId);
//    void download(QUrl url,uint taskId, QString filePath);
//    void downloadPixMap(QUrl url, uint taskId);

//private:
//    QNetworkAccessManager *manager;
//    QMap<uint,QUrl> waitTodownLoad;

//signals:
//    void downloadFinished(QByteArray data,uint taskId);
//    void downloadFinished(QPixmap pixmap, uint taskId);

//private slots:
//    void readyRead(QNetworkReply *reply);
//public slots:

};

#endif // FILEDOWNLOADER_H
