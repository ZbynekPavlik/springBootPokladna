INSERT INTO public.users (id_user, enabled, password, role, username)
VALUES
(-2, 1, '{noop}b', 'ADMIN', 'Admin'),
(-1, 1, '{noop}a', 'EMPLOYEE', 'Pokladni');

INSERT INTO public.authorities (id_authority, authority, username) VALUES
(-3, 'ROLE_EMPLOYEE', 'Admin'),
(-2, 'ROLE_ADMIN', 'Admin'),
(-1, 'ROLE_EMPLOYEE', 'Pokladni');

INSERT INTO public.sales (id_sale, amount, sold_goods, user_id) VALUES
(-4, 4000, 'mobil', -2),
(-3, 10000, 'televize', -2);

INSERT INTO public.financialtransaction (id_transaction, amount, balance_before, balance_after, deleted, description, sale_id, user_id) VALUES
(-10, 4000, 0, 4000, 'f', 'Nová tržba - prodané zboží: mobil', -4, -2),
(-8, -2000, 5000, 3000, 'f', 'Výběr peněz z pokladny', NULL, -2),
(-7, 10000, 3000, 13000, 'f', 'Nová tržba - prodané zboží: televize', -3, -2),
(-5, -6000, 14000, 8000, 'f', 'Výběr peněz z pokladny', NULL, -2),
(-3, -1000, 12000, 11000, 't', 'Zrušená transankce ID: -6 Tržba - ssd disk', NULL, -2),
(-6, 1000, 13000, 14000, 't', '(smazáno) Nová tržba - prodané zboží: ssd disk', NULL, -2),
(-2, -1000, 11000, 10000, 't', 'Zrušení transakce ID: -9 - Vklad peněz do pokladny', NULL, -2),
(-9, 1000, 4000, 5000, 't', '(smazáno) - Vklad peněz do pokladny', NULL, -2),
(-1, -4000, 10000, 6000, 't', 'Zrušená transankce ID: -4 Tržba - monitor', NULL, -2),
(-4, 4000, 8000, 12000, 't', '(smazáno) Nová tržba - prodané zboží: monitor', NULL, -2);