# RestApiCalculator

Сладков Данила Алексеевич sladkkov@yandex.ru

Программа выполенна на языке Java. Программа отражает функционал простого калькулятора 

Использованы: Java, JUnit, Swagger, Spring Web.

Java 17
Run app: src/main/java/ru/sladkkov/calculator/CalculatorApplication.java

Swagger Doc: http://localhost:9090/swagger-ui/index.html#/


<h1 class="code-line" data-line-start=0 data-line-end=1 ><a id="__0"></a>Сладков Данила</h1>
<h1 class="code-line" data-line-start=1 data-line-end=2 ><a id="_1"></a>Задание</h1>
<h2 class="code-line" data-line-start=2 data-line-end=3 ><a id="_1_2"></a>Шаг 1.</h2>
<ul>
<li class="has-line-data" data-line-start="3" data-line-end="4">Был установлен Docker.</li>
<li class="has-line-data" data-line-start="4" data-line-end="5">Был создан аккаунт на Docker Hub.</li>
<li class="has-line-data" data-line-start="5" data-line-end="6">В качестве приложения выбрал Rest Calculator написанный на Java.</li>
</ul>
<h2 class="code-line" data-line-start=6 data-line-end=7 ><a id="_2_6"></a>Шаг 2.</h2>
<p class="has-line-data" data-line-start="7" data-line-end="8">Создадим Dockerfile</p>
<pre><code class="has-line-data" data-line-start="9" data-line-end="26">FROM openjdk:8
ADD . /src
WORKDIR /src

ARG UID=1001
ARG GID=1001

RUN addgroup   --system --gid ${GID} user
RUN adduser  --system --uid  ${UID}   --group user
RUN chown -R user:user /src \
USER user

COPY   /target/Calculator-0.0.1-SNAPSHOT.jar Calculator.jar

EXPOSE 8080
ENTRYPOINT [&quot;java&quot;,&quot;-jar&quot;, &quot;Calculator.jar&quot;]
</code></pre>
<p class="has-line-data" data-line-start="26" data-line-end="27">Выполним команды</p>
<ul>
<li class="has-line-data" data-line-start="27" data-line-end="30">docker build -t sladkkov/calculator:1.0.0 -t sladkkov/calculator:1.0.0 . (Создаем image c тэгом 1.0.0 и названием sladkkov/calculator)<br>
-docker run -ti --rm -p 9090:9090 --name calculator sladkkov/calculator:1.0.0</li>
</ul>
<p class="has-line-data" data-line-start="30" data-line-end="31">После этого я проверил, что всё работает. Открыл Docker Desktop и увидел, что размер имейджа 534 Мб, что мне показалось слишком много и второе, что мне не понравилось, то что проект использует готовый jar файл, который был получен до этого.</p>
<h2 class="code-line" data-line-start=32 data-line-end=33 ><a id="_3_32"></a>Шаг 3</h2>
<p class="has-line-data" data-line-start="33" data-line-end="34">Создадим новый Dockerfile.</p>
<pre><code class="has-line-data" data-line-start="35" data-line-end="47"># syntax=docker/dockerfile:1
FROM openjdk:16-alpine3.13

WORKDIR /RestApiCalculator

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline
COPY src ./src

CMD [&quot;./mvnw&quot;, &quot;spring-boot:run&quot;]
</code></pre>
<p class="has-line-data" data-line-start="47" data-line-end="49">Также был добавлен файл .dockeringore, в котором папка target<br>
Изучив документацию, пришёл к вот такому варианту, который сам загружает зависимости, и запускает spring-boot:run.</p>
<p class="has-line-data" data-line-start="50" data-line-end="51">В этом варианте размер составляет 604.06 MB</p>
<p class="has-line-data" data-line-start="52" data-line-end="53">Здесь я не добавлял нового user’a, потому что это не финальная версия, а с разными базовыми образами приходится каждый раз менять свойства.</p>
<h2 class="code-line" data-line-start=54 data-line-end=55 ><a id="_4_54"></a>Шаг 4</h2>
<p class="has-line-data" data-line-start="55" data-line-end="56">Изучив ещё больше статей, видео и документации, пришел к такому варианту, используя multi-stage построение image.</p>
<pre><code class="has-line-data" data-line-start="58" data-line-end="92">FROM adoptopenjdk/openjdk11:jdk-11.0.5_10-alpine as builder

