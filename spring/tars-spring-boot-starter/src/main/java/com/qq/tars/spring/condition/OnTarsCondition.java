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
 * Tars-environment condition used with environment-aware classes.
 *
 * @author kongyuanyuan
 * @see ConditionalOnTars
 * @see ConditionalOnNonTars
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
     * Get the match result.
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
     * Match with condition.
     *
     * @param metadata          metadata with annotation
     * @param isTars            is in tars environment
     * @param hasConfigProperty has config system property
     * @return match result
     */
    private static ConditionOutcome buildConditionOutcome(AnnotatedTypeMetadata metadata, boolean isTars, boolean hasConfigProperty) {
        if (metadata.isAnnotated(ConditionalOnTars.class.getName())) {
            //match only when running in tars environment
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
