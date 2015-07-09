package com.examples.vertx;


import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


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

    router.post("/form").handler(BodyHandler.create().setMergeFormAttributes(true));
    router.post("/form")
      .handler(routingContext -> {

        Set<FileUpload> fileUploadSet = routingContext.fileUploads();
        Iterator<FileUpload> fileUploadIterator = fileUploadSet.iterator();
        while (fileUploadIterator.hasNext()){
          FileUpload fileUpload = fileUploadIterator.next();

          // To get the uploaded file do
          Buffer uploadedFile = vertx.fileSystem().readFileBlocking(fileUpload.uploadedFileName());

          // Uploaded File Name
          try {
            String fileName = URLDecoder.decode(fileUpload.fileName(), "UTF-8");
          } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
          }

          // Use the Event Bus to dispatch the file now
          // Since Event Bus does not support POJOs by default so we need to create a MessageCodec implementation
          // and provide methods for encode and decode the bytes
        }


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