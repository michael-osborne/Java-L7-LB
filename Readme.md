This is a small Java project to modify http requests from a browser websites.

This was created to help with hacking initatives in hackerOne, effectively allowing man-in-the-middle TLS attacks between
the browser and the website

It works in the following manner:
- User runs "BuildBountyContext" which prmpts for a list of domains.
- Program then
  - Looks up domains to create a hosts DB
  - Builds TLS keypairs (need to be trusted by the system)
  - Builds a java keystore with each domain as an alias.

User then copies the host file created to /etc/hosts and starts the Proxy.

Proxy supports simple pattern matching to grab headers/cookies/parameters from responses and add these to requests



Assuming the host "google.com" the process looks as follows:

- User starts proxy(default on port 443)
- User opens browser to https://google.com
- Host entry for google.com is 127.0.0.1, so request goes to proxy
- Proxy returns locally created & manually trusted TLS cert for google.com
- *Proxy modifies incoming request with whatever rules are currently in place.
- Proxy forwards request on to google.com
- Proxy reads response, (*optionally captures data),and forwards to browser


While running, the proxy supports capture/append data with the following simple commands:
capture(Request #, method,url, header:value, formfield:value, cookie:value);cookie/header/form/const(name, variable)
And
send(Request #, method,url, header:value, formfield:value, cookie:value);cookie/header/form/const(name, variable)

The format is the same; the first part is a selector noting what request this pertains to (any fields left blank are wildcards)
and the latter part is what to either capture (and store as a variable) or what to add/append.


