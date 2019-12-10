# 一个简单的 kafka 生产者/消费者 springboot工程

## 1. 引入依赖

```
        <dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka</artifactId>
        </dependency>
```



## 2. 定义消息格式（Event 类）

id 标识一个消息

type 标识消息类型，方便业务区分处理，只处理自己感兴趣的消息

source 标识消息来源（生产者）

data 为消息内容

```java
@JsonIgnoreProperties(ignoreUnknown = true)
public class Event<T> {

    private String id;
    private String type;
    private String source;
    private T data;

    public Event() {
    }

    public Event(String id, String type, String source, T data) {
        this.id = id;
        this.type = type;
        this.source = source;
        this.data = data;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
```



## 3. 继承 KakfaTemplate，实现消息发送

将 Event 中与业务无关的字段 id、source、topic 封装进去（不需要每次调用发送接口都传入）。

```java
public class ProducerTemplate extends KafkaTemplate<String, String> {

    private String source;
    private String topic;


    public ProducerTemplate(String source, String topic, ProducerFactory<String, String> producerFactory) {
        this(source, topic, producerFactory, false);
    }

    public ProducerTemplate(String source, String topic, ProducerFactory<String, String> producerFactory, boolean autoFlush) {
        super(producerFactory, autoFlush);
        this.source = source;
        this.topic = topic;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public <T> SendResult<String, String> sendEvent(String type, T data) throws ExecutionException, InterruptedException {
        String dataJson = JsonUtils.pojoToJson(data);
        String id = UUID.randomUUID().toString();
        Event<String> event = new Event<>(id, type, source, dataJson);
        return sendEvent(event);
    }

    public <T> SendResult<String, String> sendEvent(Event<T> event) throws ExecutionException, InterruptedException {
        String eventJson = JsonUtils.pojoToJson(event);

        if (eventJson == null) {
            // TODO
        }

        ListenableFuture<SendResult<String, String>> future = this.send(topic, event.getId(), eventJson);
        return future.get();
    }
}
```



## 4. 配置 kafka 生产者 producer

配置 kafka 消息中 key 和 value 序列化方式

并创建 ProducerTemplate 的 Bean

```java
@Configuration
public class KafkaProducerConfig {

    @Value(value = "${kafka.bootstrap.servers}")
    private String bootstrapServers;

    @Value(value = "${kafka.producer.topic}")
    private String topic;

    @Value(value = "${server.name}")
    private String serverName; // 服务名称，作为 kafka event 中的 source，表明发送源

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public ProducerTemplate producerTemplate() {
        return new ProducerTemplate(serverName, topic, producerFactory());
    }
}
```



## 5. 配置 kafka 消费者 consumer

配置 kafka 消息中 key 和 value 反序列化方式

并通过 @KafkaListener 注解启动 kafka 消费者

```java
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Value(value = "${kafka.bootstrap.servers}")
    private String bootstrapServers;

    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> listenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    @KafkaListener(topics = "${kafka.consumer.topics}", groupId = "${kafka.consumer.groupid}", containerFactory = "listenerContainerFactory")
    public void receive(String payload) {
        System.out.println("receive kafka message:" + payload);

        Event<String> event = JsonUtils.jsonToObject(payload, new TypeReference<Event<String>>() {
        });

        if (event != null) {
            System.out.println("id: " +  event.getId() + ", source: " + event.getSource() + " , type: " + event.getType());
        } else {
            System.out.println("event is null!");
        }
    }
}
```



## 通过一个定时任务向 kafka 发送消息

启动后 5s 开始发送，每隔 2s 发送一次。

```java
@Service
public class KafkaService {

    @Autowired
    private ProducerTemplate producerTemplate;

    @Scheduled(initialDelay = 5000, fixedDelay = 2000)
    public void send() {

        String id = UUID.randomUUID().toString();
        String type = "test";
        String source = "A";
        String data = new Date().toString();

        Event<String> event = new Event<>(id, type, source, data);
        try {
            producerTemplate.sendEvent(event);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```



