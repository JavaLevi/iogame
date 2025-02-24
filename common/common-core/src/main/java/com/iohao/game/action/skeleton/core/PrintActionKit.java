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

import com.iohao.game.action.skeleton.IoGameVersion;
import com.iohao.game.action.skeleton.core.codec.DataCodec;
import com.iohao.game.action.skeleton.core.flow.ActionMethodInOut;
import com.iohao.game.common.kit.ArrayKit;
import com.iohao.game.common.kit.StrKit;
import lombok.experimental.UtilityClass;
import org.fusesource.jansi.Ansi;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 打印 action
 * <BR>
 *
 * @author 渔民小镇
 * @date 2021-12-12
 */
@UtilityClass
class PrintActionKit {

    void print(BarSkeleton barSkeleton, BarSkeletonSetting setting) {

        if (!setting.print) {
            return;
        }

        if (setting.isPrintHandler()) {
            var list = List.of(barSkeleton.getHandlers());
            PrintActionKit.printHandler(list);
        }

        if (setting.isPrintInout()) {
            var list = List.of(barSkeleton.inOutManager.getInOuts());
            PrintActionKit.printInout(list);
        }

        if (setting.isPrintDataCodec()) {
            PrintActionKit.printDataCodec();
        }

        if (setting.isPrintAction()) {
            PrintActionKit.printActionCommand(barSkeleton.actionCommandRegions.actionCommands, setting.printActionShort);
        }

        System.out.println();
    }

    /**
     * 打印 inout
     *
     * @param inOuts inOuts
     */
    void printInout(List<ActionMethodInOut> inOuts) {
        String title = "@|CYAN ======================== InOut ========================= |@";
        System.out.println(Ansi.ansi().render(title));
        System.out.println("如果需要关闭打印, 查看 BarSkeletonBuilder#setting#printInout");

        for (ActionMethodInOut inOut : inOuts) {
            String info = String.format("@|BLUE %s |@", inOut.getClass());
            System.out.println(Ansi.ansi().render(info));
        }
    }

    void printHandler(List<Handler> handlers) {
        String iohaoTitle = "@|CYAN ======================== 业务框架 iohao ========================= |@";
        System.out.println(Ansi.ansi().render(iohaoTitle));
        System.out.println(IoGameVersion.VERSION);
        String colorStr = "@|BLACK BLACK|@ @|RED RED|@ @|GREEN GREEN|@ @|YELLOW YELLOW|@ @|BLUE BLUE|@ @|MAGENTA MAGENTA|@ @|CYAN CYAN|@ @|WHITE WHITE|@ @|DEFAULT DEFAULT|@";
        System.out.println(Ansi.ansi().render(colorStr));

        String title = "@|CYAN ======================== Handler ========================= |@";
        System.out.println(Ansi.ansi().render(title));
        System.out.println("如果需要关闭打印, 查看 BarSkeletonBuilder#setting#printHandler");

        for (Handler handler : handlers) {
            String info = String.format("@|BLUE %s |@", handler.getClass());
            System.out.println(Ansi.ansi().render(info));
        }
    }

