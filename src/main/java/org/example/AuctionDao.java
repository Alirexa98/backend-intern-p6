package org.example;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.TreeMap;

public class AuctionDao {
  private Jedis jedis;

  public AuctionDao(Jedis jedis) {
    this.jedis = jedis;
  }

  public void addAntique(String antiqueId, String name, int initialPrice) {
    var object = new TreeMap<String, String>();
    object.put("name", name);
    object.put("initialPrice", String.valueOf(initialPrice));
    jedis.hmset("antique:"+antiqueId, object);
  }

  public void addBidder(String bidderId, String name, int budget) {
    var object = new TreeMap<String, String>();
    object.put("name", name);
    object.put("budget", String.valueOf(budget));
    jedis.hmset("bidder:"+bidderId, object);
  }

  public void startAuctionForAntique(String antiqueId) {
    String key = "auction:" + antiqueId;
    if (jedis.exists(key)) {
      return;
    }

    var object = new TreeMap<String, String>();
    object.put("lastBid", jedis.hget("antique:"+antiqueId, "initialPrice"));
    jedis.hmset(key, object);
  }

  public void offerBid(String antiqueId, String bidderId, int price) {
    String key = "auction:" + antiqueId;
    if (!jedis.exists(key)) {

      return;
    }

    var object = jedis.hgetAll(key);
    if (Integer.parseInt(object.get("lastBid")) > price) {
      return;
    }

    var lastBidder = object.get("lastBidder");
    if (lastBidder != null) {
      jedis.hincrBy("bidder:"+lastBidder, "budget", price);
    }
    if (Integer.parseInt(jedis.hget("bidder:"+bidderId, "budget")) < price) {
      return;
    }
    jedis.hincrBy("bidder:"+bidderId, "budget", -price);

    object.put("lastBid", String.valueOf(price));
    object.put("lastBidder", bidderId);
    jedis.hmset(key, object);
    jedis.set("sold:"+antiqueId, bidderId);
    jedis.expire(key, 10);
  }

  public String getSold(String antiqueId) {
    return jedis.get("sold:" + antiqueId);
  }
}
