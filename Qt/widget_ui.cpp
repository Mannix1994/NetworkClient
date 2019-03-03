#include "widget.h"
#include "ui_widget.h"

/**
 * @brief Widget::createSystemTrayIcon 创建托盘图标
 * @param icon 要显示的图标
 * @param toolTip 托盘图标的ToolTip内容
 */
bool Widget::createSystemTrayIcon(QIcon icon,QString toolTip){
    //设置托盘图标
    if(QSystemTrayIcon::isSystemTrayAvailable())
    {
        //创建Action
        QPixmap minPix  = style()->standardPixmap(QStyle::SP_TitleBarNormalButton);
        QPixmap closePix = style()->standardPixmap(QStyle::SP_TitleBarCloseButton);
        restoreAction = new QAction("恢复(&R)", this);
        restoreAction->setIcon(minPix);
        connect(restoreAction, SIGNAL(triggered()), this, SLOT(showNormal()));
        quitAction = new QAction("退出(&Q)", this);
        quitAction->setIcon(closePix);
        quitAction->setToolTip("注销并退出程序");
        connect(quitAction,SIGNAL(triggered()), this, SLOT(closeTrayIcon()));
        //创建菜单
        trayIconMenu = new QMenu(this);
        trayIconMenu->addAction(restoreAction);
        trayIconMenu->addSeparator();
        trayIconMenu->addAction(quitAction);
        trayIcon = new QSystemTrayIcon(this);
        trayIcon->setContextMenu(trayIconMenu);
        //设置图标
        trayIcon->setIcon(icon);
        //鼠标放托盘图标上提示信息
        trayIcon->setToolTip(toolTip);
        //设置托盘图标被点击的槽函数
        this->connect(trayIcon, SIGNAL(activated(QSystemTrayIcon::ActivationReason)), this, SLOT(iconActivated(QSystemTrayIcon::ActivationReason)));
        //显示图标
        trayIcon->show();
        //将托盘图标是否创建的标志设为true
        isTrayIconCreated = true;
        return true;
    }
    else{
        isTrayIconCreated = false;
        return false;
    }
}

/**
 * @brief Widget::showMessageByTrayIcon 在托盘图标上显示信息
 * @param title
 * @param content
 * @param msecs 时长，单位为毫秒
 * @return
 */
bool Widget::showMessageByTrayIcon(QString title, QString content,int msecs){
    if(isTrayIconCreated){
        trayIcon->showMessage(title, content, QSystemTrayIcon::Information,msecs);
        return true;
    }
    else
        return false;
}

/**
 * @brief Widget::minWindow 最小化
 */
void Widget::minWindow()
{
    this->showMinimized();
}

/**
 * @brief Widget::showNormal 显示
 */
void Widget::showNormal(){
    this->show();
}

/**
 * @brief MainWidget::iconActivated 托盘图标被点击
 * @param reason
 */
void Widget::iconActivated(QSystemTrayIcon::ActivationReason reason)
{
    if(reason == QSystemTrayIcon::Trigger)
    {
        if(this->isMinimized())
        {
           this->showFullScreen();
        }
        else
           this->show();
        this->activateWindow();
    }
}

void Widget::closeTrayIcon(){
    if(ui->pbLogin->text() == LOGOUT){ //如果登录按钮上是注销
        on_pbLogin_clicked(); //那么先注销
    }
    while(ui->pbLogin->text() != LOGIN){//等待注销完成
        QCoreApplication::processEvents();//同时处理事件
    }
    this->close();
}

/**
 * @brief Widget::changeBackground 更改背景
 * @param checked
 */
void Widget::changeBackground(bool checked){
    Q_UNUSED(checked)
    QString defaultPath = QStandardPaths::standardLocations(QStandardPaths::DownloadLocation).first();
    QString programPath = QDir::currentPath();
    QString picPath = QFileDialog::getOpenFileName(this,"选择背景",defaultPath,"图片文件 (*.jpg *.jpeg)");
    if(!picPath.isEmpty()){

        //删除旧的背景
        QFile::remove(programPath+"/bg.jpeg");
        QFile::remove(programPath+"/bg.jpg");
        //将图片复制到程序目录
        QString dstPath = programPath+"/bg."+picPath.split(".").last();
        QFile::copy(picPath,dstPath);

        //更换背景
        QPixmap pixma(picPath);
        if(!pixma.isNull())
        {
            QPixmap pixmap=pixma.scaled(this->size());
            QPalette   palette;
            palette.setBrush(QPalette::Window,QBrush(pixmap));
            this->setPalette(palette);
            this->setAutoFillBackground(true);
        }
    }else{
        this->showInfo("提示","未选择图片");
    }
}
