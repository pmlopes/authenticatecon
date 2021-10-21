# Instant secure your JVM application with Vert.x and FIDO2

Here you will find the conpanion demos for the [Authenticate 2021 Conference](https://authenticatecon.com/session/instant-passwordless-modern-jvm-applications-with-vert-x-and-fido2/).

You can find the [slides](https://www.jetdrone.xyz/presentations/authenticate-2021) for the talk online.

In this repo you can find the same application written using different frameworks, however the security is handled by
[Eclipse Vert.x](https://vertx.io).

## Vert.x

In the [vert.x](./vert.x) directory you will find a simple Vert.x application that is protected using FIDO2.

## Quarkus

In the [quarkus](./quarkus) directory you will find the same application but written in Quarkus. As Quarkus uses Vert.x
internally you will find out that you will need to write less code.

## Spring

Like in the previous examples, you can find a [spring](./spring) application protected by vert.x too.

## JavaScript

With the help of [GraalVM](https://graalvm.org) we can run polyglot applications on the JVM too and
[ES4x](https//reactiverse.io/es4x) shows how we can use vert.x from a polyglot perspective and secure
polyglot applications with FIDO2.

## Gateway

In this final example, you can see how to protect applications which you have no access to the source code or are unable
to modify, by writting a simple reverse proxy in Vert.x and embed FIDO2 security.