ADD . /src
WORKDIR /src
RUN  ./mvnw package -DskipTests

ARG UID=1001
ARG GID=1001

RUN addgroup --system --gid ${GID}  user &amp;&amp; adduser --system --uid ${UID}  user --ingroup  user
RUN chown -R user:user /opt
RUN chown -R user:user /src

USER user

FROM alpine:3.10.3 as packager
RUN apk --no-cache add openjdk11-jdk openjdk11-jmods
ENV JAVA_MINIMAL=&quot;/opt/java-minimal&quot;
RUN /usr/lib/jvm/java-11-openjdk/bin/jlink \
    --verbose \
    --add-modules \
        java.base,java.naming,java.desktop,java.management,java.security.jgss,java.instrument \
    --compress 2 --strip-debug --no-header-files --no-man-pages \
      --output &quot;$JAVA_MINIMAL&quot;

FROM alpine:3.10.3
ENV JAVA_HOME=/opt/java-minimal
ENV PATH=&quot;$PATH:$JAVA_HOME/bin&quot;

COPY  --from=packager &quot;$JAVA_HOME&quot; &quot;$JAVA_HOME&quot;
COPY  --from=builder /src/target/Calculator-0.0.1-SNAPSHOT.jar Calculator.jar
EXPOSE 9090
ENTRYPOINT [&quot;java&quot;,&quot;-jar&quot;,&quot;/Calculator.jar&quot;]
</code></pre>
<p class="has-line-data" data-line-start="93" data-line-end="95">Здесь мы вручную конфигурируем модули, которые нам нужны для работы приложения.<br>
Начальную версию кода, я нашёл в статье на Хабре. Было миллион ошибок в процессе допиливания под себя. Изучил много видео, статей и документации, о том как всё здесь работает, и достаточно хорошо разобрался.</p>
<p class="has-line-data" data-line-start="96" data-line-end="97">По этой команде узнаем модули в нашем проекте</p>
<pre><code class="has-line-data" data-line-start="98" data-line-end="100"> jdeps C:\Users\sladk\Downloads\control-work-time-main\RestApiCalculator
</code></pre>
<p class="has-line-data" data-line-start="100" data-line-end="101">Размер image удалось сократить до 81.46 MB. Что меня вполне устроило.</p>
<p class="has-line-data" data-line-start="102" data-line-end="103">Выполним команды</p>
<ul>
<li class="has-line-data" data-line-start="103" data-line-end="104">docker build -t sladkkov/calculator:1.0.0 -t sladkkov/calculator:1.0.0 . (Создаем image c тэгом 1.0.0 и названием sladkkov/calculator)</li>
</ul>
<pre><code class="has-line-data" data-line-start="107" data-line-end="137">[+] Building 101.7s (20/20) FINISHED
 =&gt; [internal] load build definition from Dockerfile                                                                                                                                                                                                                               0.0s
 =&gt; =&gt; transferring dockerfile: 1.01kB                                                                                                                                                                                                                                             0.0s
 =&gt; [internal] load .dockerignore                                                                                                                                                                                                                                                  0.0s
 =&gt; =&gt; transferring context: 34B                                                                                                                                                                                                                                                   0.0s
 =&gt; [internal] load metadata for docker.io/library/alpine:3.10.3                                                                                                                                                                                                                   2.6s
 =&gt; [internal] load metadata for docker.io/adoptopenjdk/openjdk11:jdk-11.0.5_10-alpine                                                                                                                                                                                             2.5s
 =&gt; [auth] adoptopenjdk/openjdk11:pull token for registry-1.docker.io                                                                                                                                                                                                              0.0s
 =&gt; [auth] library/alpine:pull token for registry-1.docker.io                                                                                                                                                                                                                      0.0s
 =&gt; [internal] load build context                                                                                                                                                                                                                                                  0.0s
 =&gt; =&gt; transferring context: 7.92kB                                                                                                                                                                                                                                                0.0s
 =&gt; [stage-2 1/3] FROM docker.io/library/alpine:3.10.3@sha256:c19173c5ada610a5989151111163d28a67368362762534d8a8121ce95cf2bd5a                                                                                                                                                     0.0s
 =&gt; =&gt; resolve docker.io/library/alpine:3.10.3@sha256:c19173c5ada610a5989151111163d28a67368362762534d8a8121ce95cf2bd5a                                                                                                                                                             0.0s
 =&gt; CACHED [builder 1/7] FROM docker.io/adoptopenjdk/openjdk11:jdk-11.0.5_10-alpine@sha256:651c0c23656fe6e57c5ae6b9953a8c9bb23ba3f53e55b2c6df6ef243a3d07d8b                                                                                                                        0.0s
 =&gt; =&gt; resolve docker.io/adoptopenjdk/openjdk11:jdk-11.0.5_10-alpine@sha256:651c0c23656fe6e57c5ae6b9953a8c9bb23ba3f53e55b2c6df6ef243a3d07d8b                                                                                                                                       0.0s
 =&gt; [builder 2/7] ADD . /src                                                                                                                                                                                                                                                       0.1s
 =&gt; [builder 3/7] WORKDIR /src                                                                                                                                                                                                                                                     0.1s
 =&gt; [builder 4/7] RUN  ./mvnw package -DskipTests                                                                                                                                                                                                                                 90.3s
 =&gt; [builder 5/7] RUN addgroup --system --gid 1001  user &amp;&amp; adduser --system --uid 1001  user --ingroup  user                                                                                                                                                                      0.7s
 =&gt; [builder 6/7] RUN chown -R user:user /opt                                                                                                                                                                                                                                      6.0s
 =&gt; [builder 7/7] RUN chown -R user:user /src                                                                                                                                                                                                                                      1.4s
 =&gt; CACHED [packager 2/3] RUN apk --no-cache add openjdk11-jdk openjdk11-jmods                                                                                                                                                                                                     0.0s
 =&gt; CACHED [packager 3/3] RUN /usr/lib/jvm/java-11-openjdk/bin/jlink     --verbose     --add-modules         java.base,java.naming,java.desktop,java.management,java.security.jgss,java.instrument     --compress 2 --strip-debug --no-header-files --no-man-pages       --output  0.0s
 =&gt; CACHED [stage-2 2/3] COPY  --from=packager /opt/java-minimal /opt/java-minimal                                                                                                                                                                                                 0.0s
 =&gt; [stage-2 3/3] COPY  --from=builder /src/target/Calculator-0.0.1-SNAPSHOT.jar Calculator.jar                                                                                                                                                                                    0.1s
 =&gt; exporting to image                                                                                                                                                                                                                                                             0.1s
 =&gt; =&gt; exporting layers                                                                                                                                                                                                                                                            0.1s
 =&gt; =&gt; writing image sha256:b0fdb510c783e96e3e5f4f78e42210370b5ef4250df91771d06b997a39515355                                                                                                                                                                                       0.0s
 =&gt; =&gt; naming to docker.io/sladkkov/calculator:1.0.0
