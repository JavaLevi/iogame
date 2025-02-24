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
package com.iohao.game.action.skeleton.core.flow.parser;

import com.iohao.game.action.skeleton.core.ActionCommand;
import com.iohao.game.action.skeleton.core.DataCodecKit;
import com.iohao.game.action.skeleton.protocol.wrapper.IntValue;
import com.iohao.game.action.skeleton.protocol.wrapper.IntValueList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * int 包装类解析器
 *
 * @author 渔民小镇
 * @date 2023-02-10
 */
final class IntValueMethodParser implements MethodParser {

    @Override
    public Class<?> getActualClazz(ActionCommand.MethodParamResultInfo methodParamResultInfo) {
        return methodParamResultInfo.isList() ? IntValueList.class : IntValue.class;
    }

    @Override
    public Object parseParam(byte[] data, ActionCommand.ParamInfo paramInfo) {

        if (paramInfo.isList()) {

            if (Objects.isNull(data)) {
                return new ArrayList<Integer>();
            }

            var valueList = DataCodecKit.decode(data, IntValueList.class);
            return valueList.values;
        }

        if (Objects.isNull(data)) {
            return 0;
        }

        var intValue = DataCodecKit.decode(data, IntValue.class);
        return intValue.value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object parseResult(ActionCommand.ActionMethodReturnInfo actionMethodReturnInfo, Object methodResult) {

        if (actionMethodReturnInfo.isList()) {
            var valueList = new IntValueList();
            valueList.values = (List<Integer>) methodResult;
            return valueList;
        }

        /*
         * 将结果转换为 IntValue；
         * 注意这里不会检测 methodResult 是否为 null，如果担心 null 问题，
         * 可以使用 int，而不是使用 Integer
         */
        var intValue = new IntValue();
        intValue.value = (int) methodResult;
        return intValue;
    }

    private IntValueMethodParser() {
    }

    public static IntValueMethodParser me() {
        return Holder.ME;
    }

    /** 通过 JVM 的类加载机制, 保证只加载一次 (singleton) */
    private static class Holder {
        static final IntValueMethodParser ME = new IntValueMethodParser();
    }
}
