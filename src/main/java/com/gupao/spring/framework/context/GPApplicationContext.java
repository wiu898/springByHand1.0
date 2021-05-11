package com.gupao.spring.framework.context;

import com.gupao.spring.framework.annotation.GPAutowired;
import com.gupao.spring.framework.annotation.GPController;
import com.gupao.spring.framework.annotation.GPService;
import com.gupao.spring.framework.aop.GPAdvisedSupport;
import com.gupao.spring.framework.aop.config.GPAopConfig;
import com.gupao.spring.framework.aop.support.GPJdkDynamicAopProxy;
import com.gupao.spring.framework.beans.GPBeanWrapper;
import com.gupao.spring.framework.beans.config.GPBeanDefinition;
import com.gupao.spring.framework.beans.support.GPBeanDefinitionReader;
import com.gupao.spring.framework.core.GPBeanFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 职责：完成Bean的创建和DI
 *
 * @author lichao chao.li07@hand-china.com 2021-01-16 15:39
 */
public class GPApplicationContext implements GPBeanFactory {

    private GPBeanDefinitionReader reader;

    private Map<String, GPBeanDefinition> beanDefinitionMap = new HashMap<String, GPBeanDefinition>();

    //缓存对外暴露的包装对象BeanWrapper
    private Map<String,GPBeanWrapper> factoryBeanInstanceCache = new HashMap<String, GPBeanWrapper>();

    //缓存真正的实例对象
    private Map<String,Object> factoryBeanObjectCache = new HashMap<String, Object>();

