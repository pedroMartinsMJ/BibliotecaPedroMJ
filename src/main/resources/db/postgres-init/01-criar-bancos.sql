-- Bancos do ambiente de simulação de produção.
-- Executado pelo docker-entrypoint-initdb.d apenas quando o volume postgres-data
-- está vazio; para um container já criado, aplique com:
--   docker exec -i biblioteca-postgres psql -U pedro -d postgres -f - < este-arquivo

-- CREATE DATABASE não roda dentro de transação, por isso o \gexec condicional.
SELECT 'CREATE DATABASE biblioteca_prod OWNER pedro ENCODING ''UTF8'''
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'biblioteca_prod')\gexec
