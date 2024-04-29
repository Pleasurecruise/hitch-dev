package com.heima.notice.socket;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.heima.commons.constant.HtichConstants;
import com.heima.commons.domin.vo.response.ResponseVO;
import com.heima.commons.entity.SessionContext;
import com.heima.commons.enums.BusinessErrors;
import com.heima.commons.helper.RedisSessionHelper;
import com.heima.commons.utils.SpringUtil;
import com.heima.modules.po.StrokePO;
import com.heima.modules.vo.NoticeVO;
import com.heima.notice.handler.NoticeHandler;
import com.heima.notice.service.StrokeAPIService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//TODO:任务5.1-完成websocket开发-2day
@Component
@ServerEndpoint(value = "/ws/socket")
public class WebSocketServer {
    //Websocket用户链接池
    //concurrent包的线程安全Map，用来存放每个客户端对应的WebSocketServer对象。
    //key是accountId，可以通过本类中的getAccountId方法获取到，value是session
    public final static Map<String, Session> sessionPools = new ConcurrentHashMap<>();
    /*
        用户发送ws消息，message为json格式{'receiverId':'接收人','tripId':'行程id','message':'消息内容'}
    */
    @OnMessage
    public void onMessage(Session session, String message) {
        String accountId = getAccountId(session);
        //
        JSONObject jsonObject = JSON.parseObject(message);
        String tripId = jsonObject.getString("tripId");
        String messageContent = jsonObject.getString("message");
        //
        StrokeAPIService strokeAPIService = SpringUtil.getBean(StrokeAPIService.class);
        StrokePO strokePO = strokeAPIService.selectByID(tripId);
        String receiverId = strokePO.getPublisherId();
        //设置相关消息内容并存入mongodb：noticeHandler.saveNotice(noticeVO);
        NoticeVO noticeVO = new NoticeVO();
        noticeVO.setSenderId(accountId);
        noticeVO.setReceiverId(receiverId);
        noticeVO.setTripId(tripId);
        noticeVO.setMessage(messageContent);
        NoticeHandler noticeHandler = SpringUtil.getBean(NoticeHandler.class);
        noticeHandler.saveNotice(noticeVO);
    }

    /**
     * 连接建立成功调用
     *
     * @param session 客户端与socket建立的会话
     * @param session 客户端的userId
     */
    @OnOpen
    public void onOpen(Session session) {

    }

    /**
     * 关闭连接时调用
     *
     * @param session 关闭连接的客户端的姓名
     */
    @OnClose
    public void onClose(Session session) {

    }


    /**
     * 发生错误时候
     *
     * @param session
     * @param throwable
     */
    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("发生错误");
        throwable.printStackTrace();
    }



    /*
    * 在当前session中获取用户accoutId
    * */
    private String getAccountId(Session session) {
        String token = null;
        Map<String, List<String>> paramMap = session.getRequestParameterMap();
        List<String> paramList = paramMap.get(HtichConstants.SESSION_TOKEN_KEY);
        if (paramList!=null && paramList.size() != 0){
            token = paramList.get(0);
        }
        RedisSessionHelper redisSessionHelper = SpringUtil.getBean(RedisSessionHelper.class);
        if (null == redisSessionHelper) {
            return null;
        }
        SessionContext context = redisSessionHelper.getSession(token);
        boolean isisValid = redisSessionHelper.isValid(context);
        if (isisValid) {
            return context.getAccountID();
        }
        return null;
    }

}