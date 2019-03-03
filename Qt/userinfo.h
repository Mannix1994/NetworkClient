#ifndef USERINFO_H
#define USERINFO_H

#include <QWidget>
#include "base.h"
#include <QNetworkAccessManager>
#include <QNetworkReply>
#include <QNetworkRequest>
#include <QTextCodec>
#include <QJsonDocument>
#include <QJsonObject>
#include <QJsonParseError>
#include <QTimer>
#include <QModelIndex>
#include <QStandardItem>
#include <QStandardItemModel>
#include <QMap>

namespace Ui {
class UserInfo;
}

class UserInfo : public Base
{
    Q_OBJECT

public:
    explicit UserInfo(Base *parent = 0);
    void setUserIndex(QString userIndex);
    ~UserInfo();

private:
    Ui::UserInfo *ui;
    QNetworkAccessManager *postManager;
    QTextCodec *codec;
    QString userIndex;
    void initialize();

    //操作结果
    typedef struct{
        QString userIndex; //用户索引
        QString result;//结果
        QString message;//信息
        QString maxLeavingTime;//最大剩余时间
        QString userName;//用户名
        QString userId;//用户ID
        QString userIp;//用户IP
        QString userGroup;//用户组
        QString accountFee;//当前费用
        QString userPackage;//当前套餐
        QString notify;//提示信息
        QString welcomeTip;//欢迎信息
        QString portalIp;//登录服务器IP
        QString isErrorMsg;//是否是错误消息
    }Result;
    Result result; //消息结果
    bool parseJson(QByteArray json); //解析json数据
    void processResult();//处理登录和注销的结果

    //表格
    uint rowCount;//当前表格数据数量
    QStandardItemModel  *model; //表格模型
    void refreshTable();//刷新表格
    void insertToTable(QString key,QString value);

private slots:
    void postManagerReadyRead(QNetworkReply *reply);
    void reload();

};

#endif // USERINFO_H
