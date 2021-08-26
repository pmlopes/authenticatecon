package org.acme;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import io.vertx.core.Vertx;
import io.vertx.core.http.CookieSameSite;
import io.vertx.ext.auth.webauthn.RelyingParty;
import io.vertx.ext.auth.webauthn.WebAuthn;
import io.vertx.ext.auth.webauthn.WebAuthnOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.*;
import io.vertx.ext.web.sstore.LocalSessionStore;

import javax.inject.Inject;

@QuarkusMain
public class Main implements QuarkusApplication {

    public static void main(String... args) {
        Quarkus.run(Main.class, args);
    }

    @Inject
    Vertx vertx;

    @Inject
    Router app;

    @Override
    public int run(String... args) {

        // Dummy database, real world workloads
        // use a persistent store or course!
        final InMemoryStore database = new InMemoryStore();

        // create the webauthn security object
        WebAuthn webAuthN = WebAuthn.create(
                        vertx,
                        new WebAuthnOptions()
                                .setRelyingParty(new RelyingParty().setName("Vert.x Demo Server")))
                // where to load/update authenticators data
                .authenticatorFetcher(database::fetcher)
                .authenticatorUpdater(database::updater);

        // parse the BODY
        app.post()
                .handler(BodyHandler.create());

        // add a session handler
        app.route()
                .handler(SessionHandler
                        .create(LocalSessionStore.create(vertx))
                        .setCookieSameSite(CookieSameSite.STRICT));

        // security handler
        WebAuthnHandler webAuthnHandler = WebAuthnHandler.create(webAuthN)
                .setOrigin("https://192.168.178.210.nip.io:8443")
                // required callback
                .setupCallback(app.post("/webauthn/callback"))
                // optional register callback
                .setupCredentialsCreateCallback(app.post("/webauthn/register"))
                // optional login callback
                .setupCredentialsGetCallback(app.post("/webauthn/login"));

        // secure the remaining routes
        app.route("/protected/*").handler(webAuthnHandler);

        // serve the SPA
        app.route()
                .handler(StaticHandler.create("webroot"));

        Quarkus.waitForExit();
        return 0;
    }
}
