#ifndef WIDGET_H
#define WIDGET_H

#include <QWidget>
#include "base.h"
#include <QNetworkAccessManager>
#include <QNetworkReply>
#include <QNetworkRequest>
#include <QTcpSocket>
#include <QTextCodec>
#include <QJsonDocument>
#include <QJsonObject>
#include <QJsonParseError>
#include <QHostInfo>
#include <QFile>
#include <QDataStream>
#include <QTime>
#include <QCloseEvent>
#include <QFileDialog>
#include <QStandardPaths>
#include <QMouseEvent>
#include <QKeyEvent>
#include <QScriptEngine>
#include <QScriptValueList>
#include <QScriptValue>

#include "userinfo.h"
#include "servicemanager.h"

#ifndef LOGIN
#define LOGIN "登录"
#endif
#ifndef LOGOUT
#define LOGOUT "注销"
#endif

namespace Ui {
class Widget;
}

class Widget : public Base
{
    Q_OBJECT

public:
    explicit Widget(Base *parent = 0);
    ~Widget();

private:
    Ui::Widget *ui;
    QTextCodec *codec;
    void configWindow();
    void initialize();

    bool isButtonPressed;
    enum ApplicationState{GetUserInfo,Login,LoginWithOpeatorInfo,Logout,LogoutAllIP,None} applicationState;

    //托盘图标
    QAction *restoreAction;
    QAction *quitAction;
    QSystemTrayIcon *trayIcon;
    QMenu *trayIconMenu;
    bool isTrayIconCreated;
    bool createSystemTrayIcon(QIcon icon, QString toolTip); //创建托盘图标
    bool showMessageByTrayIcon(QString title,QString content,int msecs = 10000);//在托盘图标上显示信息

    //网络
    QNetworkAccessManager *getManager; //get请求管理
    QNetworkAccessManager *postManager; //post请求管理
    QString makeLoginString(QString queryString);
    QString makeLoginString(QString queryString,QString service,QString operatorUserId,QString operatorPwd);
    bool isHostAccessible(QString url);
    bool isHostAccessible(QString ip,uint port);

    //工具类
    void parseJson(QByteArray json); //解析json数据
    void processResult();//处理登录和注销的结果
    void readUserInfo();//读取用户名和密码
    void processGetOnlineUserInfoResult();
    void processLoginAndLogoutResult();
    void processLogoutAllIPResult();
    void getValidCode();
    void setWidgetVisible(bool b);

    //操作结果
    typedef struct{
        QString userIndex;
        QString result;
        QString message;
        QString userId;
    }Result;
    Result result; //登录注销等的结果

    //右键菜单
    QAction *actionChangeBackground;        //改变背景菜单项
    QAction *actionShowUserInfo;            //查看用户信息项
    QAction *actionAutoLogin;               //自动登录
    QAction *actionLogoutByUserIdAndPass;   //通过账号和密码强制注销接口
    QAction *actionChooseService;           //选择服务菜单项
    ServiceManager *serviceManager;         //选择服务界面
    void createRightKeyMenu();              //创建右键菜单

    //用户信息
    UserInfo *userInfo;
    QString service;
    QString operatorUserId;
    QString operatorPwd;
    QString queryString;

private slots:
    //托盘菜单
    void minWindow(); //最小化窗口
    void showNormal(); //恢复显示窗口
    void iconActivated(QSystemTrayIcon::ActivationReason reason); //托盘图标被点击
    void closeTrayIcon();//托盘菜单引发的退出事件

    //登录部分
    void on_pbLogin_clicked();

    //网络
    void getManagerReadyRead(QNetworkReply *reply);
    void postManagerReadyRead(QNetworkReply *reply);
    void networkStateChanged(QNetworkAccessManager::NetworkAccessibility acb);

    //UI
    void on_leUserID_textChanged(const QString &arg1);
    void on_cbSavePassword_clicked(bool checked);
    void on_lePassword_textChanged(const QString &arg1);
    void operatorInfoUpdated(QString service,QString id,QString pwd);

    //右键菜单
    void changeBackground(bool checked);
    void showUserInfo(bool checked);
    void setAutoLogin(bool checked);
    void logoutByUserIdAndPass(bool checked);
    void chooseService(bool checked);

protected:
    void closeEvent(QCloseEvent *event);
    void enterEvent(QEvent *event);
    void leaveEvent(QEvent *event);
    void mouseMoveEvent(QMouseEvent *event);
    void mousePressEvent(QMouseEvent *event);
    void mouseReleaseEvent(QMouseEvent *event);
    void keyPressEvent(QKeyEvent *event);
};

#endif // WIDGET_H
