/*
 * # iohao.com . 渔民小镇
 * Copyright (C) 2021 - 2023 double joker （262610965@qq.com） . All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.iohao.game.bolt.broker.server.processor;

import com.alipay.remoting.AsyncContext;
import com.alipay.remoting.BizContext;
import com.alipay.remoting.Connection;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.rpc.RpcServer;
import com.iohao.game.action.skeleton.core.exception.ActionErrorEnum;
import com.iohao.game.action.skeleton.protocol.HeadMetadata;
import com.iohao.game.action.skeleton.protocol.RequestMessage;
import com.iohao.game.action.skeleton.protocol.ResponseMessage;
import com.iohao.game.bolt.broker.core.common.AbstractAsyncUserProcessor;
import com.iohao.game.bolt.broker.core.common.IoGameGlobalConfig;
import com.iohao.game.bolt.broker.server.BrokerServer;
import com.iohao.game.bolt.broker.server.aware.BrokerServerAware;
import com.iohao.game.bolt.broker.server.balanced.BalancedManager;
import com.iohao.game.bolt.broker.server.balanced.ExternalBrokerClientLoadBalanced;
import com.iohao.game.bolt.broker.server.balanced.LogicBrokerClientLoadBalanced;
import com.iohao.game.bolt.broker.server.balanced.region.BrokerClientProxy;
import com.iohao.game.bolt.broker.server.balanced.region.BrokerClientRegion;
import com.iohao.game.common.kit.log.IoGameLoggerFactory;
import lombok.Setter;
import org.slf4j.Logger;

/**
 * 对外服务器消息处理
 * <pre>
 *     接收真实用户的请求，把请求转发到逻辑服
 * </pre>
 *
 * @author 渔民小镇
 * @date 2022-05-14
 */
public class ExternalRequestMessageBrokerProcessor extends AbstractAsyncUserProcessor<RequestMessage>
        implements BrokerServerAware {
    static final Logger log = IoGameLoggerFactory.getLoggerMsg();

    @Setter
    BrokerServer brokerServer;

    @Override
    public void handleRequest(BizContext bizCtx, AsyncContext asyncCtx, RequestMessage request) {
        if (IoGameGlobalConfig.requestResponseLog) {
            extractedPrint(request);
        }

        // 逻辑服的负载均衡
        BalancedManager balancedManager = brokerServer.getBalancedManager();
        LogicBrokerClientLoadBalanced loadBalanced = balancedManager.getLogicBalanced();

        // 得到路由对应的逻辑服区域
        HeadMetadata headMetadata = request.getHeadMetadata();
        int cmdMerge = headMetadata.getCmdMerge();
        BrokerClientRegion brokerClientRegion = loadBalanced.getBoltClientRegion(cmdMerge);

        if (brokerClientRegion == null) {
            //  通知对外服， 路由不存在
            extractedNotRoute(bizCtx, request);
            return;
        }

        // 从逻辑服区域得到一个逻辑服来处理请求
        BrokerClientProxy brokerClientProxy = brokerClientRegion.getBoltClientProxy(headMetadata);
        if (brokerClientProxy == null) {
            //  通知对外服， 路由不存在
            extractedNotRoute(bizCtx, request);
            return;
        }

        try {
            brokerClientProxy.oneway(request);
        } catch (RemotingException | InterruptedException | NullPointerException e) {
            log.error(e.getMessage(), e);
        }

    }

    private void extractedPrint(RequestMessage request) {

        log.info("游戏网关把对外服 请求 转发到逻辑服 : {}", request);

        BalancedManager balancedManager = brokerServer.getBalancedManager();
        ExternalBrokerClientLoadBalanced externalLoadBalanced = balancedManager.getExternalLoadBalanced();

        for (BrokerClientProxy brokerClientProxy : externalLoadBalanced.listBrokerClientProxy()) {
            log.info("brokerClientProxy : {}", brokerClientProxy);
        }
    }

    private void extractedNotRoute(BizContext bizCtx, RequestMessage requestMessage) {
        // 路由不存在
        Connection connection = bizCtx.getConnection();
        ResponseMessage responseMessage = requestMessage.createResponseMessage();

        ActionErrorEnum errorCode = ActionErrorEnum.cmdInfoErrorCode;
        responseMessage.setValidatorMsg(errorCode.getMsg())
                .setResponseStatus(errorCode.getCode());

        RpcServer rpcServer = brokerServer.getRpcServer();

        try {
            rpcServer.oneway(connection, responseMessage);
        } catch (RemotingException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 指定感兴趣的请求数据类型，该 UserProcessor 只对感兴趣的请求类型的数据进行处理；
     * 假设 除了需要处理 MyRequest 类型的数据，还要处理 java.lang.String 类型，有两种方式：
     * 1、再提供一个 UserProcessor 实现类，其 interest() 返回 java.lang.String.class.getName()
     * 2、使用 MultiInterestUserProcessor 实现类，可以为一个 UserProcessor 指定 List<String> multiInterest()
     *
     * @return 自定义处理器
     */
    @Override
    public String interest() {
        return RequestMessage.class.getName();
    }
}
