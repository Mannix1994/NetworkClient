#include "filedownloader.h"

FileDownloader::FileDownloader(QObject *parent) : QObject(parent)
{

}

QByteArray FileDownloader::download(QUrl url){
    QNetworkAccessManager manager;
    QEventLoop loop;
    QNetworkReply *reply = manager.get(QNetworkRequest(url));
    //请求结束并下载完成后，退出子事件循环
    QObject::connect(reply, SIGNAL(finished()), &loop, SLOT(quit()));
    //开启子事件循环
    loop.exec();

    QByteArray data("");
    if(reply->error() == QNetworkReply::NoError){
       data = reply->readAll();
    }else{
        mDebug(reply->errorString());
    }
    reply->deleteLater();
    return data;
}

/**
 * @brief FileDownloader::download 下载url指定文件到文件filePath
 * @param url 文件Url
 * @param filePath 文件保存目录
 * @return 下载成功返回true
 */
bool FileDownloader::download(QUrl url, QString downloadDirectory){
    //检查Url合法性
    if(!url.isValid()){
        mDebug("not a valid url");
        return false;
    }
    //对路径进行处理
    downloadDirectory  = downloadDirectory.replace(QString("\\"),QString("/"));
    QDir dir(downloadDirectory);
    downloadDirectory = dir.absolutePath();
    //如果保存路径不存在，创建保存路径
    if(!dir.exists()){
        dir.mkdir(downloadDirectory);
    }
    QByteArray data = download(url);
    if(data.isEmpty()){ //下载失败
        return false;
    }
    QString finalPath = url.fileName();
    if(!downloadDirectory.isEmpty()){
        if(downloadDirectory.endsWith('/')){
            finalPath = downloadDirectory + url.fileName();
        }else{
            finalPath = downloadDirectory +"/"+url.fileName();
        }
    }
    QFile file(finalPath);
    if(file.open(QIODevice::WriteOnly)){
        if(file.write(data)==-1){
            mDebug("Failed to write file:"+finalPath);
            return false;
        }
        file.close();
        return true;
    }else{
        mDebug("Failed to open file:"+finalPath);
        return false;
    }
}

/**
 * @brief FileDownloader::downloadPixmap 下载图像
 * @param url
 * @return
 */
QPixmap FileDownloader::downloadPixmap(QUrl url){
    QPixmap pixmap;
    if(!url.isValid()){
        mDebug("not a valid url");
        return pixmap;
    }
    QByteArray data = download(url);
    if(data.isEmpty()){ //下载失败
        return pixmap;
    }
    pixmap.loadFromData(data);
    return pixmap;
}
