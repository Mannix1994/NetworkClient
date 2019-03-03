#include "userinfo.h"
#include "ui_userinfo.h"

UserInfo::UserInfo(Base *parent) :
    Base(parent),ui(new Ui::UserInfo),rowCount(0)
{
    ui->setupUi(this);
    this->configWindow();
    this->initialize();
}

UserInfo::~UserInfo()
{
    delete ui;
}

/**
 * @brief UserInfo::initialize 初始化
 */
void UserInfo::initialize(){
    Base::initialize();
    postManager = new QNetworkAccessManager(this);
    connect(postManager,SIGNAL(finished(QNetworkReply*)),this,SLOT(postManagerReadyRead(QNetworkReply*)));
    //设置编码
    codec = QTextCodec::codecForName("utf-8");

    //初始化表格
    model = new QStandardItemModel();
    model->setColumnCount(2);
    model->setHeaderData(0,Qt::Horizontal,"项");
    model->setHeaderData(1,Qt::Horizontal,"值");

    ui->tvUserInfo->setModel(model);
    ui->tvUserInfo->setColumnWidth(0,80);
    ui->tvUserInfo->setColumnWidth(1,80);
    ui->tvUserInfo->setStyleSheet("color:black");
    ui->tvUserInfo->setEditTriggers(QAbstractItemView::NoEditTriggers);
    ui->tvUserInfo->horizontalHeader()->setStretchLastSection(true);
}

/**
 * @brief UserInfo::setUserIndex 设置用户的userIndex，并向服务器请求用户信息
 * @param userIndex
 */
void UserInfo::setUserIndex(QString userIndex){
    this->userIndex = userIndex;
    QNetworkRequest request(QUrl("http://webportal.scu.edu.cn/eportal/InterFace.do?method=getOnlineUserInfo"));
    QString data = "userIndex="+userIndex;
    postManager->post(request,data.toUtf8());
}

/**
 * @brief UserInfo::postManagerReadyRead 接收用户信息并处理
 * @param reply
 */
void UserInfo::postManagerReadyRead(QNetworkReply *reply){
    if(reply->error() == QNetworkReply::NoError){
        QByteArray bytes = reply->readAll();
        QString result = codec->toUnicode(bytes);
        if(!result.isEmpty()){ //登录或注销操作的结果不空，解析结果并处理结果
            if(this->parseJson(bytes)){//解析
                this->processResult();//处理
            }
        }else{
            this->showError("提示","获取用户信息失败");
            mDebug("获取用户信息失败");
        }
    }
    else{
        mDebug(reply->errorString());
        this->showError("提示","获取用户信息失败\n"+reply->errorString());
    }
    reply->deleteLater();
}

/**
 * @brief UserInfo::parseJson 处理收到的Json数据，从中提取出想要的数据
 * @param json
 * @return
 */
bool UserInfo::parseJson(QByteArray json){
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
            if(jsonObj.contains("userName")){
                result.userName = jsonObj.take("userName").toString();
            }
            if(jsonObj.contains("userId")){
                result.userId = jsonObj.take("userId").toString();
            }
            if(jsonObj.contains("userIp")){
                result.userIp = jsonObj.take("userIp").toString();
            }
            if(jsonObj.contains("portalIp")){
                result.portalIp = jsonObj.take("portalIp").toString();
            }
            if(jsonObj.contains("userGroup")){
                result.userGroup = jsonObj.take("userGroup").toString();
            }
            if(jsonObj.contains("accountFee")){
                result.accountFee = jsonObj.take("accountFee").toString();
            }
            if(jsonObj.contains("userPackage")){
                result.userPackage = jsonObj.take("userPackage").toString();
            }
            if(jsonObj.contains("maxLeavingTime")){
                result.maxLeavingTime = jsonObj.take("maxLeavingTime").toString();
            }
            if(jsonObj.contains("notify")){
                result.notify = jsonObj.take("notify").toString();
            }
            if(jsonObj.contains("welcomeTip")){
                result.welcomeTip = jsonObj.take("welcomeTip").toString();
            }
            if(jsonObj.contains("isErrorMsg")){
                result.isErrorMsg = jsonObj.take("isErrorMsg").toString();
            }
        }else{
            mDebug("not a json object");
            return false;
        }
    }
    else{
        mDebug(error.errorString());
        return false;
    }
    return true;
}

/**
 * @brief UserInfo::processResult 处理解析后的数据
 */
void UserInfo::processResult(){
    if(result.result == "wait"){//服务器尚未准备好信息，休眠200ms后重新请求用户信息
        QTimer::singleShot(200,this,SLOT(reload()));
        return;
    }else{
        this->refreshTable();
    }
}

/**
 * @brief UserInfo::reload 重新请求用户信息
 */
void UserInfo::reload(){
    this->setUserIndex(this->userIndex); //重新载入信息
}

/**
 * @brief UserInfo::refreshTable 刷新表的信息，并显示
 */
void UserInfo::refreshTable(){
    rowCount = 0;
    model->removeRows(0,model->rowCount());
    this->insertToTable("用户名",result.userName);
    this->insertToTable("用户ID",result.userId);
    this->insertToTable("用户IP",result.userIp);
    this->insertToTable("用户组",result.userGroup);
    this->insertToTable("服务器IP",result.portalIp);
    this->insertToTable("当前费用",result.accountFee);
    this->insertToTable("当前套餐",result.userPackage);
    this->insertToTable("剩余时间",result.maxLeavingTime);

    this->show();
}

/**
 * @brief UserInfo::insertToTable 想表中插入数据
 * @param key
 * @param value
 */
void UserInfo::insertToTable(QString key, QString value){
    model->setItem(rowCount,0,new QStandardItem(key));
    model->setItem(rowCount,1,new QStandardItem(value));
    rowCount++;
}
