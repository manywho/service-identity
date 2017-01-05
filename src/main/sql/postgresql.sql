CREATE TABLE "Group"
(
  id uuid NOT NULL,
  name character varying NOT NULL,
  description character varying,
  created_at timestamp with time zone DEFAULT now(),
  updated_at timestamp with time zone,
  CONSTRAINT pk_group PRIMARY KEY (id),
  CONSTRAINT uq_group_name UNIQUE (name)
);

CREATE TABLE "Membership"
(
  user_id uuid NOT NULL,
  group_id uuid NOT NULL,
  CONSTRAINT pk_membership PRIMARY KEY (user_id, group_id),
  CONSTRAINT fk_membership_group_id FOREIGN KEY (group_id)
      REFERENCES public."Group" (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT fk_membership_user_id FOREIGN KEY (user_id)
      REFERENCES public."User" (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE
);

CREATE TABLE "User"
(
  id uuid NOT NULL,
  first_name character varying NOT NULL,
  last_name character varying NOT NULL,
  email character varying NOT NULL,
  password character varying(65),
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL,
  CONSTRAINT pk_user PRIMARY KEY (id),
  CONSTRAINT uq_user_email UNIQUE (email)
);