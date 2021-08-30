package org.acme;

import javax.annotation.PostConstruct;

import io.vertx.core.http.CookieSameSite;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.auth.webauthn.RelyingParty;
import io.vertx.ext.auth.webauthn.WebAuthn;
import io.vertx.ext.auth.webauthn.WebAuthnOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.WebAuthnHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.vertx.core.Vertx;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @PostConstruct
    public void deployVerticle() {
        final Vertx vertx = Vertx.vertx();
        final Router app = Router.router(vertx);

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

        vertx.createHttpServer(
                        new HttpServerOptions()
                                .setSsl(true)
                                .setKeyStoreOptions(
                                        new JksOptions()
                                                .setPath("cert-store.jks")
                                                .setPassword("secret")))

                .requestHandler(app)
                .listen(8443, "0.0.0.0")
                .onFailure(Throwable::printStackTrace)
                .onSuccess(v -> System.out.println("Server listening at: https://192.168.178.210.nip.io:8443"));
    }
}