
# Spring Boot 统一回应格式 & 统一异常处理

默认直接返回对象的 json 格式，对客户端解析不方便，所以我们一般使用统一的格式进行封装。
本工程直接在 SpringBoot + Jersey 框架基础上实现。

> 题外话： 如果是 SpringMVC 框架，是通过使用 @ControllerAdvice 注解来定义统一的异常处理类的。

## 说明

在本工程中新增的 rest 包里面实现了以下类：

（1）正常 Response 类： `BaseResponse`、 `SimpleResponse`、 `ListResponse`、 `PageResponse`，分别对应格式为：

```
{
	"success": true
}
```

```
{
	"success": true;
	"data": {
	}
}
```

```
{
	"success": true;
	"datas": [
	]
}
```

```
{
    "success": true,
    "datas": [],
    "totalPages": 0,
    "totalElements": 0
}
```

（2）错误 Response 类： `ErrorCode`、 `ErrorResponse`、 `ErrorException`

格式：

```
{
	"success": false,
	"error": "Bad Request",
	"errorCode": 400
}
```

通过 ErrorCode 的 build 方法构建 ErrorResponse 对象和 ErrorException 对象。
一般在 resource 类中直接构造 ErrorResponse 即可，而在 service 类中则构造 ErrorException 抛出异常。


(3) 新建一个 GlobalExceptionHandler 类实现 ExceptionMapper 接口类，重写 toResponse 方法接收抛出的异常并创建一个用于构建 HTTP 响应的 Response 对象。

Jersey 是 JAX-RS 的实现框架之一，而 ExceptionMapper 是 JAX-RS 的接口类，所以该方法也适用于其他 JAX-RS 实现框架，如 `RESTEasy`。

```
@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {

        System.out.println("Handling exception: " + exception);

        if (exception instanceof ErrorException) {
            ErrorException e = (ErrorException) exception;
            return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(
                    new ErrorResponse(e.getError(), e.getStatus())).build();
        }

        if (exception instanceof WebApplicationException) {
            WebApplicationException e = (WebApplicationException)exception;

            Response.StatusType status = e.getResponse().getStatusInfo();

            return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(
                    new ErrorResponse(status.getReasonPhrase(), status.getStatusCode())).build();
        }

        // 其他异常作为服务内部错误返回，如 NullPointerException
        return ErrorCode.INTERNAL_SERVER_ERROR.buildResponse();
    }

}
```

## 测试

```
$ curl 127.0.0.1:8080/users/0 -s
{"success":true,"data":{"id":0,"name":"zhugeliang","nick":"kongming"}}

$ curl 127.0.0.1:8080/users/2 -s
{"success":false,"error":"Not Found","errorCode":404}

$ curl 127.0.0.1:8080/users/ -XPOST -d '{"name":"guanyu"}' -H "Content-Type: application/json" -s
{"success":false,"error":"Empty Parameter","errorCode":1000}

$ curl 127.0.0.1:8080/users/ -XPOST -d '{"name":"guanyu", "nick":"yunchang"}' -H "Content-Type: application/json" -s
{"success":true}

$ curl 127.0.0.1:8080/users -s
{"success":true,"datas":[{"id":0,"name":"zhugeliang","nick":"kongming"},{"id":1,"name":"liubei","nick":"xuande"},{"id":2,"name":"guanyu","nick":"yunchang"}]}

```

对比之前的api测试结果：
```
$ curl 127.0.0.1:8080/users/0 -s
{"id":0,"name":"zhugeliang","nick":"kongming"}

$ curl 127.0.0.1:8080/users/2 -s
（返回 404）

$ curl 127.0.0.1:8080/users/ -XPOST -d '{"name":"guanyu"}' -H "Content-Type: application/json" -s
Please provide all mandatory inputs

$ curl 127.0.0.1:8080/users/ -XPOST -d '{"name":"guanyu", "nick":"yunchang"}' -H "Content-Type: application/json" -s

$ curl 127.0.0.1:8080/users -s
[{"id":0,"name":"zhugeliang","nick":"kongming"},{"id":1,"name":"liubei","nick":"xuande"},{"id":2,"name":"guanyu","nick":"yunchang"}]

```


测试异常返回：

```
$ curl 172.16.1.188:8080/users/exception1 -s
{"success":false,"error":"Bad Request","errorCode":400}

$ curl 172.16.1.188:8080/users/exception2 -s
{"success":false,"error":"Internal Server Error","errorCode":500}

$ curl 172.16.1.188:8080/users/exception3 -s
{"success":false,"error":"Internal Server Error","errorCode":500}

```


## 补充

之前工程只有 UserResource 一个资源，所以 ResourceConfig 中只调用 `register` 对其进行注册：

```
register(UserResource.class);
```

现在需要把整个包都注册进来，可以使用 `packages` 方法：

```
@Component
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig() {
        //register(UserResource.class);

        // 使用 packages 扫描加载所需资源
        packages("com.example.jerseydemo");
    }
}
```


## 参考

[Jersey exception handling – Jersey ExceptionMapper Example](https://howtodoinjava.com/jersey/jaxrs-jersey-exceptionmapper/)
[Jersey ExceptionMapper doesn't map exceptions](https://stackoverflow.com/questions/27982948/jersey-exceptionmapper-doesnt-map-exceptions)
[RESTfu­­l Jav­a­ wit­h ­JAX­-­­RS 2.­0­ (Second Edition) - Exception Handling](https://dennis-xlc.gitbooks.io/restful-java-with-jax-rs-2-0-2rd-edition/en/part1/chapter7/exception_handling.html)