/**
 * Tencent is pleased to support the open source community by making Tars available.
 *
 * Copyright (C) 2016 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.qq.tars.support.notify;

import com.qq.tars.client.Communicator;
import com.qq.tars.server.config.ConfigurationManager;
import com.qq.tars.support.log.LoggerFactory;
import com.qq.tars.support.notify.prx.NOTIFYLEVEL;
import com.qq.tars.support.notify.prx.NotifyPrx;
import com.qq.tars.support.notify.prx.ReportInfo;
import org.slf4j.Logger;

public class NotifyHelper {

    private static final Logger omLogger = LoggerFactory.getOmLogger();
    private static final NotifyHelper Instance = new NotifyHelper();
    private Communicator communicator;
    private String app;
    private String server;

    public static NotifyHelper getInstance() {
        return Instance;
    }

    public int setNotifyInfo(Communicator comm, String app, String server) {
        this.communicator = comm;
        this.app = app;
        this.server = server;
        return 0;
    }

    private void notify(NOTIFYLEVEL level, String message) {
        try {
            if (communicator == null) {
                return;
            }
             report(  message, level,false);
        } catch (Exception e) {
            omLogger.error("RemoteNotify|notify error", e);
        }
    }

    public void notifyNormal(String info) {
        notify(NOTIFYLEVEL.NOTIFYNORMAL, info);
    }

    public void notifyWarn(String info) {
        notify(NOTIFYLEVEL.NOTIFYWARN, info);
    }

    public void notifyError(String info) {
        notify(NOTIFYLEVEL.NOTIFYERROR, info);
    }

    public void syncReport(String result) {
        report(result,NOTIFYLEVEL.NOTIFYNORMAL, true);
    }

    public void asyncReport(String result) {
        report(result, NOTIFYLEVEL.NOTIFYNORMAL,false);
    }

    private void report(String result,final NOTIFYLEVEL notifylevel,  final Boolean sync) {
        try {
            if (communicator == null) {
                return;
            }
            final NotifyPrx notifyPrx = communicator.stringToProxy(NotifyPrx.class, ConfigurationManager.getInstance().getServerConfig().getNotify());
            final ReportInfo reportInfo  = new ReportInfo();
            reportInfo.setSThreadId(String.valueOf(Thread.currentThread().getId()));
            reportInfo.setSServer(app + "." + server );
            reportInfo.setSMessage( result);
            reportInfo.setELevel(notifylevel.value());
            if(sync){
                notifyPrx.reportNotifyInfo(reportInfo);
            }else{
                notifyPrx.async_reportNotifyInfo( null, reportInfo);
            }
        } catch (Exception e) {
            omLogger.error("RemoteNotify|report error", e);
        }
    }

}
