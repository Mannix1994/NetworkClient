#include "widget.h"
#include "ui_widget.h"
#include "filedownloader.h"

//#include "trayIcon.cpp" 其他函数包含在trayIcon.cpp中

Widget::Widget(Base *parent) :
    Base(parent),ui(new Ui::Widget),isButtonPressed(false),applicationState(None),result({"","","",""})
{
    ui->setupUi(this);
    this->configWindow();
    this->initialize();
    this->createSystemTrayIcon(QIcon(":/64px/Icons/64X64.ico"),"校园宽带客户端");
    this->createRightKeyMenu();
    if(config->value("Conf/autoLogin",false).toBool() && !ui->leUserID->text().isEmpty() && !ui->lePassword->text().isEmpty()){
        on_pbLogin_clicked();
    }
}

Widget::~Widget()
{
    delete ui;
}
/**
 * @brief Widget::configWindow 配置窗口
 */
void Widget::configWindow(){
    Base::configWindow();
    this->setWindowIcon(QIcon(":/64px/Icons/64X64.ico"));
    this->setWindowTitle("校园网络客户端");
}

/**
 * @brief Widget::initialize 初始化
 */
void Widget::initialize(){
    Base::initialize();
    //get请求管理
    getManager = new QNetworkAccessManager(this);
    connect(getManager,SIGNAL(finished(QNetworkReply*)),this,SLOT(getManagerReadyRead(QNetworkReply*)));
    connect(getManager,SIGNAL(networkAccessibleChanged(QNetworkAccessManager::NetworkAccessibility)),this,SLOT(networkStateChanged(QNetworkAccessManager::NetworkAccessibility)));
    //post请求管理
    postManager = new QNetworkAccessManager(this);
    connect(postManager,SIGNAL(finished(QNetworkReply*)),this,SLOT(postManagerReadyRead(QNetworkReply*)));
    //设置编码
    codec = QTextCodec::codecForName("utf-8");
    //设置UI
    ui->lePassword->setEchoMode(QLineEdit::Password);
    ui->leValidCode->setVisible(false);
    ui->laValidCodeImage->setVisible(false);
    ui->leValidCode->setValidator(new QIntValidator(0, 9999,this));
    //读取用户信息
    this->readUserInfo();
    //
    userInfo = new UserInfo();
    //
    serviceManager = new ServiceManager();
    connect(serviceManager,SIGNAL(operatorInfoUpdated(QString,QString,QString)),this,SLOT(operatorInfoUpdated(QString,QString,QString)));
}

/**
 * @brief Widget::on_pbLogin_clicked 登录按钮被点击
 */
void Widget::on_pbLogin_clicked()
{
    QString userId = ui->leUserID->text().trimmed();
    QString password = ui->lePassword->text().trimmed();
    if(userId.isEmpty()){
        ui->leUserID->setFocus();
        this->showInfo("提示","账号为空");
        return;
    }
    if(password.isEmpty()){
        ui->lePassword->setFocus();
        this->showInfo("提示","密码为空");
        return;
    }
    if(ui->leValidCode->isVisible()){
        if(ui->leValidCode->text().trimmed().length()<4){
            this->showInfo("提示","验证码位数不足");
            return;
        }
    }
    ui->laValidCodeImage->setVisible(false);
    ui->leValidCode->setVisible(false);
    if(!this->isHostAccessible("192.168.2.135",80)){
        this->showError("提示","登录服务器不可达，请检查网络是否连接好");
        ui->pbLogin->setText(LOGIN);
        this->setWidgetVisible(true);
        return;
    }
    ui->pbLogin->setEnabled(false); //禁用登录按钮
    if(ui->pbLogin->text() == LOGIN){ //登录
        QNetworkRequest request(QUrl("http://webportal.scu.edu.cn/eportal/InterFace.do?method=getOnlineUserInfo"));
        QString data = "userIndex=";
        request.setHeader(QNetworkRequest::ContentTypeHeader,QVariant("application/x-www-form-urlencoded; charset=UTF-8"));
        postManager->post(request,data.toUtf8());
        applicationState = GetUserInfo;
    }else{//注销
        applicationState = Logout;
        QNetworkRequest request(QUrl("http://webportal.scu.edu.cn/eportal/InterFace.do?method=logout"));
        QString logoutString = "userIndex="+result.userIndex;
        request.setHeader(QNetworkRequest::ContentTypeHeader,QVariant("application/x-www-form-urlencoded; charset=UTF-8"));
        postManager->post(request,logoutString.toUtf8());
    }
}

