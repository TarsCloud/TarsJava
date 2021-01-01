package com.qq.tars.spring.condition;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * 只有是Tars环境的时候才会生效
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
 * 将代码发布到tars平台上时，info日志中会输出{@code initializing tars bean, loaded bean: candidate:TarsBean}；
 * 如果在本地启动，info日志中会输出{@code initializing non-tars bean, loaded bean: candidate:NonTarsBean}。
 *
 * @author kongyuanyuan
 * @see ConditionalOnNonTars
 * @see OnTarsCondition
 */
@Conditional(OnTarsCondition.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
public @interface ConditionalOnTars {
}
