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
package com.iohao.game.bolt.broker.client;

import com.alipay.remoting.ConnectionEventType;
import com.iohao.game.action.skeleton.core.BarSkeleton;
import com.iohao.game.bolt.broker.client.processor.*;
import com.iohao.game.bolt.broker.client.processor.connection.CloseConnectEventClientProcessor;
import com.iohao.game.bolt.broker.client.processor.connection.ConnectEventClientProcessor;
import com.iohao.game.bolt.broker.client.processor.connection.ConnectFailedEventClientProcessor;
import com.iohao.game.bolt.broker.client.processor.connection.ExceptionConnectEventClientProcessor;
import com.iohao.game.bolt.broker.core.client.BrokerAddress;
import com.iohao.game.bolt.broker.core.client.BrokerClientBuilder;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.util.Objects;

/**
 * 逻辑服抽象类
 *
 * @author 渔民小镇
 * @date 2022-05-14
 */
@Setter
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract non-sealed class AbstractBrokerClientStartup implements BrokerClientStartup {
    /** 连接 broker （游戏网关） 的地址 */
    BrokerAddress brokerAddress;
    /** 业务框架 */
    BarSkeleton barSkeleton;
    /**
     * BoltBrokerClient 构建器
     * <pre>
     *     如果字段赋值了
     *          就不会使用 {@link BrokerClientStartup#createBrokerClientBuilder()} 接口的值
     *
     *     如果字段没有赋值
     *          就会使用 {@link BrokerClientStartup#createBrokerClientBuilder()} 接口的值
     * </pre>
     */
    BrokerClientBuilder brokerClientBuilder;

    @Override
    public void connectionEventProcessor(BrokerClientBuilder brokerClientBuilder) {
        brokerClientBuilder
                .addConnectionEventProcessor(ConnectionEventType.CONNECT, ConnectEventClientProcessor::new)
                .addConnectionEventProcessor(ConnectionEventType.CLOSE, CloseConnectEventClientProcessor::new)
                .addConnectionEventProcessor(ConnectionEventType.CONNECT_FAILED, ConnectFailedEventClientProcessor::new)
                .addConnectionEventProcessor(ConnectionEventType.EXCEPTION, ExceptionConnectEventClientProcessor::new);
    }

    @Override
    public void registerUserProcessor(BrokerClientBuilder brokerClientBuilder) {
        brokerClientBuilder
                // 收到网关请求模块信息
                .registerUserProcessor(RequestBrokerClientModuleMessageClientProcessor::new)
                // broker （游戏网关）集群处理
                .registerUserProcessor(BrokerClusterMessageClientProcessor::new)
                // 接收扩展逻辑服的消息
                .registerUserProcessor(ExtRequestMessageClientProcessor::new)
                // 业务请求处理器
                .registerUserProcessor(RequestMessageClientProcessor::new)
                .registerUserProcessor(SyncRequestMessageClientProcessor::new)
        ;
    }

    /**
     * 初始化一些配置到构建器中
     * <pre>
     *     这个方法的目的在于，先设置一些配置到 builder 中，后续有需要修改的部分配置在单独到 builder 中设置
     * </pre>
     *
     * @return BoltBrokerClientBuilder
     */
    BrokerClientBuilder initConfig() {
        // 业务框架
        this.barSkeleton = this.createBarSkeleton();
        // 连接到游戏网关的地址
        this.brokerAddress = this.createBrokerAddress();
        // 构建器
        if (Objects.isNull(this.brokerClientBuilder)) {
            this.brokerClientBuilder = this.createBrokerClientBuilder();
        }

        Objects.requireNonNull(this.brokerClientBuilder, "brokerClient 构建器必须要有");

        // 设置 config 配置信息到 BoltBrokerClientBuilder 中
        this.brokerClientBuilder
                .barSkeleton(this.barSkeleton)
                .brokerAddress(this.brokerAddress);

        // 添加连接处理器
        this.connectionEventProcessor(this.brokerClientBuilder);
        // 注册用户处理器
        this.registerUserProcessor(this.brokerClientBuilder);

        // 实验性功能
        experiment();

        return this.brokerClientBuilder;
    }

    /**
     * 实验性功能，将来可能移除的。
     */
    private void experiment() {
//        ExtRegions.me().add(new MonitorExtRegion());
    }
}
