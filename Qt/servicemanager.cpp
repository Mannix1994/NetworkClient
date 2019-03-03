#include "servicemanager.h"
#include "ui_servicemanager.h"

ServiceManager::ServiceManager(Base *parent) :
    Base(parent),
    ui(new Ui::ServiceManager)
{
    ui->setupUi(this);
    this->configWindow();
    this->initialize();
}

ServiceManager::~ServiceManager()
{
    delete ui;
}

void ServiceManager::initialize(){
    Base::initialize();
    ui->leOperatorUserId->setPlaceholderText("密码");
    ui->leOperatorPwd->setPlaceholderText("密码");
    if(ui->cmbServiceNames->currentIndex()==0){
        setControlsEnable(false);
    }
    ui->cmbServiceNames->setCurrentIndex(config->value("OperatorInfo/currentIndex",0).toInt());
    ui->leOperatorUserId->setText(config->value("OperatorInfo/operatorUserId","").toString());
    ui->leOperatorPwd->setText(config->value("OperatorInfo/operatorPwd","").toString());
}

void ServiceManager::on_cmbServiceNames_currentIndexChanged(int index)
{
    switch (index) {
    case 0:
        ui->leOperatorUserId->setPlaceholderText("账号");
        ui->leOperatorUserId->clear();
        ui->leOperatorPwd->clear();
        setControlsEnable(false);
        break;
    case 1:
        ui->leOperatorUserId->setPlaceholderText("移动账号");
        setControlsEnable(true);
        break;
    case 2:
        ui->leOperatorUserId->setPlaceholderText("联通账号");
        setControlsEnable(true);
        break;
    default:
        break;
    }
}

void ServiceManager::setControlsEnable(bool b){
    ui->leOperatorUserId->setEnabled(b);
    ui->leOperatorPwd->setEnabled(b);
}

void ServiceManager::on_pbConfirm_clicked()
{
    QString id = ui->leOperatorUserId->text().trimmed();
    QString pwd = ui->leOperatorPwd->text().trimmed();
    if(ui->cmbServiceNames->currentIndex() != 0){
        if(id.isEmpty()){
            this->showError("错误","未填写账号");
            return;
        }
        if(pwd.isEmpty()){
            this->showError("错误","未填写密码");
            return;
        }
    }
    QString service = ui->cmbServiceNames->currentText();
    if(service == "校园网"){
        service = "internet";
    }else if(service == "中国移动"){
        service = QByteArray("移动出口").toPercentEncoding().toPercentEncoding();
    }else if(service == "中国联通"){
        service = QByteArray("联通出口").toPercentEncoding().toPercentEncoding();
    }
    config->setValue("OperatorInfo/operatorUserId",id);
    config->setValue("OperatorInfo/operatorPwd",pwd);
    config->setValue("OperatorInfo/service",service);
    config->setValue("OperatorInfo/currentIndex",ui->cmbServiceNames->currentIndex());
    emit operatorInfoUpdated(service,id,pwd);
    this->close();
}
