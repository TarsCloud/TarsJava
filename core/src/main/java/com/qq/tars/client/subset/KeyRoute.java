package com.qq.tars.client.subset;


import com.qq.tars.common.util.DyeingSwitch;
import com.qq.tars.context.DistributedContext;
import com.qq.tars.rpc.protocol.tars.TarsServantRequest;
import java.util.HashMap;


public class KeyRoute {

    private String action = null;
    private String value = null;
    private String route = null;

    public static final String TARS_ROUTE_KEY = "TARS_ROUTE_KEY";


    //根据分布式上下文信息获取KeyRoute
    public static String getRouteKey(DistributedContext distributedContext){
        String routeValue = "";
        if(distributedContext != null){
            TarsServantRequest tarsServantRequest = distributedContext.get(DyeingSwitch.REQ);
            routeValue = tarsServantRequest.getStatus().get(TARS_ROUTE_KEY);
        }
        return routeValue;
    }

    //根据分布式上下文信息设置KeyRoute
    public static void setRouteKey(DistributedContext distributedContext, String routeKey){

        if(distributedContext != null && routeKey != null ){
            TarsServantRequest tarsServantRequest = distributedContext.get(DyeingSwitch.REQ);
            tarsServantRequest.getStatus().put(TARS_ROUTE_KEY, routeKey);

        }

    }

    public static void setRouteKeyToRequest(DistributedContext distributedContext, TarsServantRequest request){
        String routeValue = KeyRoute.getRouteKey(distributedContext);
        if( routeValue != null && !"".equals(routeValue)){
            if(request.getStatus() != null){
                request.getStatus().put(KeyRoute.TARS_ROUTE_KEY ,routeValue);
            } else {
                HashMap<String, String> status = new HashMap<>();
                status.put(KeyRoute.TARS_ROUTE_KEY ,routeValue);
                request.setStatus(status);
            }

        }
    }

    //将分布式上下文信息的routeValue 设置到KeyRoute.value
    public void setValue(DistributedContext distributedContext){
        String routeKey = getRouteKey(distributedContext);
        if( !"".equals(routeKey) && routeKey != null){
            this.value = routeKey;
        }
    }

    public KeyRoute() {
    }

    public KeyRoute(String action, String value, String route) {
        this.action = action;
        this.value = value;
        this.route = route;
    }

    public String getValue() {
        return value;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }
}
