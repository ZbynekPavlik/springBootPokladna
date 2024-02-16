INSERT INTO public.users (id_user, enabled, password, role, username)
SELECT * FROM (VALUES
    (-2, 1, '{noop}b', 'ADMIN', 'Admin'),
    (-1, 1, '{noop}a', 'EMPLOYEE', 'Pokladni')
) AS data (id_user, enabled, password, role, username)
WHERE NOT EXISTS (
    SELECT 1 FROM public.users WHERE id_user IN (-2, -1)
)
ON CONFLICT DO NOTHING;

INSERT INTO public.authorities (id_authority, authority, username)
SELECT * FROM (VALUES
    (-3, 'ROLE_EMPLOYEE', 'Admin'),
    (-2, 'ROLE_ADMIN', 'Admin'),
    (-1, 'ROLE_EMPLOYEE', 'Pokladni')
) AS data (id_authority, authority, username)
WHERE NOT EXISTS (
    SELECT 1 FROM public.authorities WHERE id_authority IN (-3, -2, -1)
)
ON CONFLICT DO NOTHING;

INSERT INTO public.sales (id_sale, amount, sold_goods, user_id)
SELECT * FROM (VALUES
    (-4, 4000, 'mobil', -2),
    (-3, 10000, 'televize', -2)
) AS data (id_sale, amount, sold_goods, user_id)
WHERE NOT EXISTS (
    SELECT 1 FROM public.sales WHERE id_sale IN (-4, -3)
)
ON CONFLICT DO NOTHING;

INSERT INTO public.financialtransaction (id_transaction, amount, balance_before, balance_after, deleted, description, sale_id, user_id)
SELECT * FROM (VALUES
    (-10, 4000, 0, 4000, FALSE, 'Nová tržba - prodané zboží: mobil', -4, -2),
    (-8, -2000, 5000, 3000, FALSE, 'Výběr peněz z pokladny', NULL, -2),
    (-7, 10000, 3000, 13000, FALSE, 'Nová tržba - prodané zboží: televize', -3, -2),
    (-5, -6000, 14000, 8000, FALSE, 'Výběr peněz z pokladny', NULL, -2),
    (-3, -1000, 12000, 11000, TRUE, 'Zrušená transakce ID: -6 Tržba - ssd disk', NULL, -2),
    (-6, 1000, 13000, 14000, TRUE, '(smazáno) Nová tržba - prodané zboží: ssd disk', NULL, -2),
    (-2, -1000, 11000, 10000, TRUE, 'Zrušení transakce ID: -9 - Vklad peněz do pokladny', NULL, -2),
    (-9, 1000, 4000, 5000, TRUE, '(smazáno) - Vklad peněz do pokladny', NULL, -2),
    (-1, -4000, 10000, 6000, TRUE, 'Zrušená transankce ID: -4 Tržba - monitor', NULL, -2),
    (-4, 4000, 8000, 12000, TRUE, '(smazáno) Nová tržba - prodané zboží: monitor', NULL, -2)
) AS data (id_transaction, amount, balance_before, balance_after, deleted, description, sale_id, user_id)
WHERE NOT EXISTS (
    SELECT 1 FROM public.financialtransaction WHERE id_transaction IN (-10, -8, -7, -5, -3, -6, -2, -9, -1, -4)
)
ON CONFLICT DO NOTHING;



