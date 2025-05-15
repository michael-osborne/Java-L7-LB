package com.utopiarealized.bugbounty.proxy.main;

import com.utopiarealized.bugbounty.proxy.*;
import com.utopiarealized.bugbounty.proxy.model.SpyContext;
import com.utopiarealized.bugbounty.proxy.modify.HttpModifier;
import com.utopiarealized.bugbounty.proxy.rules.ParseRule;
import com.utopiarealized.bugbounty.proxy.rules.RuleHandler;
import com.utopiarealized.bugbounty.proxy.setup.SetupBounty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;

public class StartProxy {

    private static HttpProxy httpProxy;

    private static HttpsProxy httpsProxy;


    private static HttpModifier httpRequestModifier = new HttpModifier();

    private volatile boolean running = true;

    private static HttpSpy httpSpy;

    private static RuleHandler ruleHandler = new RuleHandler();

    public static void main(String[] args) throws IOException, InterruptedException, GeneralSecurityException {


        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Enter the bounty name:");
        String bountyName = reader.readLine(); // Read a line of text from the user

        System.out.println("Bounty name is: " + bountyName);

        final SpyContext spyContext = new SpyContext(bountyName, SetupBounty.DIRECTORY);

        final CachedDnsResolver cachedDnsResolver = new CachedDnsResolver();
        SetupBounty setupBounty = new SetupBounty(bountyName);
        cachedDnsResolver.addAll(setupBounty.getDNSEntries());

        System.out.println("Loading bounty context for " + bountyName);


        httpSpy = new HttpSpy(httpRequestModifier, spyContext, ruleHandler);
        final HttpForwarder httpForwarder = new HttpForwarder(cachedDnsResolver, httpSpy);

        httpsProxy = new HttpsProxy(443,
                SetupBounty.getKeystoreLocation(bountyName),
                "mypass",
                httpForwarder,
                httpSpy
        );

        while (true) {

            System.out.println("Running. Enter a rule in the following format. Blank line to end");
            System.out.println("capture/send(#, method,url, header:value, formfield:value, cookie:value);cookie/header/form/const(name, store)");
            final String rule = reader.readLine();
            if (rule.length() == 0) {
                break;
            }
            try {
                ruleHandler.addRule(rule);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("Goodbye.");
    }


}
