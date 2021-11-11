package com.wyc.cloudapp.application.syncinstance

abstract class AbstractFactory {
    protected abstract fun createImp(p:Any): ISync
}