package com.tiaohe.lock.util;

import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * SpEL (Spring Expression Language) 工具类
 * 用于解析方法参数并执行表达式
 */
public class SpElUtils {

    // SpEL表达式解析器
    private static final ExpressionParser parser = new SpelExpressionParser();

    // 获取方法参数名称的发现器
    private static final DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    /**
     * 解析传入的 SpEL 表达式
     *
     * @param method 当前执行的方法
     * @param args   方法的参数值
     * @param spEl   需要解析的 SpEL 表达式
     * @return 解析后的字符串结果
     */
    public static String parseSpEl(Method method, Object[] args, String spEl) {
        // 获取方法的参数名
        String[] params = Optional.ofNullable(parameterNameDiscoverer.getParameterNames(method))
                .orElse(new String[]{});

        // 创建 SpEL 表达式的上下文
        EvaluationContext context = new StandardEvaluationContext();

        // 将方法的参数与参数名绑定到上下文中
        for (int i = 0; i < params.length; i++) {
            context.setVariable(params[i], args[i]);
        }

        // 解析并执行 SpEL 表达式
        Expression expression = parser.parseExpression(spEl);
        return expression.getValue(context, String.class);
    }

    /**
     * 获取方法的唯一标识（全限定类名 + 方法名）
     *
     * @param method 目标方法
     * @return 方法的唯一标识
     */
    public static String getMethodKey(Method method) {
        return method.getDeclaringClass().getName() + "#" + method.getName();
    }
}