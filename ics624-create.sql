create table dim_account (
  s_k_account_id            number(19) not null,
  account_id                varchar2(255),
  broker_id                 varchar2(255),
  s_k_customer_id           number(19),
  status                    varchar2(255),
  account_desc              varchar2(255),
  tax_status                varchar2(255),
  is_current                number(1),
  effective_date            timestamp,
  end_date                  timestamp,
  constraint pk_dim_account primary key (s_k_account_id))
;

create table dim_customer (
  s_k_customer_id           number(19) not null,
  customer_id               number(19),
  tax_id                    varchar2(255),
  last_name                 varchar2(255),
  first_name                varchar2(255),
  middle_initial            varchar2(255),
  gender                    varchar2(255),
  tier                      number(10),
  dob                       timestamp,
  address_line1             varchar2(255),
  address_line2             varchar2(255),
  postal_code               varchar2(255),
  city                      varchar2(255),
  state_prov                varchar2(255),
  country                   varchar2(255),
  phone1                    varchar2(255),
  phone2                    varchar2(255),
  phone3                    varchar2(255),
  e_mail1                   varchar2(255),
  e_mail2                   varchar2(255),
  status                    varchar2(255),
  is_current                number(1),
  effective_date            timestamp,
  end_date                  timestamp,
  constraint pk_dim_customer primary key (s_k_customer_id))
;

create sequence dim_account_seq;

create sequence dim_customer_seq;