</code></pre>
<p class="has-line-data" data-line-start="137" data-line-end="138">Посмотрим список image</p>
<pre><code class="has-line-data" data-line-start="139" data-line-end="142">REPOSITORY                    TAG       IMAGE ID       CREATED          SIZE
sladkkov/calculator           1.0.0     b0fdb510c783   3 minutes ago    81.5MB
</code></pre>
<p class="has-line-data" data-line-start="142" data-line-end="144">Запустим Docker container из созданного image<br>
-docker run -ti --rm -p 9090:9090 --name calculator sladkkov/calculator:1.0.0</p>
<pre><code class="has-line-data" data-line-start="145" data-line-end="164">
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v2.6.6)

2022-06-17 03:32:16.273  INFO 1 --- [           main] r.s.calculator.CalculatorApplication     : Starting CalculatorApplication v0.0.1-SNAPSHOT using Java 11.0.4 on 35ff9db9b783 with PID 1 (/Calculator.jar started by root in /)
2022-06-17 03:32:16.278  INFO 1 --- [           main] r.s.calculator.CalculatorApplication     : No active profile set, falling back to 1 default profile: &quot;default&quot;
2022-06-17 03:32:17.758  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 9090 (http)
2022-06-17 03:32:17.780  INFO 1 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2022-06-17 03:32:17.781  INFO 1 --- [           main] org.apache.catalina.core.StandardEngine  : Starting Servlet engine: [Apache Tomcat/9.0.60]
2022-06-17 03:32:17.862  INFO 1 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2022-06-17 03:32:17.862  INFO 1 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 1477 ms
2022-06-17 03:32:19.339  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 9090 (http) with context path ''
2022-06-17 03:32:19.356  INFO 1 --- [           main] r.s.calculator.CalculatorApplication     : Started CalculatorApplication in 3.746 seconds (JVM running for 4.293)
</code></pre>
<p class="has-line-data" data-line-start="164" data-line-end="165">Проверим перейдя по ссылке, <a href="http://localhost:9090/swagger-ui/index.html#/Calculation%20controller/multiply">http://localhost:9090/swagger-ui/index.html#/Calculation controller/multiply</a>, что приложение работает.</p>
<p class="has-line-data" data-line-start="166" data-line-end="168">Отправим Docker image в Registry Docker Hub<br>
docker push sladkkov/calculator:1.0.0</p>
<h2 class="code-line" data-line-start=168 data-line-end=169 ><a id="_5_168"></a>Шаг 5</h2>
<pre><code class="has-line-data" data-line-start="170" data-line-end="177">PS C:\Users\sladk\Downloads\control-work-time-main\RestApiCalculator&gt;  docker push sladkkov/calculator:1.0.0
The push refers to repository [docker.io/sladkkov/calculator]
aa27681dfd45: Pushed
1052af976019: Layer already exists
77cae8ab23bf: Layer already exists
1.0.0: digest: sha256:e9e75476dac11c05cf31175b7342f21ab625e4341564d23b508adb68c5b4f991 size: 952
</code></pre>
<p class="has-line-data" data-line-start="177" data-line-end="178">Откроем web-интерфейс Docker Hub по адресу и найдем загруженный image</p>
<pre><code class="has-line-data" data-line-start="179" data-line-end="185">docker pull sladkkov/calculator:1.0.0
1.0.0: Pulling from sladkkov/calculator
Digest: sha256:e9e75476dac11c05cf31175b7342f21ab625e4341564d23b508adb68c5b4f991
Status: Image is up to date for sladkkov/calculator:1.0.0
docker.io/sladkkov/calculator:1.0.0
</code></pre>
<h2 class="code-line" data-line-start=185 data-line-end=186 ><a id="_6_185"></a>Шаг 6</h2>
<p class="has-line-data" data-line-start="186" data-line-end="187">Запуск кластера</p>
<pre><code class="has-line-data" data-line-start="188" data-line-end="203">PS C:\Users\sladk\Downloads\control-work-time-main\RestApiCalculator&gt; minikube start --embed-certs
😄  minikube v1.25.2 на Microsoft Windows 11 Home Single Language 10.0.22000 Build 22000
✨  Используется драйвер docker на основе существующего профиля
👍  Запускается control plane узел minikube в кластере minikube
🚜  Скачивается базовый образ ...
🔄  Перезагружается существующий docker container для &quot;minikube&quot; ...
❗  This container is having trouble accessing https://k8s.gcr.io
💡  To pull new external images, you may need to configure a proxy: https://minikube.sigs.k8s.io/docs/reference/networking/proxy/
🐳  Подготавливается Kubernetes v1.23.3 на Docker 20.10.12 ...
    ▪ kubelet.housekeeping-interval=5m