    void printActionCommand(ActionCommand[][] behaviors, boolean shortName) {
        String title = "@|CYAN ======================== action ========================= |@";
        System.out.println(Ansi.ansi().render(title));

        String tip = """
                如果需要关闭打印, 查看 BarSkeletonBuilder#setting#printAction;
                如需要打印（class method params return）完整的包名, 查看 BarSkeletonBuilder#setting#printActionShort;
                """;
        System.out.print(tip);

        for (int cmd = 0; cmd < behaviors.length; cmd++) {
            ActionCommand[] subBehaviors = behaviors[cmd];

            if (Objects.isNull(subBehaviors)) {
                continue;
            }

            for (int subCmd = 0; subCmd < subBehaviors.length; subCmd++) {
                ActionCommand subBehavior = subBehaviors[subCmd];

                if (Objects.isNull(subBehavior)) {
                    continue;
                }

                ActionCommand.ParamInfo[] paramInfos = subBehavior.getParamInfos();
                String paramInfo = "";
                String paramInfoShort = "";

                if (ArrayKit.notEmpty(paramInfos)) {
                    paramInfoShort = Arrays.stream(paramInfos)
                            .map(ActionCommand.ParamInfo::toStringShort)
                            .collect(Collectors.joining(", "));

                    paramInfo = ArrayKit.join(paramInfos, ", ");
                }

                Map<String, Object> params = new HashMap<>();
                params.put("cmd", cmd);
                params.put("subCmd", subCmd);
                params.put("actionName", subBehavior.getActionControllerClazz().getName());
                params.put("methodName", subBehavior.getActionMethodName());
                params.put("paramInfo", paramInfo);
                params.put("paramInfoShort", paramInfoShort);
                params.put("actionNameShort", subBehavior.getActionControllerClazz().getSimpleName());
                params.put("throw", subBehavior.isThrowException() ? "throws" : "");

                shortName(params, shortName);

                // 返回类型
                ActionCommand.ActionMethodReturnInfo actionMethodReturnInfo = subBehavior.getActionMethodReturnInfo();
                final Class<?> returnTypeClazz = actionMethodReturnInfo.getActualClazz();
                params.put("returnTypeClazz", returnTypeClazz.getName());
                params.put("returnTypeClazzShort", returnTypeClazz.getSimpleName());

                checkReturnType(actionMethodReturnInfo.getReturnTypeClazz());

                shortName(params, shortName);

                params.put("actionSimpleName", subBehavior.getActionControllerClazz().getSimpleName());
                params.put("lineNumber", subBehavior.actionCommandDoc.getLineNumber());

                String routeCell = Color.red.format("路由: {cmd} - {subCmd}", params);
                String actionCell = Color.white.wrap("--- action :");
                String actionNameCell = Color.blue.format("{actionName}", params);
                String methodNameCell = Color.blue.format("{methodName}", params);
                String paramInfoCell = Color.green.format("{paramInfo}", params);

                String returnCell = Color.defaults.wrap("return");
                String returnValueCell = Color.magenta.format("{returnTypeClazz}", params);
                String throwCell = Color.red.format("{throw}", params);

                params.put("routeCell", routeCell);
                params.put("actionCell", actionCell);
                params.put("actionNameCell", actionNameCell);
                params.put("methodNameCell", methodNameCell);
                params.put("returnCell", returnCell);
                params.put("returnValueCell", returnValueCell);
                params.put("throwCell", throwCell);
                params.put("paramInfoCell", paramInfoCell);

                String lineTemplate = "{routeCell} {actionCell} {actionNameCell}.{methodNameCell}({paramInfoCell}) {throwCell} --- return {returnValueCell}  ~~~ see.({actionSimpleName}.java:{lineNumber})";
                String text = StrKit.format(lineTemplate, params);
                System.out.println(Ansi.ansi().render(text));
            }
        }
    }

    void printDataCodec() {
        DataCodec dataCodec = DataCodecKit.dataCodec;

        String title = "@|CYAN ======================== 当前使用的编解码器 ========================= |@";
        System.out.println(Ansi.ansi().render(title));
        System.out.println("如果需要关闭打印, 查看 BarSkeletonBuilder#setting#printDataCodec");

        String info = String.format("@|BLUE %s - %s |@", dataCodec.codecName(), dataCodec.getClass().getName());
        System.out.println(Ansi.ansi().render(info));

    }

    private void shortName(Map<String, Object> params, boolean shortName) {
        if (!shortName) {
            return;
        }

        params.put("paramInfo", params.get("paramInfoShort"));
        params.put("actionName", params.get("actionNameShort"));
        params.put("returnTypeClazz", params.get("returnTypeClazzShort"));
        params.put("actualTypeArgumentClazz", params.get("actualTypeArgumentClazzShort"));

    }

    private void checkReturnType(final Class<?> returnTypeClazz) {
        if (Set.class.isAssignableFrom(returnTypeClazz) || Map.class.isAssignableFrom(returnTypeClazz)) {
            // 参数的不支持不写逻辑了，这里告诉一下就行了。看之后的需要在考虑是否支持吧
            throw new RuntimeException("action 返回值和参数不支持 set、map 和 基础类型!");
        }
    }

    private static class Color {
        String start;
        static final Color red = new Color("@|red");
        static final Color white = new Color("@|white");
        static final Color blue = new Color("@|blue");
        static final Color green = new Color("@|green");
        static final Color defaults = new Color("@|default");
        static final Color magenta = new Color("@|magenta");

        public Color(String start) {
            this.start = start;
        }

        String wrap(String str) {
            return start + " " + str + "|@";
        }

        String format(String template, Map<String, Object> params) {
            String str = StrKit.format(template, params);
            str = wrap(str);
            return str;
        }
    }
}
