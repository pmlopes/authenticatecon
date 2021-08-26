/// <reference types="@vertx/core" />
// @ts-check
vertx
  .createHttpServer()
  .requestHandler(function (req) {
    req.response().end("Hello!");
  })
  .listen(8080);

console.log('Vert.x started on port 8080');

print(process.properties['polyglot.js.intl-402'])

print(Intl.NumberFormat)
