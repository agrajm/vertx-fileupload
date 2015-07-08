package com.examples.vertx;


import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

import java.net.URLDecoder;
import java.util.List;
import java.util.Map;


/**
 * @author <a href="agraj.mng@gmail.com">Agraj Mangal</a>
 */
public class SimpleFormUploadServer extends AbstractVerticle {

  private EventBus eventBus;

  private static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
  private static final String CONTENT_TYPE = "Content-Type";
  private static final int BAD_REQUEST_ERROR_CODE = 400;


  @Override
  public void start() {

    HttpServer httpServer = vertx.createHttpServer();
    eventBus = vertx.eventBus();
    Router router = Router.router(vertx);

    router.get("/").handler(getContext -> {
      getContext.request().response().sendFile("index.html");
    });

    router.post("/form")
      .handler(routingContext -> {

        final HttpServerRequest request = routingContext.request();

        // Upload Handler
        request.setExpectMultipart(true);
        request.uploadHandler(upload -> {
          upload.exceptionHandler(exception -> {
            request.response().end("Upload failed");
          });
          upload.endHandler(success -> {
            request.response().end("Upload successful, you should see the file in the server directory");
          });
          upload.streamToFileSystem(upload.filename());
        });

        // End Handler
        request.endHandler(handler -> {
          for (Map.Entry<String, String> entry : request.formAttributes()) {
            request.response().write("Got attr " + entry.getKey() + " : " + entry.getValue() + "\n");
          }
          request.response().end();
        });


        // Handling Form Encoded Parameters
        /*request.bodyHandler(handler -> {

          Buffer buff = handler.getBuffer(0, handler.length());
          JsonObject paramMap = new JsonObject();
          String contentType = request.headers().get(CONTENT_TYPE);
          if (APPLICATION_X_WWW_FORM_URLENCODED.equals(contentType)) {
            paramMap = this.getQueryMap(buff.toString()); // takes the buffer string and returns map of post params
          } else {
            paramMap = routingContext.getBodyAsJson();
          }
          //TODO Send paramMap to EventBus for processing by Worker Verticles
        }); */


      });

    httpServer.requestHandler(router::accept).listen(8080);
  }


    private JsonObject getRequestParams(MultiMap params){

      JsonObject paramMap = new JsonObject();
      for( Map.Entry entry: params.entries()){
        String key = (String)entry.getKey();
        Object value = entry.getValue();
        if(value instanceof List){
          value = (List<String>) entry.getValue();
        }
        else{
          value = (String) entry.getValue();
        }
        paramMap.put(key, value);
      }
      return paramMap;
    }

    private static JsonObject getQueryMap(String query)
    {
      String[] params = query.split("&");
      JsonObject map = new JsonObject();
      for (String param : params) {
        String name = param.split("=")[0];
        String value = "";
        try {
          value = URLDecoder.decode(param.split("=")[1], "UTF-8");
        } catch (Exception e) {
        }
        map.put(name, value);
      }
      return map;
    }
}