/**
 * @brief Widget::getManagerReadyRead getManager的槽函数
 * @param reply
 */
void Widget::getManagerReadyRead(QNetworkReply *reply){
    if(reply->error() == QNetworkReply::NoError){
        QByteArray bytes = reply->readAll();
        QString result = codec->toUnicode(bytes);
        queryString = result.mid(result.indexOf("?")+1).replace("'</script>\r\n","");
        QString loginString = this->makeLoginString(queryString);
        if(!loginString.isEmpty()){//如果结果不空，做登录操作
            QNetworkRequest request(QUrl("http://webportal.scu.edu.cn/eportal/InterFace.do?method=login"));
            request.setHeader(QNetworkRequest::ContentTypeHeader,QVariant("application/x-www-form-urlencoded; charset=UTF-8"));
            postManager->post(request,loginString.toUtf8());
        }
    }
    else{
        mDebug(reply->errorString());
    }
    reply->deleteLater();
}

/**
 * @brief Widget::postManagerReadyRead postManager的槽函数
 * @param reply
 */
void Widget::postManagerReadyRead(QNetworkReply *reply){
    if(reply->error() == QNetworkReply::NoError){
        QByteArray bytes = reply->readAll();
        QString result = codec->toUnicode(bytes);
        if(!result.isEmpty()){ //登录或注销操作的结果不空，解析结果并处理结果
            this->parseJson(bytes);//解析
            //this->processResult();//处理
        }else{//如果为空，说明已经登录网络
            ui->pbLogin->setText(LOGOUT);
            ui->pbLogin->setEnabled(true);
            this->setWidgetVisible(false);
        }
    }
    else{
        mDebug(reply->errorString());
    }
    reply->deleteLater();
}

/**
 * @brief Widget::networkStateChanged 网络状态变化时，若网络不可用，则退出程序
 * @param acb
 */
void Widget::networkStateChanged(QNetworkAccessManager::NetworkAccessibility acb){
    mDebug("");
    if(ui->pbLogin->text() == LOGOUT){
        if(acb != QNetworkAccessManager::Accessible){
            this->showWarning("提示","网络被断开了");
            ui->pbLogin->setText(LOGIN);
            this->close();
        }
    }
}

/**
 * @brief Widget::makeLoginString 返回登录数据
 * @param queryString
 * @return
 */
QString Widget::makeLoginString(QString queryString){
    //
    QString userId = ui->leUserID->text().trimmed();
    QString password = ui->lePassword->text().trimmed();
    QString validCode = ui->leValidCode->text().trimmed();
    ui->leValidCode->clear();
    if(userId.isEmpty()){
        ui->leUserID->setFocus();
        return "";
    }
    if(password.isEmpty()){
        ui->lePassword->setFocus();
        return "";
    }
    queryString = QString(queryString.toUtf8().toPercentEncoding());
    QString loginString = "userId="+userId.toUtf8().toPercentEncoding().toPercentEncoding()+"&password="
            +password.toUtf8().toPercentEncoding().toPercentEncoding()+"&service="+service+"&queryString="
            +queryString+"&operatorPwd=&operatorUserId=&validcode="+validCode;
    return loginString;
}

QString Widget::makeLoginString(QString queryString,QString service, QString operatorUserId, QString operatorPwd){
    QString userId = ui->leUserID->text().trimmed();
    QString password = ui->lePassword->text().trimmed();
    QString validCode = ui->leValidCode->text().trimmed();
    ui->leValidCode->clear();
    queryString = QString(queryString.toUtf8().toPercentEncoding());
    QString loginString = "userId="+userId.toUtf8().toPercentEncoding().toPercentEncoding()
            +"&password="+password.toUtf8().toPercentEncoding().toPercentEncoding()
            +"&service="+service+"&queryString="+queryString+"&operatorPwd="
            +operatorPwd.toUtf8().toPercentEncoding().toPercentEncoding()
            +"&operatorUserId="+operatorUserId.toUtf8().toPercentEncoding().toPercentEncoding()+"&validcode="+validCode;
    return loginString;
}

