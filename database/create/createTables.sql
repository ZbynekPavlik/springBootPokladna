CREATE TABLE IF NOT EXISTS public.users (
    id_user SERIAL PRIMARY KEY,
    username VARCHAR(50) COLLATE pg_catalog."default",
    password VARCHAR(50) COLLATE pg_catalog."default" NOT NULL,
    enabled SMALLINT NOT NULL,
    role VARCHAR(50) COLLATE pg_catalog."default" DEFAULT 'EMPLOYEE'::VARCHAR,
    CONSTRAINT users_username_key UNIQUE (username)
);

CREATE TABLE IF NOT EXISTS public.authorities (
    id_authority SERIAL PRIMARY KEY,
    username VARCHAR(50) COLLATE pg_catalog."default",
    authority VARCHAR(50) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT authorities_username_authority_key UNIQUE (username, authority),
    CONSTRAINT authorities_username_fkey FOREIGN KEY (username)
        REFERENCES public.users (username) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS public.sales (
    id_sale SERIAL PRIMARY KEY,
    amount INTEGER,
    sold_goods VARCHAR(255) COLLATE pg_catalog."default",
    user_id INTEGER,
    CONSTRAINT sales_user_id_fkey FOREIGN KEY (user_id)
        REFERENCES public.users (id_user) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS public.financialtransaction (
    id_transaction SERIAL PRIMARY KEY,
    description VARCHAR(255) COLLATE pg_catalog."default",
    amount INTEGER,
    balance_before INTEGER,
    balance_after INTEGER,
    sale_id INTEGER,
    user_id INTEGER,
    deleted BOOLEAN DEFAULT false,
    CONSTRAINT financialtransaction_sale_id_fkey FOREIGN KEY (sale_id)
        REFERENCES public.sales (id_sale) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT financialtransaction_user_id_fkey FOREIGN KEY (user_id)
        REFERENCES public.users (id_user) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);
