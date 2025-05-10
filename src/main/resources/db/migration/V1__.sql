CREATE TABLE event
    (
        id               BIGINT AUTO_INCREMENT NOT NULL,
        created_at       datetime     NOT NULL,
        updated_at       datetime     NOT NULL,
        name             VARCHAR(100) NOT NULL,
        content          TEXT         NOT NULL,
        venue            VARCHAR(100) NOT NULL,
        event_date       datetime     NOT NULL,
        sales_start_date datetime     NOT NULL,
        sales_end_date   datetime     NOT NULL,
        total_seats      INT          NOT NULL,
        status           VARCHAR(20)  NOT NULL,
        is_queue_active  BIT(1)       NOT NULL,
        CONSTRAINT pk_event PRIMARY KEY (id)
    );

CREATE TABLE member
    (
        id           BIGINT AUTO_INCREMENT NOT NULL,
        created_at   datetime     NOT NULL,
        updated_at   datetime     NOT NULL,
        email        VARCHAR(100) NOT NULL,
        password     VARCHAR(100) NOT NULL,
        name         VARCHAR(50)  NOT NULL,
        phone        VARCHAR(20)  NULL,
        device_token VARCHAR(255) NULL,
        member_role  VARCHAR(20)  NOT NULL,
        CONSTRAINT pk_member PRIMARY KEY (id)
    );

CREATE TABLE notification
    (
        id            BIGINT AUTO_INCREMENT NOT NULL,
        created_at    datetime     NOT NULL,
        updated_at    datetime     NOT NULL,
        member_id     BIGINT       NOT NULL,
        type          VARCHAR(50)  NOT NULL,
        title         VARCHAR(200) NOT NULL,
        content       TEXT         NOT NULL,
        link          VARCHAR(500) NULL,
        status        VARCHAR(20)  NOT NULL,
        sent_at       datetime     NULL,
        read_at       datetime     NULL,
        error_message VARCHAR(500) NULL,
        CONSTRAINT pk_notification PRIMARY KEY (id)
    );

CREATE TABLE payment
    (
        id             BIGINT AUTO_INCREMENT NOT NULL,
        created_at     datetime       NOT NULL,
        updated_at     datetime       NOT NULL,
        member_id      BIGINT         NOT NULL,
        reservation_id BIGINT         NOT NULL,
        amount         DECIMAL(10, 2) NOT NULL,
        status         VARCHAR(20)    NOT NULL,
        payment_method VARCHAR(50)    NOT NULL,
        transaction_id VARCHAR(100)   NULL,
        paid_at        datetime       NULL,
        refunded_at    datetime       NULL,
        refund_reason  VARCHAR(500)   NULL,
        CONSTRAINT pk_payment PRIMARY KEY (id)
    );

CREATE TABLE queue_entry
    (
        id             BIGINT AUTO_INCREMENT NOT NULL,
        created_at     datetime    NOT NULL,
        updated_at     datetime    NOT NULL,
        event_id       BIGINT      NOT NULL,
        member_id      BIGINT      NOT NULL,
        queue_position INT         NOT NULL,
        status         VARCHAR(20) NOT NULL,
        notified_at    datetime    NULL,
        entered_at     datetime    NULL,
        expires_at     datetime    NULL,
        CONSTRAINT pk_queue_entry PRIMARY KEY (id)
    );

CREATE TABLE reservation
    (
        id            BIGINT AUTO_INCREMENT NOT NULL,
        created_at    datetime       NOT NULL,
        updated_at    datetime       NOT NULL,
        member_id     BIGINT         NOT NULL,
        event_id      BIGINT         NOT NULL,
        ticket_id     BIGINT         NOT NULL,
        total_amount  DECIMAL(10, 2) NOT NULL,
        status        VARCHAR(20)    NOT NULL,
        payment_id    VARCHAR(255)   NULL,
        confirmed_at  datetime       NULL,
        cancelled_at  datetime       NULL,
        cancel_reason VARCHAR(500)   NULL,
        CONSTRAINT pk_reservation PRIMARY KEY (id)
    );

CREATE TABLE ticket
    (
        id                    BIGINT AUTO_INCREMENT NOT NULL,
        created_at            datetime       NOT NULL,
        updated_at            datetime       NOT NULL,
        event_id              BIGINT         NOT NULL,
        ticket_type_id        BIGINT         NOT NULL,
        seat_number           VARCHAR(255)   NOT NULL,
        price                 DECIMAL(10, 2) NOT NULL,
        status                VARCHAR(20)    NOT NULL,
        reserved_by_member_id BIGINT         NULL,
        reserved_at           datetime       NULL,
        CONSTRAINT pk_ticket PRIMARY KEY (id)
    );

CREATE TABLE ticket_type
    (
        id                 BIGINT AUTO_INCREMENT NOT NULL,
        created_at         datetime       NOT NULL,
        updated_at         datetime       NOT NULL,
        event_id           BIGINT         NOT NULL,
        name               VARCHAR(50)    NOT NULL,
        price              DECIMAL(10, 2) NOT NULL,
        quantity           INT            NOT NULL,
        available_quantity INT            NOT NULL,
        content            VARCHAR(255)   NULL,
        CONSTRAINT pk_ticket_type PRIMARY KEY (id)
    );

ALTER TABLE member
    ADD CONSTRAINT uc_member_email UNIQUE (email);

ALTER TABLE reservation
    ADD CONSTRAINT uc_reservation_ticket UNIQUE (ticket_id);