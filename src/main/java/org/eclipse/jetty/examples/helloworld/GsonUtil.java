package org.eclipse.jetty.examples.helloworld;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import io.grpc.examples.helloworld.HelloReply;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class GsonUtil {

    public static String toJson(Message message) {
        return getGson(message.getClass()).toJson(message);
    }

    public static <T extends Message> T toMessage(Class<T> klass, String json) {
        return getGson(klass).fromJson(json, klass);
    }

    private static <E extends Message> Gson getGson(Class<E> messageClass) {
        return  new GsonBuilder().registerTypeAdapter(messageClass, new MessageAdapter<>(messageClass)).create();
    }

    public static void printJson(HttpServletResponse response, HelloReply reply) throws IOException {
        response.getWriter().println(toJson(reply));

    }

    private static class MessageAdapter<E extends Message> extends TypeAdapter<E> {

        private final Class<E> messageClass;

        public MessageAdapter(Class<E> messageClass) {
            this.messageClass = messageClass;
        }

        @Override
        public void write(JsonWriter jsonWriter, E value) throws IOException {
            jsonWriter.jsonValue(JsonFormat.printer().print(value));
        }

        @Override
        public E read(JsonReader jsonReader) throws IOException {
            try {
                Method method = messageClass.getMethod("newBuilder");
                E.Builder builder = (E.Builder) method.invoke(null);
                JsonFormat.parser().merge(JsonParser.parseReader(jsonReader).toString(), builder);
                return (E) builder.build();
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new IOException(e);
            }
        }
    }
}