/**
 * @brief Widget::parseJson 解析postManager返回的json数据
 * @param json
 */
void Widget::parseJson(QByteArray json){
    QJsonParseError error;
    QJsonDocument jsonDoc = QJsonDocument::fromJson(json,&error);
    if(error.error == QJsonParseError::NoError){
        //qDebug()<<jsonDoc.isArray()<<jsonDoc.isEmpty()<<jsonDoc.isNull()<<jsonDoc.isObject();
        if(jsonDoc.isObject()){
            QJsonObject jsonObj = jsonDoc.object();
            if(jsonObj.contains("userIndex")){
                result.userIndex = jsonObj.take("userIndex").toString();
            }
            if(jsonObj.contains("result")){
                result.result = jsonObj.take("result").toString();
            }
            if(jsonObj.contains("message")){
                result.message = jsonObj.take("message").toString();
            }
            if(jsonObj.contains("userId")){
                result.userId = jsonObj.take("userId").toString();
            }
            //mDebug(result.toString());
            this->processResult();
        }else{
            mDebug("not a json object");
            this->showError("错误","不是一个Json对象");
            this->setWidgetVisible(true);
        }
    }
    else{
        mDebug("Json解析错误"+error.errorString());
        this->showError("错误","Json解析错误"+error.errorString());
        this->setWidgetVisible(true);
    }
}

/**
 * @brief Widget::processResult 处理解析后的数据
 */
void Widget::processResult(){
    //
    if(applicationState == GetUserInfo){
        this->processGetOnlineUserInfoResult();
    }else if(applicationState == Login || applicationState == Logout || applicationState == LoginWithOpeatorInfo){
        this->processLoginAndLogoutResult();
    }else if(applicationState == LogoutAllIP){
        this->processLogoutAllIPResult();
    }
}

void Widget::processGetOnlineUserInfoResult(){
    if(result.result == "fail"){
        applicationState = Login;
        QNetworkRequest request(QUrl("http://www.baidu.com"));
        request.setHeader(QNetworkRequest::ContentTypeHeader,QVariant("application/x-www-form-urlencoded; charset=UTF-8"));
        getManager->get(request);
    }else{
        if(result.userId != ui->leUserID->text().trimmed()){
            bool r = this->question("提示","本机已经有人登录，是否将其强制下线？");
            if(r){
                applicationState = Logout;
                QNetworkRequest request(QUrl("http://webportal.scu.edu.cn/eportal/InterFace.do?method=logout"));
                QString logoutString = "userIndex="+result.userIndex;
                request.setHeader(QNetworkRequest::ContentTypeHeader,QVariant("application/x-www-form-urlencoded; charset=UTF-8"));
                postManager->post(request,logoutString.toUtf8());
            }
            else{
                setWidgetVisible(true);
            }
        }
        else{ //已经登录
            ui->pbLogin->setText(LOGOUT);
            ui->pbLogin->setEnabled(true);
            this->setWidgetVisible(false);
        }
    }
}

void Widget::processLoginAndLogoutResult(){
    ui->pbLogin->setEnabled(true);
    if(ui->pbLogin->text() == LOGIN){
        if(result.result == "success"){
            ui->pbLogin->setText(LOGOUT);
            this->setWidgetVisible(false);
        }
        else{
            if(result.message.contains("您未绑定服务对应的运营商!")){
                applicationState = LoginWithOpeatorInfo;
                QString userId = ui->leUserID->text().trimmed();
                QString password = ui->lePassword->text().trimmed();
                QString loginString = this->makeLoginString(queryString,service,operatorUserId,operatorPwd);
                if(!loginString.isEmpty()){//如果结果不空，做登录操作
                    QNetworkRequest request(QUrl("http://webportal.scu.edu.cn/eportal/InterFace.do?method=login"));
                    postManager->post(request,loginString.toUtf8());
                }
            }else if(result.message.contains("验证码错误")){
                this->showError("提示","密码错误次数太多，请1分钟后重试");
//                显示验证码输入框
//                getValidCode();
            }else{
                this->showError("提示",result.message);
                this->setWidgetVisible(true);
            }
        }
    }else{
        this->setWidgetVisible(true);
        ui->pbLogin->setText(LOGIN);
        if(result.result != "success"){
            this->showError("提示",result.message);
        }
    }
}


