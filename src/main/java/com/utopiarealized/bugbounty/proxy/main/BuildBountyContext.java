package com.utopiarealized.bugbounty.proxy.main;

import com.utopiarealized.bugbounty.proxy.setup.SetupBounty;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class BuildBountyContext {


    public static void main(String[] args) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Enter the bounty name: ");
        String bountyName = reader.readLine(); // Read a line of text from the user
        System.out.println("Bounty name is: " + bountyName);

        SetupBounty setupBounty = new SetupBounty( bountyName);

        System.out.println("Enter comma delimited list of hosts: ");
        String hosts = reader.readLine();
/*
# $1 is the target directory for the certificate
# $2 is the name of the bounty
# $3 is the host
 */
        setupBounty.writeFiles(hosts);

        System.out.println("Done!");
        System.out.println("sudo cp " + setupBounty.getBaseDir() + "/hosts /etc/hosts");
        System.out.println("sudo " + setupBounty.getBaseDir() + "/setup.sh");
    }
}