    public GPApplicationContext(String... configLocations){

        //1.通过 BeanDefinitionReader 加载配置文件
        reader = new GPBeanDefinitionReader(configLocations);
        try{
            //2.通过 BeanDefinitionReader 解析配置文件，封装成 Spring内部的 BeanDefinition
            List<GPBeanDefinition> beanDefinitions = reader.loadBeanDefinitions();

            //3.缓存BeanDefinition
            doRegistBeanDefinition(beanDefinitions);

            //4.执行依赖注入
            doAutowrited();

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void doAutowrited() {
        //此处所有的bean并没有真正的实例化，还只是配置阶段
        for(Map.Entry<String,GPBeanDefinition> beanDefinitionEntry : this.beanDefinitionMap.entrySet()){
            String beanName = beanDefinitionEntry.getKey();
            //调用getBean触发依赖注入
            getBean(beanName);
        }
    }

    /*
     * 缓存Map格式的BeanDefinition
     */
    private void doRegistBeanDefinition(List<GPBeanDefinition> beanDefinitions) throws Exception{
        for(GPBeanDefinition beanDefinition : beanDefinitions){
            if(this.beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName())){
                throw new Exception("The " + beanDefinition.getFactoryBeanName() + " is exists");
            }
            beanDefinitionMap.put(beanDefinition.getFactoryBeanName(),beanDefinition);
            beanDefinitionMap.put(beanDefinition.getBeanClassName(),beanDefinition);
        }
    }


    /*
     * 依赖注入方法
     * Bean的实例化，DI是从这个方法真正开始的
     */
    public Object getBean(String beanName){
        //1.拿到BeanDefinition配置信息
        GPBeanDefinition beanDefinition = this.beanDefinitionMap.get(beanName);
        //2.通过反射实例化对象，并放入缓存
        Object instance = instantiateBean(beanName,beanDefinition);
        //3.将实例化的 BeanDefinition对象 封装成BeanWrapper
        GPBeanWrapper beanWrapper = new GPBeanWrapper(instance);
        //4.将BeanWrapper保存到IOC容器
        factoryBeanInstanceCache.put(beanName,beanWrapper);
        //5.执行依赖注入
        populateBean(beanName,beanDefinition,beanWrapper);
        return beanWrapper.getWrapperInstance();
    }

    /*
     * 实例化方法
     * 创建真正的实例对象
     */
    private Object instantiateBean(String beanName, GPBeanDefinition beanDefinition) {
        String className = beanDefinition.getBeanClassName();
        Object instance = null;
        try{
            //保证单例模式，如果已经存在直接取出使用，如果没有再创建
            if(this.factoryBeanObjectCache.containsKey(beanName)){
                instance = this.factoryBeanObjectCache.get(beanName);
            }else{
                //根据类名获取类然后实例化
                Class<?> clazz = Class.forName(className);
                instance = clazz.newInstance();

                //======================AOP开始=========================

                //1.加载AOP配置文件
                GPAdvisedSupport config = instantionAopConfig(beanDefinition);
                //判断目标类，类名是否符合AOP配置规则
                config.setTargetClass(clazz);
                //设置对象本身
                config.setTarget(instance);

                //AOP判断规则，判断要不要生成代理类。 如果满足条件，覆盖原生对象，如果不满足 就不做任何处理返回原生对象
                if(config.pointCutMatch()){
                    //生成代理类，用代理类覆盖原生对象
                    instance = new GPJdkDynamicAopProxy(config).getProxy();
                }

                //======================AOP结束=========================

                //将真正实例化的对象保存到缓存中
                this.factoryBeanObjectCache.put(beanName,instance);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return instance;
    }

    private GPAdvisedSupport instantionAopConfig(GPBeanDefinition beanDefinition) {
        GPAopConfig config = new GPAopConfig();
        config.setPointCut(this.reader.getConfig().getProperty("pointCut"));
        config.setAspectClass(this.reader.getConfig().getProperty("aspectClass"));
        config.setAspectBefore(this.reader.getConfig().getProperty("aspectBefore"));
        config.setAspectAfter(this.reader.getConfig().getProperty("aspectAfter"));
        config.setAspectAfterThrow(this.reader.getConfig().getProperty("aspectAfterThrow"));
        config.setAspectAfterThrowingName(this.reader.getConfig().getProperty("aspectAfterThrowingName"));
        return new GPAdvisedSupport(config);
    }

    /*
     * 依赖注入真正执行的方法
     */
    private void populateBean(String beanName, GPBeanDefinition beanDefinition, GPBeanWrapper beanWrapper) {
        /* 可能涉及到循环依赖
         * 解决方案：用两个缓存，循环两次
         * 1.把第一次读取结果为空的BeanDefinition存到第一个缓存
         * 2.等第一次循环之后，第二次循环再检查第一次的缓存，再进行赋值
         */
        Object instance = beanWrapper.getWrapperInstance();
        Class<?> clazz = beanWrapper.getWrappedClass();

        //在Spring中是 @Component
        if(!(clazz.isAnnotationPresent(GPController.class)
                || clazz.isAnnotationPresent(GPService.class))){
            return;
        }

        //获取类下的所有属性 包括private/protected/default/private 修饰的字段
        //比如 @Autowired private IService service;
        for(Field field : clazz.getDeclaredFields()){
            //如果没有声明@Autowired直接跳过不处理
            if(!field.isAnnotationPresent(GPAutowired.class)){ continue; }
            //获取@Autowired注解
            GPAutowired autowired = field.getAnnotation(GPAutowired.class);
            //获取@GPAutowired注解中的值
            String autowiredBeanName = autowired.value().trim();
            //如果用户没有自定义的beanName,就默认根据类型注入
            if("".equals(autowiredBeanName)){
                //获取字段类型 field.getType().getName(),得到接口全名
                autowiredBeanName = field.getType().getName() ;
            }
            //暴力访问 比如 private属性
            field.setAccessible(true);
            try {
                if(this.factoryBeanInstanceCache.get(autowiredBeanName) == null){
                    continue;
                }
                //给类声明的属性赋值
                //ioc.get(beanName)根据接口名beanName，拿到接口实现类型的实例也就是实现类
                field.set(instance,this.factoryBeanInstanceCache.get(autowiredBeanName).getWrapperInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                continue;
            }
        }
    }


    public Object getBean(Class beanClass){
        return getBean(beanClass.getName());
    }

    /*
     * 获取容器中已经实例化的Bean数量
     */
    public int getBeanDefinitionCount() {
        return this.beanDefinitionMap.size();
    }

    /*
     * 获取容器中已经实例化的BeanNames
     */
    public String[] getBeanDefinitionNames() {
        return this.beanDefinitionMap.keySet().toArray(new String[this.beanDefinitionMap.size()]);
    }

    public Properties getConfig(){
        return this.reader.getConfig();

    }
}