void Widget::getValidCode(){
    //调用js脚本，获取随机数
    QString jsScript = "function getRandom() {  return Math.random(); }";
    QScriptEngine engine;
    QScriptValue js = engine.evaluate(jsScript);
    QScriptValue func = engine.globalObject().property("getRandom");
    QString ramdomNumber = func.call().toString();
    qDebug() << "result:" << ramdomNumber;
    //获取验证码
    QPixmap validCodeImage = FileDownloader::downloadPixmap(QUrl("http://webportal.scu.edu.cn/eportal/validcode?rnd=?"+ramdomNumber));
    ui->laValidCodeImage->setPixmap(validCodeImage);
    ui->laValidCodeImage->setVisible(true);
    ui->leValidCode->setVisible(true);
}

void Widget::processLogoutAllIPResult(){
    if(result.result == "success"){
        this->showInfo("提示",result.message);
//        this->setWidgetVisible(false);
    }
    else{
        this->showError("提示",result.message);
//        this->setWidgetVisible(true);
    }
}

/**
 * @brief Widget::isHostAccessible 指定ip和port是否可连接
 * @param ip
 * @param port
 * @return
 */
bool Widget::isHostAccessible(QString ip,uint port)
{
    QTcpSocket *s = new QTcpSocket(this);
    s->connectToHost(ip,port);
    bool accessible = s->waitForConnected(3000);
    s->disconnectFromHost();
    delete s;
    return accessible;
}

/**
 * @brief Widget::isHostAccessible 指定url的80端口是否可连接
 * @param url
 * @return
 */
bool Widget::isHostAccessible(QString url)
{
    QHostInfo info = QHostInfo::fromName(url);
    if(info.error() == QHostInfo::NoError){
        foreach(QHostAddress add,info.addresses()){
            if(add.protocol() == QAbstractSocket::IPv4Protocol){
                qDebug()<<add.toString();
                return this->isHostAccessible(add.toString(),80);
            }
        }
        return false;
    }else{
        mDebug(info.errorString());
        return false;
    }
}

/**
 * @brief Widget::readUserInfo 读取用户信息
 */
void Widget::readUserInfo(){
    //读取其他信息
    service = config->value("OperatorInfo/service","internet").toString();
    operatorUserId = config->value("OperatorInfo/operatorUserId","").toString();
    operatorPwd = config->value("OperatorInfo/operatorPwd","").toString();
    bool checked = config->value("Conf/checked",false).toBool();
    ui->cbSavePassword->setChecked(checked);
    //读取用户名
    QString userId = config->value("Info/userID","").toString();
    ui->leUserID->setText(userId);
    if(!checked){//如果保存密码没有被选中
        return;//停止读取密码
    }
    //读取密码文件加密次数times
    bool ok = false;
    int times = config->value("Info/times",-1).toUInt(&ok);
    if(!ok || times == -1){
        qsrand(QTime(0,0,0).msecsTo(QTime::currentTime()));
        uint times = 5 + qrand()%20;
        config->setValue("Info/times",times);
        return;
    }
    //读取密码
#ifdef Q_OS_WIN
        QFile file("libletitgo.dll");
#elif define(Q_OS_LINUX)
        QFile file("libletitgo.so");
#endif
    if(!file.open(QIODevice::ReadOnly)){
        mDebug("用户密码文件不存在");
        return;
    }
    QDataStream in(&file);
    in.setVersion(QDataStream::Qt_5_7);
    QByteArray pass;
    in>>pass;
    file.close();
    //解密
    for(int i=0;i<times;i++){
        pass = QByteArray::fromBase64(pass);
    }
    ui->lePassword->setText(QString(pass));
}

/**
 * @brief Widget::on_leUserID_textChanged
 * @param arg1
 */
void Widget::on_leUserID_textChanged(const QString &arg1)
{
    config->setValue("Info/userID",arg1);
}

