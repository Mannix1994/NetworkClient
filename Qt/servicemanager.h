#ifndef SERVICEMANAGER_H
#define SERVICEMANAGER_H

#include <QWidget>
#include "base.h"

namespace Ui {
class ServiceManager;
}

class ServiceManager : public Base
{
    Q_OBJECT

public:
    explicit ServiceManager(Base *parent = 0);
    ~ServiceManager();

private slots:
    void on_cmbServiceNames_currentIndexChanged(int index);

    void on_pbConfirm_clicked();

private:
    Ui::ServiceManager *ui;

    void initialize();
    void setControlsEnable(bool b);
signals:
    void operatorInfoUpdated(QString service,QString id,QString pwd); //用户的出口账号和密码更新了
};

#endif // SERVICEMANAGER_H