## 运行结果

```
Connected to the target VM, address: '127.0.0.1:0', transport: 'socket'

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v2.2.2.RELEASE)

2019-12-10 11:29:48.660  INFO 14728 --- [           main] com.example.demo.DemoApplication         : Starting DemoApplication on DESKTOP-FFIPPK4 with PID 14728 (D:\codes\demo\kafka\target\classes started by won in D:\codes\demo\kafka)
2019-12-10 11:29:48.664  INFO 14728 --- [           main] com.example.demo.DemoApplication         : No active profile set, falling back to default profiles: default
2019-12-10 11:29:49.252  INFO 14728 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8080 (http)
2019-12-10 11:29:49.257  INFO 14728 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2019-12-10 11:29:49.257  INFO 14728 --- [           main] org.apache.catalina.core.StandardEngine  : Starting Servlet engine: [Apache Tomcat/9.0.29]
2019-12-10 11:29:49.306  INFO 14728 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2019-12-10 11:29:49.307  INFO 14728 --- [           main] o.s.web.context.ContextLoader            : Root WebApplicationContext: initialization completed in 611 ms
2019-12-10 11:29:49.438  INFO 14728 --- [           main] o.s.s.concurrent.ThreadPoolTaskExecutor  : Initializing ExecutorService 'applicationTaskExecutor'
2019-12-10 11:29:49.533  INFO 14728 --- [           main] o.s.s.c.ThreadPoolTaskScheduler          : Initializing ExecutorService 'taskScheduler'
2019-12-10 11:29:49.576  INFO 14728 --- [           main] o.a.k.clients.consumer.ConsumerConfig    : ConsumerConfig values: 
	allow.auto.create.topics = true
	auto.commit.interval.ms = 5000
	auto.offset.reset = latest
	bootstrap.servers = [172.16.1.225:9092]
	check.crcs = true
	client.dns.lookup = default
	client.id = 
	client.rack = 
	connections.max.idle.ms = 540000
	default.api.timeout.ms = 60000
	enable.auto.commit = false
	exclude.internal.topics = true
	fetch.max.bytes = 52428800
	fetch.max.wait.ms = 500
	fetch.min.bytes = 1
	group.id = group-test
	group.instance.id = null
	heartbeat.interval.ms = 3000
	interceptor.classes = []
	internal.leave.group.on.close = true
	isolation.level = read_uncommitted
	key.deserializer = class org.apache.kafka.common.serialization.StringDeserializer
	max.partition.fetch.bytes = 1048576
	max.poll.interval.ms = 300000
	max.poll.records = 500
	metadata.max.age.ms = 300000
	metric.reporters = []
	metrics.num.samples = 2
	metrics.recording.level = INFO
	metrics.sample.window.ms = 30000
	partition.assignment.strategy = [class org.apache.kafka.clients.consumer.RangeAssignor]
	receive.buffer.bytes = 65536
	reconnect.backoff.max.ms = 1000
	reconnect.backoff.ms = 50
	request.timeout.ms = 30000
	retry.backoff.ms = 100
	sasl.client.callback.handler.class = null
	sasl.jaas.config = null
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.min.time.before.relogin = 60000
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	sasl.kerberos.ticket.renew.window.factor = 0.8
	sasl.login.callback.handler.class = null
	sasl.login.class = null
	sasl.login.refresh.buffer.seconds = 300
	sasl.login.refresh.min.period.seconds = 60
	sasl.login.refresh.window.factor = 0.8
	sasl.login.refresh.window.jitter = 0.05
	sasl.mechanism = GSSAPI
	security.protocol = PLAINTEXT
	send.buffer.bytes = 131072
	session.timeout.ms = 10000
	ssl.cipher.suites = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.endpoint.identification.algorithm = https
	ssl.key.password = null
	ssl.keymanager.algorithm = SunX509
	ssl.keystore.location = null
	ssl.keystore.password = null
	ssl.keystore.type = JKS
	ssl.protocol = TLS
	ssl.provider = null
	ssl.secure.random.implementation = null
	ssl.trustmanager.algorithm = PKIX
	ssl.truststore.location = null
	ssl.truststore.password = null
	ssl.truststore.type = JKS
	value.deserializer = class org.apache.kafka.common.serialization.StringDeserializer

2019-12-10 11:29:49.610  INFO 14728 --- [           main] o.a.kafka.common.utils.AppInfoParser     : Kafka version: 2.3.1
2019-12-10 11:29:49.611  INFO 14728 --- [           main] o.a.kafka.common.utils.AppInfoParser     : Kafka commitId: 18a913733fb71c01
2019-12-10 11:29:49.611  INFO 14728 --- [           main] o.a.kafka.common.utils.AppInfoParser     : Kafka startTimeMs: 1575948589609
2019-12-10 11:29:49.612  INFO 14728 --- [           main] o.a.k.clients.consumer.KafkaConsumer     : [Consumer clientId=consumer-1, groupId=group-test] Subscribed to topic(s): topic-test
2019-12-10 11:29:49.613  INFO 14728 --- [           main] o.s.s.c.ThreadPoolTaskScheduler          : Initializing ExecutorService
2019-12-10 11:29:49.627  INFO 14728 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
2019-12-10 11:29:49.629  INFO 14728 --- [           main] com.example.demo.DemoApplication         : Started DemoApplication in 1.205 seconds (JVM running for 1.658)
2019-12-10 11:29:49.784  INFO 14728 --- [ntainer#0-0-C-1] org.apache.kafka.clients.Metadata        : [Consumer clientId=consumer-1, groupId=group-test] Cluster ID: J0UGCxe9SKactYcPKxXL_A
2019-12-10 11:29:49.819  INFO 14728 --- [ntainer#0-0-C-1] o.a.k.c.c.internals.AbstractCoordinator  : [Consumer clientId=consumer-1, groupId=group-test] Discovered group coordinator kk01:9091 (id: 2147483646 rack: null)
2019-12-10 11:29:49.821  INFO 14728 --- [ntainer#0-0-C-1] o.a.k.c.c.internals.ConsumerCoordinator  : [Consumer clientId=consumer-1, groupId=group-test] Revoking previously assigned partitions []
2019-12-10 11:29:49.821  INFO 14728 --- [ntainer#0-0-C-1] o.s.k.l.KafkaMessageListenerContainer    : group-test: partitions revoked: []
2019-12-10 11:29:49.821  INFO 14728 --- [ntainer#0-0-C-1] o.a.k.c.c.internals.AbstractCoordinator  : [Consumer clientId=consumer-1, groupId=group-test] (Re-)joining group
2019-12-10 11:29:49.829  INFO 14728 --- [ntainer#0-0-C-1] o.a.k.c.c.internals.AbstractCoordinator  : [Consumer clientId=consumer-1, groupId=group-test] (Re-)joining group
2019-12-10 11:29:49.837  INFO 14728 --- [ntainer#0-0-C-1] o.a.k.c.c.internals.AbstractCoordinator  : [Consumer clientId=consumer-1, groupId=group-test] Successfully joined group with generation 11
2019-12-10 11:29:49.839  INFO 14728 --- [ntainer#0-0-C-1] o.a.k.c.c.internals.ConsumerCoordinator  : [Consumer clientId=consumer-1, groupId=group-test] Setting newly assigned partitions: topic-test-0
2019-12-10 11:29:49.846  INFO 14728 --- [ntainer#0-0-C-1] o.a.k.c.c.internals.ConsumerCoordinator  : [Consumer clientId=consumer-1, groupId=group-test] Setting offset for partition topic-test-0 to the committed offset FetchPosition{offset=140, offsetEpoch=Optional.empty, currentLeader=LeaderAndEpoch{leader=kk04:9094 (id: 4 rack: null), epoch=0}}
2019-12-10 11:29:49.851  INFO 14728 --- [ntainer#0-0-C-1] o.s.k.l.KafkaMessageListenerContainer    : group-test: partitions assigned: [topic-test-0]
2019-12-10 11:29:54.651  INFO 14728 --- [   scheduling-1] o.a.k.clients.producer.ProducerConfig    : ProducerConfig values: 
	acks = 1
	batch.size = 16384
	bootstrap.servers = [172.16.1.225:9092]
	buffer.memory = 33554432
	client.dns.lookup = default
	client.id = 
	compression.type = none
	connections.max.idle.ms = 540000
	delivery.timeout.ms = 120000
	enable.idempotence = false
	interceptor.classes = []
	key.serializer = class org.apache.kafka.common.serialization.StringSerializer
	linger.ms = 0
	max.block.ms = 60000
	max.in.flight.requests.per.connection = 5
	max.request.size = 1048576
	metadata.max.age.ms = 300000
	metric.reporters = []
	metrics.num.samples = 2
	metrics.recording.level = INFO
	metrics.sample.window.ms = 30000
	partitioner.class = class org.apache.kafka.clients.producer.internals.DefaultPartitioner
	receive.buffer.bytes = 32768
	reconnect.backoff.max.ms = 1000
	reconnect.backoff.ms = 50
	request.timeout.ms = 30000
	retries = 2147483647
	retry.backoff.ms = 100
	sasl.client.callback.handler.class = null
	sasl.jaas.config = null
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.min.time.before.relogin = 60000
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	sasl.kerberos.ticket.renew.window.factor = 0.8
	sasl.login.callback.handler.class = null
	sasl.login.class = null
	sasl.login.refresh.buffer.seconds = 300
	sasl.login.refresh.min.period.seconds = 60
	sasl.login.refresh.window.factor = 0.8
	sasl.login.refresh.window.jitter = 0.05
	sasl.mechanism = GSSAPI
	security.protocol = PLAINTEXT
	send.buffer.bytes = 131072
	ssl.cipher.suites = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.endpoint.identification.algorithm = https
	ssl.key.password = null
	ssl.keymanager.algorithm = SunX509
	ssl.keystore.location = null
	ssl.keystore.password = null
	ssl.keystore.type = JKS
	ssl.protocol = TLS
	ssl.provider = null
	ssl.secure.random.implementation = null
	ssl.trustmanager.algorithm = PKIX
	ssl.truststore.location = null
	ssl.truststore.password = null
	ssl.truststore.type = JKS
	transaction.timeout.ms = 60000
	transactional.id = null
	value.serializer = class org.apache.kafka.common.serialization.StringSerializer

2019-12-10 11:29:54.661  INFO 14728 --- [   scheduling-1] o.a.kafka.common.utils.AppInfoParser     : Kafka version: 2.3.1
2019-12-10 11:29:54.661  INFO 14728 --- [   scheduling-1] o.a.kafka.common.utils.AppInfoParser     : Kafka commitId: 18a913733fb71c01
2019-12-10 11:29:54.661  INFO 14728 --- [   scheduling-1] o.a.kafka.common.utils.AppInfoParser     : Kafka startTimeMs: 1575948594661
2019-12-10 11:29:54.670  INFO 14728 --- [ad | producer-1] org.apache.kafka.clients.Metadata        : [Producer clientId=producer-1] Cluster ID: J0UGCxe9SKactYcPKxXL_A
receive kafka message:{"id":"cbb3340d-9582-49ff-afd4-2c14fc017a96","type":"test","source":"A","data":"Tue Dec 10 11:29:54 CST 2019"}
id: cbb3340d-9582-49ff-afd4-2c14fc017a96, source: A , type: test
receive kafka message:{"id":"eac7537b-c87d-47ef-a1d5-f228552a9290","type":"test","source":"A","data":"Tue Dec 10 11:29:56 CST 2019"}
id: eac7537b-c87d-47ef-a1d5-f228552a9290, source: A , type: test
```



