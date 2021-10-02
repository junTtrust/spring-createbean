### 本工程代码主要是模拟Spring 创建bean的过程
A.class --> 无参构造方法 --> 普通对象  --> 依赖注入(属性赋值) --> 初始化前(@PostConsturct)  --> 初始化(InitializingBean) -->
初始化后(AOP) --> 代理对象  --> bean