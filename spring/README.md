# WebAuthN Vert.x Demo

This is a small PoC for WebAuthN vert.x application.

All users are ephemeral as the data is stored in memory in the class `InMemoryStore`.

# Development

To test on a local machine there is no need to use SSL, however to connect to the app from a different machine/device
the browser credentials API requires an SSL certificate.

To create a self-signed key for your IP address do the following:

```bash
# replace the CN with your own IP address (other than localhost) with suffix .nip.io
keytool -genkeypair -alias rsakey -keyalg rsa -storepass secret -keystore cert-store.jks -storetype JKS -dname "CN=192.168.178.210.nip.io,O=Vert.x Development"
# convert to PKCS#12 format for compatibility reasons
keytool -importkeystore -srckeystore cert-store.jks -destkeystore cert-store.jks -deststoretype pkcs12
# your new ssl certificate is on the file `cert-store.jks`
```

# Run

```shell
java -jar target/spring-vertx-0.0.1-SNAPSHOT.jar
```

Open your browser to https://192.168.172.210.nip.io:8443

NOTE: replace the given IP address with your own.