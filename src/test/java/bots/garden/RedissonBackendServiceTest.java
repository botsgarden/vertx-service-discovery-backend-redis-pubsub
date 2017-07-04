package bots.garden;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.types.HttpEndpoint;
import io.vertx.servicediscovery.Record;
import org.junit.Before;
import org.junit.Test;
import junit.framework.TestCase;
import java.util.concurrent.atomic.AtomicReference;
import static com.jayway.awaitility.Awaitility.await;

public class RedissonBackendServiceTest extends TestCase {
  Vertx vertx;
  RedissonBackendService redissonBackend;

  @Before
  public void setUp() {
    vertx = Vertx.vertx();

    redissonBackend = new RedissonBackendService();
    redissonBackend.init(vertx, new JsonObject()
      .put("channel", "redisson-topic")
      .put("key", "redisson-ms")
    );

  }

  @Test
  public void testServiceStore() throws Exception {
    // create the microservice record
    Record record = HttpEndpoint.createRecord(
      "awesome",
      "127.0.0.1",
      9091,
      "/api"
    );

    AtomicReference<String> action = new AtomicReference<>();

    redissonBackend.onStorePubEvent(ar -> {
      if(ar.succeeded()) {
        System.out.println(
          ar.result().getJsonObject("record")
        );
        action.set(ar.result().getString("action"));
      }
    }).onErrorPubEvent(ar -> {
      System.out.println(ar.result().getString("error"));
    });


    redissonBackend.store(record, res -> {
      if(!res.succeeded()) {
        res.cause().printStackTrace();
      }
    });

    await().until(() -> action.get() != null);
    assertEquals("store", action.get());
  }


  @Test
  public void testServiceRemove() throws Exception {
    // create the microservice record
    Record record = HttpEndpoint.createRecord(
      "amazing",
      "127.0.0.1",
      9092,
      "/api"
    );

    AtomicReference<String> action = new AtomicReference<>();
    AtomicReference<String> reference = new AtomicReference<>();

    redissonBackend.onRemovePubEvent(ar -> {
      if(ar.succeeded()) {
        System.out.println(
          ar.result().getJsonObject("record")
        );
        action.set(ar.result().getString("action"));
      }
    }).onErrorPubEvent(ar -> {
      System.out.println(ar.result().getString("error"));
    });


    redissonBackend.store(record, res -> {
      if(!res.succeeded()) {
        res.cause().printStackTrace();
      }
      reference.set(record.getRegistration());
    });

    await().until(() -> reference.get() != null);

    redissonBackend.remove(record.getRegistration(), res -> {
      if(!res.succeeded()) {
        res.cause().printStackTrace();
      }
      reference.set(record.getRegistration());
    });

    await().until(() -> reference.get() != null);

    await().until(() -> action.get() != null);
    assertEquals("remove", action.get());
  }

  @Test
  public void testServiceUpdate() throws Exception {
    // create the microservice record
    Record record = HttpEndpoint.createRecord(
      "fantastic",
      "127.0.0.1",
      9092,
      "/api"
    );

    AtomicReference<String> action = new AtomicReference<>();
    AtomicReference<String> reference = new AtomicReference<>();

    redissonBackend.onUpdatePubEvent(ar -> {
      if(ar.succeeded()) {
        System.out.println(
          ar.result().getJsonObject("record")
        );
        action.set(ar.result().getString("action"));
      }
    }).onErrorPubEvent(ar -> {
      System.out.println(ar.result().getString("error"));
    });


    redissonBackend.store(record, res -> {
      if(!res.succeeded()) {
        res.cause().printStackTrace();
      }
      reference.set(record.getRegistration());
    });

    await().until(() -> reference.get() != null);

    record.setMetadata(new JsonObject().put("message", "👋 Hello 🌍"));

    redissonBackend.update(record, res -> {
      if(!res.succeeded()) {
        res.cause().printStackTrace();
      }
      reference.set(record.getRegistration());
    });

    await().until(() -> reference.get() != null);

    await().until(() -> action.get() != null);
    assertEquals("update", action.get());
  }

  @Test
  public void testServiceAllRecords() throws Exception {
    Record record1 = HttpEndpoint.createRecord(
      "awesome",
      "127.0.0.1",
      9091,
      "/api"
    );
    Record record2 = HttpEndpoint.createRecord(
      "amazing",
      "127.0.0.1",
      9092,
      "/api"
    );
    Record record3 = HttpEndpoint.createRecord(
      "fantastic",
      "127.0.0.1",
      9093,
      "/api"
    );

    AtomicReference<String> ref1 = new AtomicReference<>();
    AtomicReference<String> ref2 = new AtomicReference<>();
    AtomicReference<String> ref3 = new AtomicReference<>();

    redissonBackend.store(record1, res -> {
      if(!res.succeeded()) {
        res.cause().printStackTrace();
      }
      ref1.set(record1.getRegistration());
    });

    redissonBackend.store(record2, res -> {
      if(!res.succeeded()) {
        res.cause().printStackTrace();
      }
      ref2.set(record2.getRegistration());
    });

    redissonBackend.store(record3, res -> {
      if(!res.succeeded()) {
        res.cause().printStackTrace();
      }
      ref3.set(record3.getRegistration());
    });

    await().until(() -> ref1.get() != null);
    await().until(() -> ref2.get() != null);
    await().until(() -> ref3.get() != null);

    AtomicReference<Integer> count = new AtomicReference<>();

    redissonBackend.getRecords(res -> {

      res.result().forEach(service -> {
        System.out.println(service.getName() + ": " + service.getRegistration());
      });

      count.set(res.result().size());
    });

    await().until(() -> count.get() != null);
    assertNotNull(count);
  }
}
