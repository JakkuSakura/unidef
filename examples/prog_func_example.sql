CREATE OR REPLACE FUNCTION func_example(foo integer)
    RETURNS integer AS
$$
BEGIN
    RETURN foo + 1;
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION func_example2(IN foo integer, OUT bar integer)
AS
$$
BEGIN
    SELECT foo + 1 INTO bar;
END
$$ LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION func_example3(foo integer)
    RETURNS integer
    LANGUAGE plpgsql AS
$$
BEGIN
    RETURN foo + 1;
END
$$;

CREATE OR REPLACE FUNCTION func_example4(IN foo integer, OUT bar integer)
    LANGUAGE plpgsql AS
$$
BEGIN
    SELECT foo + 1 INTO bar;
END
$$;
CREATE OR REPLACE FUNCTION func_example5()
    RETURNS TABLE (
        foo integer,
        bar integer
                  )
    LANGUAGE plpgsql
    AS
$$
BEGIN
    RETURN QUERY SELECT 1 as foo, 2 as bar;
END
$$;

CREATE OR REPLACE FUNCTION api.func_example6()
    RETURNS TABLE (
                      foo integer,
                      bar integer
                  )
    LANGUAGE plpgsql
AS
$$
BEGIN
    RETURN QUERY SELECT 1 as foo, 2 as bar;
END
$$;
