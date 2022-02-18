-- auto-generated definition
create table usr
(
    pkey_id              bigint        default not null
        constraint "tbl.usr_pk"
            primary key,
    role                 tbl.enum_role default 'guest'::tbl.enum_role              not null,
    public_id            bigint                                                    not null
        constraint ak_user_public_id
            unique,
    username             varchar(20)                                               not null
        constraint ak_username
            unique,
    password_hash        bytea                                                     not null,
    password_salt        bytea                                                     not null,
    age                  smallint,
    preferred_language   varchar(5)                                                not null,
    family_name          varchar(128),
    given_name           varchar(128),
    agreed_tos           boolean                                                   not null,
    agreed_privacy       boolean                                                   not null,
    created_at           oid           default not null,
    updated_at           oid           default not null,
    email                varchar(320),
    phone_number         varchar(15),
    last_ip              inet                                                      not null,
    last_login           oid,
    last_password_reset  oid,
    logins_count         integer       default 0                                   not null,
    user_device_id       varchar(256),
    admin_device_id      varchar(256),
    user_token           uuid,
    admin_token          uuid,
    password_reset_token uuid,
    reset_token_valid    oid,
    is_blocked           boolean       default false                               not null,
    max_slp_earned       varchar
);
