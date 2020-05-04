#!/bin/sh

# Convert PEM format certificate from Let's Encrypt to P12 format certificate for JETTY
# Run with SUDO

set -e
set -x

(
  cd /etc/letsencrypt/live/appserver-test.c19x.org
  openssl pkcs12 -export -in fullchain.pem -inkey privkey.pem -out /opt/c19x/keystore.p12 -name c19x -CAfile chain.pem -caname root -password file:/opt/c19x/keystore.pw
  chmod a+r /opt/c19x/keystore.p12
)


