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
package com.iohao.game.widget.light.domain.event.disruptor;

import com.iohao.game.widget.light.domain.event.message.Topic;

/**
 * 领域事件接口 - 源事件源
 *
 * @author 渔民小镇
 * @date 2021-12-26
 */
public interface DomainEventSource extends Topic {
    /**
     * 获取领域事件主题
     *
     * @return 领域事件主题
     */
    @Override
    default Class<?> getTopic() {
        return this.getClass();
    }

    /**
     * 获取事件源
     *
     * @param <T> source
     * @return 事件源
     */
    @SuppressWarnings("unchecked")
    default <T> T getSource() {
        return (T) this;
    }
}
