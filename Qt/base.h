#ifndef BASE_H
#define BASE_H

#define DEBUG

//显示调试信息的宏
#ifdef DEBUG
#define mDebug(x)     (qDebug()    << "Debug   " + QDateTime::currentDateTime().toString("yyyy.MM.dd hh:mm:ss.zzz ") + QString(__FILE__) + " " + QString(__FUNCTION__) + " " + QString::number(__LINE__) + ">>" + " " + x)
#define mWarning(x)   (qWarning()  << "Warning " + QDateTime::currentDateTime().toString("yyyy.MM.dd hh:mm:ss.zzz ") + QString(__FILE__) + " " + QString(__FUNCTION__) + " " + QString::number(__LINE__) + ">>" + " " + x)
#define mError(x)     (qCritical() << "Error   " + QDateTime::currentDateTime().toString("yyyy.MM.dd hh:mm:ss.zzz ") + QString(__FILE__) + " " + QString(__FUNCTION__) + " " + QString::number(__LINE__) + ">>" + " " + x)
#define mInfo(x)      (qInfo()     << "Infor   " + QDateTime::currentDateTime().toString("yyyy.MM.dd hh:mm:ss.zzz ") + QString(__FILE__) + " " + QString(__FUNCTION__) + " " + QString::number(__LINE__) + ">>" + " " + x)
#else
#define mDebug(x)     (Base::log("Debug   " + QDateTime::currentDateTime().toString("yyyy.MM.dd hh:mm:ss.zzz ") + QString(__FILE__) + " " + QString(__FUNCTION__) + " " + QString::number(__LINE__) + ">>" + " " + x))
#define mWarning(x)   (Base::log("Warning " + QDateTime::currentDateTime().toString("yyyy.MM.dd hh:mm:ss.zzz ") + QString(__FILE__) + " " + QString(__FUNCTION__) + " " + QString::number(__LINE__) + ">>" + " " + x))
#define mError(x)     (Base::log("Error   " + QDateTime::currentDateTime().toString("yyyy.MM.dd hh:mm:ss.zzz ") + QString(__FILE__) + " " + QString(__FUNCTION__) + " " + QString::number(__LINE__) + ">>" + " " + x))
#define mInfo(x)      (Base::log("Infor   " + QDateTime::currentDateTime().toString("yyyy.MM.dd hh:mm:ss.zzz ") + QString(__FILE__) + " " + QString(__FUNCTION__) + " " + QString::number(__LINE__) + ">>" + " " + x))
#endif

#include <QWidget>
#include <QToolButton>
#include <QPoint>
#include <QMouseEvent>
#include <QDebug>
#include <QMessageBox>
#include <QSettings>
#include <QException>
#include <QDesktopWidget>
#include <QDir>
#include <QSystemTrayIcon>
#include <QMenu>
#include <QAction>
#include <QStyle>
#include <QApplication>
#include <QFile>
#include <QTextStream>
#include <QDateTime>
#include <QEventLoop>

namespace Ui {
class Base; //y一个应用程序的基类，包含许多有用的函数
}

class Base : public QWidget
{
    Q_OBJECT

public:

    explicit Base(QWidget *parent = 0);

    //配置文件
    QSettings *config; //配置文件读写指针
    QString confPath; //configration file path

    //UI配置
    QPoint last; //窗口最后的坐标
    QToolButton *minButton; //最小化按钮
    QToolButton *closeButton; //关闭按钮
    virtual void configWindow();

    //实用函数
    enum ButtonClicked{ButtonYes,ButtonNo,ButtonClose};
    virtual void initialize(); //初始化函数
    static void showInfo(QString title, QString tip); //显示信息
    static void showWarning(QString title, QString tip); //显示警告
    static void showError(QString title, QString tip); //显示错误
    static bool question(QString title, QString content); //显示提问窗口
    static ButtonClicked question(QString buttonYesText, QString buttonNoText,QString buttonCancelText, QString title, QString content); //显示提问窗口

    //日志记录
    static void log(QString msg);

    ~Base();

private:
    Ui::Base *ui;

public slots:
    void minWindow(); //最小化窗口

protected:
    //与窗口移动相关
    void mousePressEvent (QMouseEvent *e);
    void mouseMoveEvent (QMouseEvent *e);
    void mouseReleaseEvent(QMouseEvent *e);
};

#endif // BASE_H
