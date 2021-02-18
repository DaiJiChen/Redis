import redis.clients.jedis.Jedis;

public class TestMS {
    public static void main(String[] args) {

        Jedis jedis_master = new Jedis("18.188.81.36", 6379);
        Jedis jedis_slave = new Jedis("18.188.81.36", 6380);

        jedis_slave.slaveof("18.188.81.36", 6379);

        jedis_master.set("Hello", "World");

        String result = jedis_slave.get("Hello");
        System.out.println(result);
    }
}

