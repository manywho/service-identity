CREATE TABLE groups
(
  id uuid NOT NULL,
  name character varying NOT NULL,
  tenant_id uuid NOT NULL,
  description character varying,
  created_at timestamp with time zone DEFAULT now(),
  updated_at timestamp with time zone,
  CONSTRAINT pk_groups PRIMARY KEY (id),
  CONSTRAINT uniq_groups_name UNIQUE (name, tenant_id)
);

CREATE TABLE memberships
(
  user_id uuid NOT NULL,
  group_id uuid NOT NULL,
  CONSTRAINT pk_memberships PRIMARY KEY (user_id, group_id)
);

CREATE TABLE users
(
  id uuid NOT NULL,
  first_name character varying NOT NULL,
  last_name character varying NOT NULL,
  email character varying NOT NULL,
  password character varying(65),
  tenant_id uuid,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL,
  CONSTRAINT pk_users PRIMARY KEY (id),
  CONSTRAINT uniq_users_email UNIQUE (email, tenant_id)
);