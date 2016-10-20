package org.springframework.boot.autoconfigure.condition;

import org.springframework.boot.context.embedded.ReactiveWebApplicationContext;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.support.StandardServletEnvironment;

/**
 * {@link Condition} that checks for the presence or absence of
 * {@link ReactiveWebApplicationContext}.
 *
 * @author Dave Syer
 * @see ConditionalOnReactiveWebApplication
 * @see ConditionalOnNotReactiveWebApplication
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class OnReactiveWebApplicationCondition extends SpringBootCondition {
    private static final String WEB_CONTEXT_CLASS = "org.springframework.boot.context.embedded" +
            ".ReactiveWebApplicationContext";

    OnReactiveWebApplicationCondition() {
    }

    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        boolean required = metadata.isAnnotated(ConditionalOnReactiveWebApplication.class.getName());
        ConditionOutcome outcome = this.isReactiveWebApplication(context, metadata, required);
        return required && !outcome.isMatch() ? ConditionOutcome.noMatch(outcome.getConditionMessage()) : (!required
                && outcome.isMatch() ? ConditionOutcome.noMatch(outcome.getConditionMessage()) : ConditionOutcome
                .match(outcome.getConditionMessage()));
    }

    private ConditionOutcome isReactiveWebApplication(ConditionContext context, AnnotatedTypeMetadata metadata, boolean
            required) {
        ConditionMessage.Builder message = ConditionMessage
                .forCondition(ConditionalOnReactiveWebApplication.class, required ? "(required)" : "");
        if (!ClassUtils.isPresent(WEB_CONTEXT_CLASS, context.getClassLoader())) {
            return ConditionOutcome.noMatch(message.didNotFind("reactive web application classes").atAll());
        } else {
            if (context.getBeanFactory() != null) {
                String[] scopes = context.getBeanFactory().getRegisteredScopeNames();
                if (ObjectUtils.containsElement(scopes, "session")) {
                    return ConditionOutcome.match(message.foundExactly("\'session\' scope"));
                }
            }

            return context.getEnvironment() instanceof StandardServletEnvironment ?
                    ConditionOutcome.match(message.foundExactly("StandardServletEnvironment")) :
                    (
                            context.getResourceLoader() instanceof ReactiveWebApplicationContext ?
                                    ConditionOutcome.match(message.foundExactly("ReactiveWebApplicationContext")) :
                                    ConditionOutcome.noMatch(message.because("not a reactive web application"))
                    );
        }
    }
}