/**
 * @brief Widget::on_cbSavePassword_clicked 保存用户密码
 * @param checked
 */
void Widget::on_cbSavePassword_clicked(bool checked)
{
    config->setValue("Conf/checked",checked);
    if(!checked){
        return;
    }else{
        //读取密码文件加密次数times
        bool ok = false;
        int times = config->value("Info/times",-1).toUInt(&ok);
        if(!ok || times == -1){//如果次数不存在
            qsrand(QTime(0,0,0).msecsTo(QTime::currentTime()));
            uint times = 5 + qrand()%20;
            config->setValue("Info/times",times);
            return;
        }
        //写入密码
#ifdef Q_OS_WIN
        QFile file("libletitgo.dll");
#elif define(Q_OS_LINUX)
        QFile file("libletitgo.so");
#endif
        if(!file.open(QIODevice::WriteOnly)){
            mDebug("写入文件失败");
            return;
        }
        QDataStream out(&file);
        out.setVersion(QDataStream::Qt_5_7);
        QByteArray pass = ui->lePassword->text().toUtf8();
        //解密
        for(int i=0;i<times;i++){
            pass = pass.toBase64();
        }
        out<<pass;
        file.close();
    }
}

/**
 * @brief Widget::on_lePassword_textChanged
 * @param arg1
 */
void Widget::on_lePassword_textChanged(const QString &arg1)
{
    Q_UNUSED(arg1)
    this->on_cbSavePassword_clicked(ui->cbSavePassword->isChecked());
}

/**
 * @brief Widget::setWidgetVisible 设置控件可见性
 * @param b
 */
void Widget::setWidgetVisible(bool b){
    ui->cbSavePassword->setVisible(b);
    ui->leUserID->setVisible(b);
    ui->lePassword->setVisible(b);
    if(b==true){
        ui->pbLogin->setVisible(true);
        ui->pbLogin->setEnabled(true);
    }
}

/**
 * @brief Widget::closeEvent 重新关闭事件
 * @param event
 */
void Widget::closeEvent(QCloseEvent *event){
    if(ui->pbLogin->text() == LOGOUT){
        this->hide();
        this->showMessageByTrayIcon("四川大学校园网客户端","我被隐藏了，请记得注销哦",3000);
        event->ignore();
    }
    else{
        event->accept();
    }
}

/**
 * @brief Widget::enterEvent 进入窗口事件
 * @param event
 */
void Widget::enterEvent(QEvent *event){
    ui->pbLogin->setVisible(true);
    event->accept();
}

/**
 * @brief Widget::leaveEvent 离开窗口事件
 * @param event
 */
void Widget::leaveEvent(QEvent *event){
    if(ui->pbLogin->text() == LOGOUT){
        ui->pbLogin->setVisible(false);
    }
    event->accept();
}

/**
 * @brief Widget::mousePressEvent 鼠标点击事件
 * @param event
 */
void Widget::mousePressEvent(QMouseEvent *event){
    Base::mousePressEvent(event);
    this->isButtonPressed = true;
}

/**
 * @brief Widget::mouseReleaseEvent 鼠标释放事件
 * @param event
 */
void Widget::mouseReleaseEvent(QMouseEvent *event){
    Base::mouseReleaseEvent(event);
    this->isButtonPressed = false;
}

/**
 * @brief Widget::mouseMoveEvent 鼠标移动事件
 * @param event
 */
void Widget::mouseMoveEvent(QMouseEvent *event){
    Base::mouseMoveEvent(event);
    event->accept();
}

/**
 * @brief Widget::createRightKeyMenu 创建右键菜单
 */
