package com.example.webauthn;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.CookieSameSite;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.auth.webauthn.RelyingParty;
import io.vertx.ext.auth.webauthn.WebAuthn;
import io.vertx.ext.auth.webauthn.WebAuthnOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.*;
import io.vertx.ext.web.sstore.LocalSessionStore;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> start) {

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

        final Router app = Router.router(vertx);
        // parse the BODY
        app.post()
                .handler(BodyHandler.create());
        // favicon
        app.route()
                .handler(FaviconHandler.create(vertx));
        // add a session handler
        app.route()
                .handler(SessionHandler
                        .create(LocalSessionStore.create(vertx))
                        .setCookieSameSite(CookieSameSite.STRICT));

        // security handler
        WebAuthnHandler webAuthnHandler = WebAuthnHandler.create(webAuthN)
                .setOrigin(String.format("https://%s.nip.io:8443", System.getenv("IP")))
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
                .handler(StaticHandler.create());

        vertx.createHttpServer(
                        new HttpServerOptions()
                                .setSsl(true)
                                .setKeyStoreOptions(
                                        new JksOptions()
                                                .setPath("cert-store.jks")
                                                .setPassword(System.getenv("CERTSTORE_SECRET"))))

                .requestHandler(app)
                .listen(8443, "0.0.0.0")
                .onFailure(start::fail)
                .onSuccess(v -> {
                    System.out.printf("Server listening at: https://%s.nip.io:8443%n", System.getenv("IP"));
                    start.complete();
                });
    }
}
