--------------------------------------------------------------------------------
-- AquaVitae — Trigger de sincronização bebida_rating_medio / bebida_total_reviews
--
-- Implementado como COMPOUND TRIGGER (não como trigger simples FOR EACH ROW)
-- de propósito: um trigger de linha simples que consulta a própria tabela
-- `review` dentro do corpo dispararia ORA-04091 (mutating table) sempre que
-- uma instrução afetasse mais do que uma linha de `review` na mesma
-- transação (ex.: DELETE em massa). O compound trigger só recalcula os
-- agregados depois de a instrução completar (AFTER STATEMENT), o que evita
-- esse erro por construção.
--------------------------------------------------------------------------------

CREATE OR REPLACE TRIGGER trg_review_sync_bebida_stats
FOR INSERT OR UPDATE OR DELETE ON review
COMPOUND TRIGGER

    TYPE t_bebida_ids IS TABLE OF review.bebida_id%TYPE INDEX BY PLS_INTEGER;
    g_bebida_ids t_bebida_ids;
    g_count      PLS_INTEGER := 0;

    PROCEDURE add_bebida_id(p_bebida_id review.bebida_id%TYPE) IS
    BEGIN
        IF p_bebida_id IS NOT NULL THEN
            g_count := g_count + 1;
            g_bebida_ids(g_count) := p_bebida_id;
        END IF;
    END add_bebida_id;

    AFTER EACH ROW IS
    BEGIN
        IF INSERTING OR UPDATING THEN
            add_bebida_id(:NEW.bebida_id);
        END IF;
        IF UPDATING OR DELETING THEN
            add_bebida_id(:OLD.bebida_id);
        END IF;
    END AFTER EACH ROW;

    AFTER STATEMENT IS
    BEGIN
        FOR i IN 1 .. g_count LOOP
            UPDATE bebida b
               SET b.bebida_rating_medio  = (
                       SELECT NVL(ROUND(AVG(r.rating_value), 2), 0)
                       FROM review r
                       WHERE r.bebida_id = g_bebida_ids(i)
                   ),
                   b.bebida_total_reviews = (
                       SELECT COUNT(*)
                       FROM review r
                       WHERE r.bebida_id = g_bebida_ids(i)
                   )
             WHERE b.bebida_id = g_bebida_ids(i);
        END LOOP;

        g_bebida_ids.DELETE;
        g_count := 0;
    END AFTER STATEMENT;

END trg_review_sync_bebida_stats;
/
