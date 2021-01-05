package com.qq.tars.spring.condition;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * Match only on non-tars environment. Such as local, CI/CD, test case, etc.
 * <pre class="code">
 * public class Main {
 *     public static void main(String[] args) {
 *         ConfigurableApplicationContext run = SpringApplication.run(Main.class, args);
 *         Map<String, TestBean> beansOfType = run.getBeansOfType(TestBean.class);
 *         TestBean candidate = beansOfType.values().stream().findFirst().get();
 *         log.info("loaded bean: candidate:{}", candidate.getClass().getCanonicalName());
 *     }
 *
 *     public interface TestBean {
 *     }
 *
 *     &#064;Component
 *     &#064;ConditionalOnTars
 *     public static class TarsBean implements TestBean {
 *         public TarsBean() {
 *             log.info("initializing tars bean");
 *         }
 *     }
 *
 *     &#064;Component
 *     &#064;ConditionalOnNonTars
 *     public static class NonTarsBean implements TestBean {
 *         public NoTarsBean() {
 *             log.info("initializing non-tars bean");
 *         }
 *     }
 * }
 * </pre>
 * When running on tars platform, {@code initializing tars bean, loaded bean: candidate:TarsBean} will be shown in info log,
 * while running on non-tars platform(local, CI/CD, test case, etc.), {@code initializing non-tars bean, loaded bean: candidate:NonTarsBean} will be shown in info log.
 *
 * @author kongyuanyuan
 * @see ConditionalOnTars
 * @see OnTarsCondition
 */
@Conditional(OnTarsCondition.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
public @interface ConditionalOnNonTars {
}
