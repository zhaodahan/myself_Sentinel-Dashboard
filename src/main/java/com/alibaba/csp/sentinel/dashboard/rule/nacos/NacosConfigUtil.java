/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.dashboard.rule.nacos;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.util.JSONUtils;
import com.alibaba.csp.sentinel.slots.block.Rule;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Eric Zhao
 * @author www.itmuch.com
 * @since 1.4.0
 */
public final class NacosConfigUtil {

    public static final String GROUP_ID = "SENTINEL_GROUP";

    public static final String FLOW_DATA_ID_POSTFIX = "-flow-rules";
    public static final String DEGRADE_DATA_ID_POSTFIX = "-degrade-rules";
    public static final String SYSTEM_DATA_ID_POSTFIX = "-system-rules";
    public static final String PARAM_FLOW_DATA_ID_POSTFIX = "-param-flow-rules";
    public static final String AUTHORITY_DATA_ID_POSTFIX = "-authority-rules";
    public static final String DASHBOARD_POSTFIX = "-dashboard";
    public static final String CLUSTER_MAP_DATA_ID_POSTFIX = "-cluster-map";

    /**
     * cc for `cluster-client`
     */
    public static final String CLIENT_CONFIG_DATA_ID_POSTFIX = "-cc-config";
    /**
     * cs for `cluster-server`
     */
    public static final String SERVER_TRANSPORT_CONFIG_DATA_ID_POSTFIX = "-cs-transport-config";
    public static final String SERVER_FLOW_CONFIG_DATA_ID_POSTFIX = "-cs-flow-config";
    public static final String SERVER_NAMESPACE_SET_DATA_ID_POSTFIX = "-cs-namespace-set";


    private NacosConfigUtil() {
    }

    /**
     * ?????????????????????JSON??????????????????Nacos server???
     *
     * @param configService nacos config service
     * @param app           ????????????
     * @param postfix       ???????????? eg.NacosConfigUtil.FLOW_DATA_ID_POSTFIX
     * @param rules         ????????????
     * @throws NacosException ??????
     */
    public static <T> void setRuleStringToNacos(ConfigService configService, String app, String postfix, List<T> rules) throws NacosException {
        AssertUtil.notEmpty(app, "app name cannot be empty");
        if (rules == null) {
            return;
        }

        List<Rule> ruleForApp = rules.stream()
            .map(rule -> {
                RuleEntity rule1 = (RuleEntity) rule;
                System.out.println(rule1.getClass());
                Rule rule2 = rule1.toRule();
                System.out.println(rule2.getClass());
                return rule2;
            })
            .collect(Collectors.toList());

        // ???????????????????????????
        String dataId = genDataId(app, postfix);
        configService.publishConfig(
            dataId,
            NacosConfigUtil.GROUP_ID,
            JSONUtils.toJSONString(ruleForApp)
        );

        // ???????????????????????????
        configService.publishConfig(
            dataId + DASHBOARD_POSTFIX,
            NacosConfigUtil.GROUP_ID,
            JSONUtils.toJSONString(rules)
        );
    }

    /**
     * ???Nacos server??????????????????????????????????????????????????????Rule??????
     *
     * @param configService nacos config service
     * @param appName       ????????????
     * @param postfix       ???????????? eg.NacosConfigUtil.FLOW_DATA_ID_POSTFIX
     * @param clazz         ???
     * @param <T>           ??????
     * @return ??????????????????
     * @throws NacosException ??????
     */
    public static <T> List<T> getRuleEntitiesFromNacos(ConfigService configService, String appName, String postfix, Class<T> clazz) throws NacosException {
        String rules = configService.getConfig(
            genDataId(appName, postfix) + DASHBOARD_POSTFIX,
            NacosConfigUtil.GROUP_ID,
            3000
        );
        if (StringUtil.isEmpty(rules)) {
            return new ArrayList<>();
        }
        return JSONUtils.parseObject(clazz, rules);
    }

    private static String genDataId(String appName, String postfix) {
        return appName + postfix;
    }
}
