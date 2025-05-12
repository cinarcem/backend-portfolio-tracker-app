FROM quay.io/keycloak/keycloak:25.0.4 AS builder

ARG KEYCLOAK_ADMIN
ARG KEYCLOAK_ADMIN_PASSWORD
ARG KC_DB
ARG KC_DB_USERNAME
ARG KC_DB_PASSWORD
ARG KC_DB_URLgit -
ARG KC_HOSTNAME
ARG KC_PROXY
ARG KC_HOSTNAME_PORT
ARG KC_HOSTNAME_STRICT_BACKCHANNEL
ARG KC_HOSTNAME_STRICT_HTTPS
ARG KC_HTTP_ENABLED
ARG PROXY_ADDRESS_FORWARDING
ARG QUARKUS_TRANSACTION_MANAGER_ENABLE_RECOVERY

RUN /opt/keycloak/bin/kc.sh build

FROM quay.io/keycloak/keycloak:25.0.4

COPY --from=builder /opt/keycloak/ /opt/keycloak/

COPY themes /opt/keycloak/themes

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]

CMD ["start", "--optimized", "--import-realm"]