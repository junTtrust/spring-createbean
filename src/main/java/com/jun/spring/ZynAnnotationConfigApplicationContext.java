package com.jun.spring;

import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: yijunjun
 * @Date: 2021/9/25 17:23
 */
public class ZynAnnotationConfigApplicationContext {

    // beanDefinitionMap
    private Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();
    // 单例池bean
    private Map<String, Object> singletonObject = new HashMap<>();
    // 实现了beanPostProcessor 接口的bean
    List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();

    private Class configClass;

    public ZynAnnotationConfigApplicationContext(Class configClass) {
        this.configClass = configClass;
        // 1.扫描类
        scan(configClass);

        // 遍历beanDefinitionMap
       for(Map.Entry<String,BeanDefinition> entry : beanDefinitionMap.entrySet()){
           String beanName = entry.getKey();
           BeanDefinition beanDefinition = entry.getValue();

           //找出所有单例bean
           if("singleton".equals(beanDefinition.getScope())){
               Object bean = createBean(beanName, beanDefinition);
               singletonObject.put(beanName,bean);
           }
       }
    }


    private Object createBean(String beanName,BeanDefinition beanDefinition){

        Object instance = null;
        try {
            Class clazz = beanDefinition.getType();
            // 获取无参构造方法
            Constructor constructor = clazz.getConstructor();
            // 创建实例
            instance = constructor.newInstance();

            // 依赖注入
            // 获取bean所有属性
            for(Field field : clazz.getDeclaredFields()){
                if(field.isAnnotationPresent(Autowired.class)){
                    field.setAccessible(true);
                    //TODO 这里会存在循环依赖问题，暂时不考虑
                    field.set(instance,getBean(field.getName()));
                }
            }

            //aware
            if(instance instanceof BeanNameAware){
                ((BeanNameAware) instance).setBeanName(beanName);
            }

            // 初始化
            if(instance instanceof InitializingBean){
                ((InitializingBean) instance).afterPropertiesSet();
            }

            // beanPostProcessor
            for(BeanPostProcessor beanPostProcessor : beanPostProcessorList){
                // 这里的instance 如果后置处理器实现了代理对象，那么返回的是代理bean
                // 这里已UserService 为例
                instance = beanPostProcessor.postProcessAfterInitialization(instance,beanName);
            }

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return instance;
    }

    /**
     *
     */
    private void instanceSingletonBean() {

    }


    /**
     * 扫描类 得到 BeanDefinition
     * @Desc 扫描class，转化为BeanDefinition对象，最后添加到beanDefinitionMap中
     * @param appConfig
     */
    private void scan(Class appConfig){
        // 1.获取扫描路径
        if(appConfig.isAnnotationPresent(ComponentScan.class)){
            ComponentScan componentScanAnnotation = (ComponentScan) appConfig.getAnnotation(ComponentScan.class);
            String packagePath = componentScanAnnotation.value();
            packagePath = packagePath.replace(".","/");
//            System.out.println("packagePath="+packagePath);
            //2.通过应用程序加载类
            ClassLoader classLoader = ZynAnnotationConfigApplicationContext.class.getClassLoader();
            URL resource = classLoader.getResource(packagePath);
//            System.out.println("resource="+resource);
//            System.out.println("resource.getfile="+resource.getFile());
            File file = new File(resource.getFile());
            // TODO 样例代码只有一层目录结构，如果有多层目录结构需要递归处理
            if(file.isDirectory()){
                File[] files = file.listFiles();
                for(File f : files){
                    String fileName = f.getAbsolutePath();
//                    System.out.println("f.getAbsoultPath="+fileName);
                    if(fileName.endsWith(".class")){
                        String className = fileName.substring(fileName.indexOf("com"), fileName.indexOf(".class"));
                        className = className.replace("\\", ".");
                        // 通过应用程序加载器加载class文件
                        Class<?> aClass = null;
                        try {
                            aClass = classLoader.loadClass(className);
                            // 如果添加了@Component 注解，表示是个bean
                            if(aClass.isAnnotationPresent(Component.class)){
                                // 判断某个类是否实现BeanPostProcessor 接口
                                if(BeanPostProcessor.class.isAssignableFrom(aClass)){
                                    BeanPostProcessor instance = (BeanPostProcessor)aClass.getConstructor().newInstance();
                                    beanPostProcessorList.add(instance);
                                }else{
                                    // 获取beanName
                                    Component componentAnnotation = aClass.getAnnotation(Component.class);
                                    String beanName = componentAnnotation.value();
                                    if("".equals(beanName)){
                                        beanName = Introspector.decapitalize(aClass.getSimpleName());
                                    }
                                    // 创建beanDefinition
                                    BeanDefinition beanDefinition = new BeanDefinition();
                                    beanDefinition.setType(aClass);
                                    if(aClass.isAnnotationPresent(Scope.class)){
                                        // 原型bean
                                        Scope scopeAnnotation = aClass.getAnnotation(Scope.class);
                                        String value = scopeAnnotation.value();
                                        beanDefinition.setScope(value);
                                    }else{
                                        // 单例bean
                                        beanDefinition.setScope("singleton");
                                    }
                                    beanDefinitionMap.put(beanName,beanDefinition);
                                }
                            }
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        } catch (InstantiationException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

    }

//    private List<Class> getBeanClasses(String packagePath){
//        System.out.println("packagePath="+packagePath);
//        List<Class> beanClasses = new ArrayList<>();
//
//
//        return beanClasses;
//    }


    /**
     *
     * @param beanName
     */
    public Object getBean(String beanName) {
        // 判断beanName 是否扫描存在,不存在抛出一层
        if(!beanDefinitionMap.containsKey(beanName)){
            throw new NullPointerException();
        }
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if("singleton".equals(beanDefinition.getScope())){
            // 单例
            Object singletonBean = singletonObject.get(beanName);
            if(singletonBean == null){
                // 如果为空，创建bean,这里主要是防止依赖注入的bean未被创建
                singletonBean = createBean(beanName, beanDefinition);
                singletonObject.put(beanName,singletonBean);
            }
            return singletonBean;
        }else{
            // 原型
            Object prototypeBean = createBean(beanName, beanDefinition);
            return prototypeBean;
        }
    }
}