void Widget::createRightKeyMenu(){
    //QPixmap cb  = style()->standardPixmap(QStyle::SP_FileDialogStart);
    actionChangeBackground = new QAction("更改背景图片",this);
    actionChangeBackground->setIcon(QIcon(":/64px/Icons/picture_64px.ico"));
    connect(actionChangeBackground,SIGNAL(triggered(bool)),this,SLOT(changeBackground(bool)));

    actionShowUserInfo = new QAction("查看个人信息",this);
    actionShowUserInfo->setIcon(QIcon(":/64px/Icons/information_64px.ico"));
    connect(actionShowUserInfo,SIGNAL(triggered(bool)),this,SLOT(showUserInfo(bool)));

    if(config->value("Conf/autoLogin",false).toBool()){
        actionAutoLogin = new QAction("取消自动登录",this);
        QPixmap pixmap = style()->standardPixmap(QStyle::SP_DialogApplyButton);
        actionAutoLogin->setIcon(pixmap);
    }else{
        actionAutoLogin = new QAction("设为自动登录",this);
    }
    connect(actionAutoLogin,SIGNAL(triggered(bool)),this,SLOT(setAutoLogin(bool)));

    actionLogoutByUserIdAndPass = new QAction("下线所有设备",this);
    actionLogoutByUserIdAndPass->setIcon(QIcon(":/64px/Icons/logout_64X64.ico"));
    connect(actionLogoutByUserIdAndPass,SIGNAL(triggered(bool)),this,SLOT(logoutByUserIdAndPass(bool)));

    actionChooseService = new QAction("选择出口服务",this);
    actionChooseService->setIcon(QIcon(":/64px/Icons/service_64.ico"));
    connect(actionChooseService,SIGNAL(triggered(bool)),this,SLOT(chooseService(bool)));

    this->addAction(actionAutoLogin);
    this->addAction(actionLogoutByUserIdAndPass);
    this->addAction(actionChangeBackground);
    this->addAction(actionShowUserInfo);
    this->addAction(actionChooseService);
    this->setContextMenuPolicy(Qt::ActionsContextMenu);
}

/**
 * @brief Widget::showUserInfo 显示用户信息
 * @param checked
 */
void Widget::showUserInfo(bool checked){
    Q_UNUSED(checked)
    if(!this->isHostAccessible("192.168.2.135",80)){
        this->showError("提示","登录服务器不可达，请检查网络是否连接好");
        ui->pbLogin->setText(LOGIN);
        this->setWidgetVisible(true);
        return;
    }
    if(ui->pbLogin->text() == LOGOUT){
        userInfo->setUserIndex(result.userIndex);
    }
    else{
        this->showError("提示","尚未登录,不可查看用户信息");
    }
}

/**
 * @brief Widget::setAutoLogin 设置是否自动登录
 * @param checked
 */

void Widget::setAutoLogin(bool checked){
    Q_UNUSED(checked)
    if(actionAutoLogin->icon().isNull()){
        QPixmap pixmap = style()->standardPixmap(QStyle::SP_DialogApplyButton);
        actionAutoLogin->setIcon(pixmap);
        config->setValue("Conf/autoLogin",true);
        actionAutoLogin->setText("取消自动登录");
    }
    else{
        actionAutoLogin->setIcon(QIcon());
        config->setValue("Conf/autoLogin",false);
        actionAutoLogin->setText("设为自动登录");
    }
}

void Widget::logoutByUserIdAndPass(bool checked){
    Q_UNUSED(checked);
    QString userId = ui->leUserID->text().trimmed();
    if(userId.isEmpty()){
        this->showWarning("警告","用户名为空");
        ui->leUserID->setFocus();
        return;
    }
    QString pass = ui->lePassword->text().trimmed();
    if(pass.isEmpty()){
        this->showWarning("警告","密码为空");
        ui->lePassword->setFocus();
        return;
    }
    applicationState = LogoutAllIP;
    QNetworkRequest request(QUrl("http://webportal.scu.edu.cn/eportal/InterFace.do?method=logoutByUserIdAndPass"));
    QString logoutString = "userId="+userId+"&pass="+pass;
    postManager->post(request,logoutString.toUtf8());
}

void Widget::chooseService(bool checked){
    Q_UNUSED(checked);
    serviceManager->show();
}

/**
 * @brief Widget::keyPressEvent 监听键盘，当回车键被点击时，执行按钮被点击时的操作
 * @param event
 */
void Widget::keyPressEvent(QKeyEvent *event){
    if(event->key() == Qt::Key_Return){
        on_pbLogin_clicked();
    }
    event->accept();
}

void Widget::operatorInfoUpdated(QString service, QString id, QString pwd){
    this->service = service;
    operatorUserId = id;
    operatorPwd = pwd;
}
