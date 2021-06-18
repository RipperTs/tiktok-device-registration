# 部署文档

> 基于抖音版本12.5.0 so文件

#### 依赖环境

Java 1.8 Maven

##### 安装 Maven

```
wget http://mirrors.tuna.tsinghua.edu.cn/apache/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz
```

````
tar -zxvf apache-maven-3.3.9-bin.tar.gz
````

````
mv apache-maven-3.3.9 /usr/local/maven
````

````
vim /etc/profile
````

在/etc/profile文件末尾增加以下配置：

````
M2_HOME=/usr/local/maven
export PATH=${M2_HOME}/bin:${PATH}
````

重载/etc/profile这个文件

````
source /etc/profile
````

检查安装

```
mvn -v
```



### 一键代码Jar包

```
cd 目录
```
```
mvn clean install -DskipTests
```
```
cd target
```
```
java -jar douyin-spider-0.0.1-SNAPSHOT.jar
```
---
访问地址: http://localhost:9022/register

---

### 注意

服务器需要开9022端口。  

跑脚本需要间隔3秒，否则可能出现抖音风控