🔎  Компоненты Kubernetes проверяются ...
    ▪ Используется образ gcr.io/k8s-minikube/storage-provisioner:v5
🌟  Включенные дополнения: storage-provisioner, default-storageclass
🏄  Готово! kubectl настроен для использования кластера &quot;minikube&quot; и &quot;default&quot; пространства имён по умолчанию
</code></pre>
<p class="has-line-data" data-line-start="203" data-line-end="204">Просмотрим статус кластера</p>
<pre><code class="has-line-data" data-line-start="205" data-line-end="213">PS C:\Users\sladk\Downloads\control-work-time-main\RestApiCalculator&gt; minikube status
minikube
type: Control Plane
host: Running
kubelet: Running
apiserver: Running
kubeconfig: Configured
</code></pre>
<p class="has-line-data" data-line-start="213" data-line-end="214">Посмотрим файл конфига kubeconfig</p>
<pre><code class="has-line-data" data-line-start="215" data-line-end="249">kubectl config view
apiVersion: v1
clusters:
- cluster:
    certificate-authority-data: DATA+OMITTED
    extensions:
    - extension:
        last-update: Fri, 17 Jun 2022 07:45:32 +04
        provider: minikube.sigs.k8s.io
        version: v1.25.2
      name: cluster_info
    server: https://127.0.0.1:59245
  name: minikube
