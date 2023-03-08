package top.iot.gateway.simulator.core.benchmark;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.vertx.core.buffer.Buffer;
import org.hswebframework.web.utils.DigestUtils;
import top.iot.gateway.reactor.ql.utils.CastUtils;
import org.joda.time.DateTime;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public interface BenchmarkHelper {

    Object require(String location);

    default ByteBuf newBuffer() {
        return Unpooled.buffer();
    }

    default String formatDate(Object time, String format) {
        return new DateTime(CastUtils.castDate(time)).toString(format);
    }

    default long now() {
        return System.currentTimeMillis();
    }

    default String now(String format) {
        return formatDate(now(), format);
    }

    default String toJson(Object obj) {
        return JSON.toJSONString(Benchmark.scriptFactory.convertToJavaType(obj));
    }

    default Object parseJson(Object obj) {
        obj = Benchmark.scriptFactory.convertToJavaType(obj);

        if (obj instanceof Buffer) {
            obj = ((Buffer) obj).toString(StandardCharsets.UTF_8);
        } else if (obj instanceof ByteBuf) {
            obj = ((ByteBuf) obj).toString(StandardCharsets.UTF_8);
        } else if (obj instanceof String) {
            return JSON.parseObject(String.valueOf(obj));
        }

        return obj;
    }

    default Object toJavaType(Object obj) {
        return Benchmark.scriptFactory.convertToJavaType(obj);
    }

    default String md5(Object obj) {
        return DigestUtils.md5Hex(String.valueOf(Benchmark.scriptFactory.convertToJavaType(obj)));
    }

    default float randomFloat(float from, float to) {
        return (float) ThreadLocalRandom.current().nextDouble(from, to);
    }

    default int randomInt(int from, int to) {
        return ThreadLocalRandom.current().nextInt(from, to);
    }

    default <T> List<T> newArrayList() {
        return new ArrayList<>();
    }

    default <K, V> Map<String, Object> newHashMap() {
        return new HashMap<>();
    }

}
