package org.example;

import redis.clients.jedis.Jedis;

public class Main {
  public static void main(String[] args) {
    System.out.println("Hello world!");
    var jedis = new Jedis();
    var dao = new AuctionDao(jedis);
    dao.addAntique("1", "Temp", 500);
    dao.addBidder("1", "Ali", 1000);
    dao.startAuctionForAntique("1");
    dao.offerBid("1", "1", 600);
    System.out.println(dao.getSold("1"));
  }
}