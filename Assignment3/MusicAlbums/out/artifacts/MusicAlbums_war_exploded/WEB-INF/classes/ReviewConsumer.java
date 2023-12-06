import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rabbitmq.client.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReviewConsumer {
    private final static String QUEUE_NAME = "ReviewServletPostQueue";
    private final static Integer NUM_THREADS = 512;

    public static void main(String[] args) throws Exception {
//        Gson gson = new Gson();
        ConnectionFactory factory = new ConnectionFactory();
        JedisPool pool;

        // Configure RabbitMQ and Redis connection
        // Replace with your own configuration
        factory.setHost("52.13.25.243");
//        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");

        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(10);
        pool = new JedisPool(poolConfig, "54.188.78.177", 6379);
//        pool = new JedisPool(poolConfig, "localhost", 6379);

        Connection connection = factory.newConnection();

        // Test Jedis connection
        System.out.println(pool.getResource().ping());

        Runnable runnable = () -> {
            try (Jedis jedis = pool.getResource()) {
                final Channel channel = connection.createChannel();
                channel.queueDeclare(QUEUE_NAME, false, false, false, null);
                System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
                channel.basicQos(1);

                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    String message = new String(delivery.getBody(), "UTF-8");
                    System.out.println(" [x] Received '" + message + "'");
                    JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();
                    String albumId = jsonObject.get("albumId").getAsString();
                    String isLike = jsonObject.get("isLike").getAsString();

                    String key = isLike + ":" + albumId;
                    System.out.println("DEBUG:: Key is " + key);
                    jedis.incr(key);

                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                };
                channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> {});
            } catch (IOException e) {
                Logger.getLogger(ReviewConsumer.class.getName()).log(Level.SEVERE, null, e);
            }
        };

        // Start threads
        ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS);
        for (int i = 0; i < NUM_THREADS; i++) {
            executorService.execute(runnable);
        }
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
        pool.close();
    }
}
