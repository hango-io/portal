package org.hango.cloud.dashboard.apiserver.service;

public interface IRedisService {


    <K, V> V getValue(K key);

    <K, V> void setValue(K key, V value, long expire);

    <K, V> Boolean getLock(K key, V value, long expire);

    <K> void deleteKey(K key);

    <K> boolean hasKey(K key);
}