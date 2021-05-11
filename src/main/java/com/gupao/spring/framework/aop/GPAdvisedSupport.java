package com.gupao.spring.framework.aop;

import com.gupao.spring.framework.aop.aspect.GPAdvice;
import com.gupao.spring.framework.aop.config.GPAopConfig;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 解析AOP配置的工具类
 *
 * @author lichao chao.li07@hand-china.com 2021-01-21 17:06
 */
public class GPAdvisedSupport {

    private GPAopConfig config;           //AOP配置类
    private Object target;                //目标对象
    private Class<?> targetClass;         //需要切如切面的目标类
    private Pattern pointCutClassPattern; //切面类正则

    private Map<Method,Map<String,GPAdvice>> methodCache;

    public GPAdvisedSupport(GPAopConfig config) {
        this.config = config;
    }

    /*
     * 解析配置文件
     */
    private void parse(){
        //一定是离不开正则
        //把spring的Excpress变成一个Java能够识别的正则表达式
        String pointCut = config.getPointCut()
                .replaceAll("\\.","\\\\.")
                .replaceAll("\\\\.\\*",".*")
                .replaceAll("\\(","\\\\(")
                .replaceAll("\\)","\\\\)");
        //三段
        //第一段：方法的修饰符和返回值

        //第二段：类名

        //第三段：方法的名称和形参列表

        //生成匹配Class的正则-保存专门匹配Class的正则
        String pointCutForClassRegex
                = pointCut.substring(0,pointCut.lastIndexOf("\\(") - 4);
        pointCutClassPattern =
                Pattern.compile("class " + pointCutForClassRegex
                        .substring(pointCutForClassRegex.lastIndexOf(" ") +1));

        //享元的共享池
        methodCache = new HashMap<Method, Map<String, GPAdvice>>();
        //保存专门匹配方法的正则
        Pattern pointCutPattern = Pattern.compile(pointCut);
        try{
            Class aspectClass = Class.forName(this.config.getAspectClass());
            Map<String,Method> aspectMethods = new HashMap<String, Method>();
            for(Method method : aspectClass.getMethods()){
                aspectMethods.put(method.getName(),method);
            }

            //以上都是初始化工作，准备阶段

            //从此处开始封装GPAdvice
            for(Method method : this.targetClass.getMethods()){
                //method.toString方法会拿到方法的所有名称信息，全路径名、方法类型、返回值、方法名、形参列表
                String methodString = method.toString();
                if(methodString.contains("throws")){
                    methodString = methodString.substring(0,methodString.lastIndexOf("throws")).trim();
                }
                Matcher matcher = pointCutPattern .matcher(methodString);
                if(matcher.matches()){
                    Map<String,GPAdvice> advices = new HashMap<String, GPAdvice>();

                    if(!(null == config.getAspectBefore() || "".equals(config.getAspectBefore()))){
                        advices.put("before",new GPAdvice(aspectClass.newInstance(),aspectMethods.get(config.getAspectBefore())));
                    }

                    if(!(null == config.getAspectAfter() || "".equals(config.getAspectAfter()))){
                        advices.put("after",new GPAdvice(aspectClass.newInstance(),aspectMethods.get(config.getAspectAfter())));
                    }

                    if(!(null == config.getAspectAfterThrow() || "".equals(config.getAspectAfterThrow()))){
                        GPAdvice advice = new GPAdvice(aspectClass.newInstance(),aspectMethods.get(config.getAspectAfterThrow()));
                        advice.setThrowName(config.getAspectAfterThrowingName());
                        advices.put("afterThrow",advice);
                    }

                    //根目标代理类的业务方法和Advices建立一对多的关联关系，以便在Proxy类中获得
                    methodCache.put(method,advices);
                }
            }
        }catch (Exception e){
            e.printStackTrace();

        }
    }

    /*
     * 根据一个目标代理类方法，获得目标通知
     */
    public Map<String, GPAdvice> getAdvices(Method method, Object o) throws Exception {
        //享元设计模式应用
        Map<String,GPAdvice> cache = methodCache.get(method);
        if(null == cache){
            Method m = targetClass.getMethod(method.getName(),method.getParameterTypes());
            cache = methodCache.get(m);
            this.methodCache.put(m,cache);
        }
        return cache;
    }

    /*
     * 给ApplicationContext IOC中的对象初始化时调用，决定要不要生成代理类逻辑
     * 根据目标类名以及包名和aop配置文件中配置的需要切入的路径做匹配
     */
    public boolean pointCutMatch() {
        return pointCutClassPattern.matcher(this.targetClass.toString()).matches();
    }

    /*
     * 判断目标类，类名是否符合AOP配置规则å
     */
    public void setTargetClass(Class<?> targetClass) {
        this.targetClass = targetClass;
        parse();
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public Class<?> getTargetClass() {
        return this.targetClass;
    }

    public Object getTarget() {
        return this.target;
    }
}
