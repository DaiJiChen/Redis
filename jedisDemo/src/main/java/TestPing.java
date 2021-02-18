import redis.clients.jedis.Jedis;

public class TestPing {
    public static void main(String[] args) {
        Jedis jedis = new Jedis("18.188.81.36", 6379);
        System.out.println(jedis.ping());
    }
}
