package com.qq.tars.spring.condition;

import com.qq.tars.server.config.ConfigurationManager;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.util.StringUtils;

/**
 * 判断是不是Tars环境
 *
 * @author kongyuanyuan
 */
public class OnTarsCondition extends SpringBootCondition {
    private final boolean hasConfigProperty;
    private final boolean isTars;

    public OnTarsCondition() {
        String config = System.getProperty("config");
        hasConfigProperty = StringUtils.hasText(config);
        isTars = hasConfigProperty
                && ConfigurationManager.getInstance().getServerConfig() != null;
    }

    /**
     * 判断是不是Tars环境.
     *
     * @param context  the condition context
     * @param metadata the metadata of the {@link AnnotationMetadata class}
     *                 or {@link MethodMetadata method} being checked
     * @return {@code true} if the condition matches and the component can be registered,
     * or {@code false} to veto the annotated component's registration
     */
    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return buildConditionOutcome(metadata, isTars, hasConfigProperty);
    }

    /**
     * 根据是不是Tars环境以及是不是有config系统配置，返回条件判定结果与判定原因
     *
     * @param metadata          要判断的注解meta信息
     * @param isTars            是不是Tars环境
     * @param hasConfigProperty 是不是有config系统配置
     * @return 告诉spring判定结果
     */
    private static ConditionOutcome buildConditionOutcome(AnnotatedTypeMetadata metadata, boolean isTars, boolean hasConfigProperty) {
        if (metadata.isAnnotated(ConditionalOnTars.class.getName())) {
            //match only when running in tars environment
            // 当是Tars环境的时候才命中
            if (isTars) {
                return ConditionOutcome.match("System has property 'config' and ConfigurationManager has been successfully loaded");
            }
            String message;
            if (!hasConfigProperty) {
                message = "System does not have property 'config'";
            } else {
                message = "ConfigurationManager hasn't been loaded";
            }
            return ConditionOutcome.noMatch(message);
        } else {
            //match only when running in non-tars environment, such as local, ci/cd, test case, etc.
            //当是非Tars环境的时候才命中
            if (!isTars) {
                return ConditionOutcome.match("System does not have property 'config' and ConfigurationManager hasn't been loaded");
            }
            //in tars environment, return noMatch
            String message;
            if (!hasConfigProperty) {
                message = "ConfigurationManager has been loaded";
            } else {
                message = "System does have property 'config'";
            }
            return ConditionOutcome.noMatch(message);
        }
    }
}
