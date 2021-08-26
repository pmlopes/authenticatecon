/// <reference types="@vertx/core" />
// @ts-check

import {WebAuthn} from "@vertx/auth-webauthn";
import {RelyingParty, WebAuthnOptions} from "@vertx/auth-webauthn/options";
import {
    BodyHandler, ErrorHandler,
    FaviconHandler,
    LocalSessionStore,
    Router,
    SessionHandler,
    StaticHandler,
    WebAuthnHandler
} from "@vertx/web";
import {CookieSameSite} from "@vertx/core/enums";
import {HttpServerOptions, JksOptions} from "@vertx/core/options";
import {fetcher, updater} from "./store.mjs";

// create the webauthn security object
const webAuthN = WebAuthn.create(
    vertx,
    new WebAuthnOptions()
        .setRelyingParty(new RelyingParty().setName("Vert.x Demo Server")))
    // where to load/update authenticators data
    .authenticatorFetcher(fetcher)
    .authenticatorUpdater(updater);

const app = Router.router(vertx);

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

app.route()
    .failureHandler(ErrorHandler.create(vertx, true));

// security handler
const webAuthnHandler = WebAuthnHandler.create(webAuthN)
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
    .handler(StaticHandler.create());

try {
    let server = vertx.createHttpServer(
        new HttpServerOptions()
            .setSsl(true)
            .setKeyStoreOptions(
                new JksOptions()
                    .setPath("cert-store.jks")
                    .setPassword("secret")))
        .requestHandler(app)
        .listen(8443, "0.0.0.0");

    console.log("Server listening at: https://192.168.178.210.nip.io:8443");
} catch (e) {
    console.trace(e);
}