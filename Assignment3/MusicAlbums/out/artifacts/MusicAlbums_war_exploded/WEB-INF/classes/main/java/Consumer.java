import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rabbitmq.client.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Not in use anymore. Use ReviewConsumer.java instead.
 */
@Deprecated
public class Consumer {
    private final static String QUEUE_NAME = "ReviewServletPostQueue";
    private final static Integer NUM_THREADS = 512; //512;
    private static Jedis jedis = null; // default port for Redis

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
//        jedis = new Jedis("localhost", 6379);

        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println(" [x] Received '" + message + "'");
                // Parse the message
                JsonObject reviewMsg = JsonParser.parseString(message).getAsJsonObject();
                System.out.println(" [x] reviewMsg: " + reviewMsg);
                String albumId = reviewMsg.get("albumId").getAsString();
                String isLike = reviewMsg.get("isLike").getAsString();
                System.out.println(" [x] albumId: " + albumId + ", isLike: " + isLike);

                // Process the message: add like/dislike count for the album
                switch (isLike) {
                    case "like":
                        System.out.println(" [x] Like count added for album " + albumId);
                        jedis.incr("like:" + albumId);
                        break;
                    case "dislike":
                        System.out.println(" [x] Dislike count added for album " + albumId);
                        jedis.incr("dislike:" + albumId);
                        break;
                    default:
                        System.out.println(" [x] Invalid isLike value: " + isLike);
                        break;
                }
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            };
            channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
        }
//        jedis.close();
    }
}
