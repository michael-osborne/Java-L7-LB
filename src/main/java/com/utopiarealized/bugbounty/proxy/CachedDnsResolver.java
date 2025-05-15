package com.utopiarealized.bugbounty.proxy;

import org.apache.http.conn.DnsResolver;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class CachedDnsResolver implements DnsResolver {

    private static final DnsResolver delegate = new SystemDefaultDnsResolver();

    private final Map<String, InetAddress> cache = new HashMap<>();

    public InetAddress resolveOne(String host) throws UnknownHostException {
        InetAddress address = cache.get(host.toLowerCase());
        if (address == null) {
            address = resolveUncached(host);
            if (address != null) {
                cache.put(host.toLowerCase(), address);
            }
        }
        return address;
    }

    public InetAddress[] resolve(String host) throws UnknownHostException {
        InetAddress address = cache.get(host.toLowerCase());
        if (address == null) {
            address = resolveUncached(host);
            if (address != null) {
                cache.put(host.toLowerCase(), address);
            }
        }
        return new InetAddress[]{address};
    }

    public void addAll(final Map<String, String> dnsEntries) {
        try {
            for (Map.Entry<String, String> entry : dnsEntries.entrySet()) {
                cache.put(entry.getKey().toLowerCase(), InetAddress.getByName(entry.getValue()));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static InetAddress resolveUncached(String host) throws UnknownHostException {
        InetAddress[] address = delegate.resolve(host);
        if (address != null) {
            return address[0];
        }
        return null;
    }

}
