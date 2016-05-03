package com.elasticbox.jenkins.k8s.plugin.util;

import java.util.Map;

public class KeyValuePair<K, V> implements Map.Entry<K, V> {
    private K key;
    private V value;

    public KeyValuePair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return this.key;
    }

    public V getValue() {
        return this.value;
    }

    public V setValue(V value) {
        return this.value = value;
    }
}

