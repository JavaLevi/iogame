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
package com.iohao.game.bolt.broker.core.kit;

import com.alipay.remoting.exception.CodecException;
import com.alipay.remoting.serialization.HessianSerializer;
import com.iohao.game.common.kit.log.IoGameLoggerFactory;
import lombok.experimental.UtilityClass;
import org.slf4j.Logger;

/**
 * @author 渔民小镇
 * @date 2023-01-18
 */
@UtilityClass
public class HessianKit {
    final Logger log = IoGameLoggerFactory.getLoggerCommonStdout();
    final HessianSerializer hessianSerializer = new HessianSerializer();

    public byte[] serialize(Object obj) {
        try {
            return hessianSerializer.serialize(obj);
        } catch (CodecException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public <T> T deserialize(byte[] data, Class<T> classOfT) {
        try {
            return hessianSerializer.deserialize(data, "");
        } catch (CodecException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
}
