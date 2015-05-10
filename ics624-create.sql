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
  is_current                number(1),
  effective_date            timestamp,
  end_date                  timestamp,
  constraint pk_dim_customer primary key (s_k_customer_id))
;

create sequence dim_customer_seq;