contexts:
- context:
    cluster: minikube
    extensions:
    - extension:
        last-update: Fri, 17 Jun 2022 07:45:32 +04
        provider: minikube.sigs.k8s.io
        version: v1.25.2
      name: context_info
    namespace: default
    user: minikube
  name: minikube
current-context: minikube
kind: Config
preferences: {}
users:
- name: minikube
  user:
    client-certificate-data: REDACTED
    client-key-data: REDACTED
</code></pre>
<p class="has-line-data" data-line-start="249" data-line-end="250">Проверка подключения к кластеру.</p>
<pre><code class="has-line-data" data-line-start="251" data-line-end="255">kubectl cluster-info
Kubernetes control plane is running at https://127.0.0.1:59245
CoreDNS is running at https://127.0.0.1:59245/api/v1/namespaces/kube-system/services/kube-dns:dns/proxy
</code></pre>
<p class="has-line-data" data-line-start="255" data-line-end="256">Создадим pod manifest</p>
<pre><code class="has-line-data" data-line-start="257" data-line-end="269">apiVersion: v1
kind: Pod
metadata:
  name: calculator
  labels:
    env: test
spec:
  containers:
    - name: calculator
      image: sladkkov/calculator:1.0.0
      imagePullPolicy: IfNotPresent
</code></pre>
<p class="has-line-data" data-line-start="269" data-line-end="270">Применим manifest. Манифест ранее уже был создан</p>
<pre><code class="has-line-data" data-line-start="271" data-line-end="274">PS C:\Users\sladk\Downloads\control-work-time-main\RestApiCalculator&gt; kubectl apply -f pod.yaml -n default
pod/calculator unchanged
</code></pre>
<p class="has-line-data" data-line-start="275" data-line-end="276">Проверим установку манифеста</p>
<pre><code class="has-line-data" data-line-start="277" data-line-end="338">PS C:\Users\sladk\Downloads\control-work-time-main\RestApiCalculator&gt; kubectl describe pod calculator -n default
Name:         calculator
Namespace:    default
Priority:     0
Node:         minikube/192.168.49.2
Start Time:   Fri, 17 Jun 2022 06:40:05 +0400
Labels:       env=test
Annotations:  &lt;none&gt;
Status:       Running
IP:           172.17.0.2
IPs:
  IP:  172.17.0.2
