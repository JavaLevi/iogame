/*
 * # iohao.com . 渔民小镇
 * Copyright (C) 2021 - 2023 double joker （262610965@qq.com） . All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License..
 */
package com.iohao.game.action.skeleton.core;

import com.iohao.game.action.skeleton.core.flow.FlowContext;

/**
 * 业务框架处理器
 *
 * @author 渔民小镇
 * @date 2021-12-20
 */
public interface Handler {
    /**
     * 处理一个action请求
     *
     * @param flowContext flowContext
     * @return 如果返回 false 就不交给下一个链进行处理. 全剧终了.
     */
    boolean handler(FlowContext flowContext);
}