Containers:
  calculator:
    Container ID:   docker://8389d4f5f2a6032c584e3fad026da80b8af49f9c1314affc0a00a4e1628bde96
    Image:          sladkkov/calculator:1.0.0
    Image ID:       docker-pullable://sladkkov/calculator@sha256:d5fbbf62745a42c04b3b2008465bd835fcc44af54dbff369b1584feaa0d692a1
    Port:           &lt;none&gt;
    Host Port:      &lt;none&gt;
    State:          Running
      Started:      Fri, 17 Jun 2022 07:45:36 +0400
    Last State:     Terminated
      Reason:       Error
      Exit Code:    143
      Started:      Fri, 17 Jun 2022 06:40:06 +0400
      Finished:     Fri, 17 Jun 2022 06:42:14 +0400
    Ready:          True
    Restart Count:  1
    Environment:    &lt;none&gt;
    Mounts:
      /var/run/secrets/kubernetes.io/serviceaccount from kube-api-access-2j4fp (ro)
Conditions:
  Type              Status
  Initialized       True
  Ready             True
  ContainersReady   True
  PodScheduled      True
Volumes:
  kube-api-access-2j4fp:
    Type:                    Projected (a volume that contains injected data from multiple sources)
    TokenExpirationSeconds:  3607
    ConfigMapName:           kube-root-ca.crt
    ConfigMapOptional:       &lt;nil&gt;
    DownwardAPI:             true
QoS Class:                   BestEffort
Node-Selectors:              &lt;none&gt;
Tolerations:                 node.kubernetes.io/not-ready:NoExecute op=Exists for 300s
                             node.kubernetes.io/unreachable:NoExecute op=Exists for 300s
Events:
  Type    Reason          Age    From               Message
  ----    ------          ----   ----               -------
  Normal  Scheduled       70m    default-scheduler  Successfully assigned default/calculator to minikube
  Normal  Pulled          70m    kubelet            Container image &quot;sladkkov/calculator:1.0.0&quot; already present on machine
  Normal  Created         70m    kubelet            Created container calculator
  Normal  Started         70m    kubelet            Started container calculator
  Normal  SandboxChanged  5m23s  kubelet            Pod sandbox changed, it will be killed and re-created.
  Normal  Pulled          5m18s  kubelet            Container image &quot;sladkkov/calculator:1.0.0&quot; already present on machine
  Normal  Created         5m18s  kubelet            Created container calculator
  Normal  Started         5m18s  kubelet            Started container calculator
PS C:\Users\sladk\Downloads\control-work-time-main\RestApiCalculator&gt;
</code></pre>
<p class="has-line-data" data-line-start="339" data-line-end="340">Пробросим наружу порт web-приложения.</p>
<pre><code class="has-line-data" data-line-start="341" data-line-end="348">port-forward pods/calculator 9090:9090
Forwarding from 127.0.0.1:9090 -&gt; 9090
Forwarding from [::1]:9090 -&gt; 9090
Handling connection for 9090
Handling connection for 9090
Handling connection for 9090
</code></pre>
<p class="has-line-data" data-line-start="348" data-line-end="349">Удалим Pod с web-приложением</p>
<pre><code class="has-line-data" data-line-start="350" data-line-end="353">PS C:\Users\sladk\Downloads\control-work-time-main\RestApiCalculator&gt;  kubectl delete po calculator
pod &quot;calculator&quot; deleted
</code></pre>
<p class="has-line-data" data-line-start="353" data-line-end="354">Остановим minikube</p>
<pre><code class="has-line-data" data-line-start="355" data-line-end="362">PS C:\Users\sladk\Downloads\control-work-time-main\RestApiCalculator&gt;
                                                                      minikube stop
✋  Узел &quot;minikube&quot; останавливается ...
🛑  Выключается &quot;minikube&quot; через SSH ...
🛑  Остановлено узлов: 1.

</code></pre>
